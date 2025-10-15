#!/bin/bash

# 0. 加载环境变量
source /etc/profile
source ~/.bashrc

# 1. 检查是否传入了参数
if [ $# -ne 4 ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: 参数数量不正确"
    echo "Usage: $0 <source_chain_id> <target_chain_id> <target_ip> <target_port>"
    echo "Received parameters: $@"
    exit 1
fi

# 2. 获取传入的参数
SOURCE_CHAIN_ID=$1
TARGET_CHAIN_ID=$2
TARGET_IP=$3
TARGET_PORT=$4

# 3. 定义变量
REPO_DIR=/root/CIPS-Gemini-v1/CIPS-Gemini-Bubi
TARGET_FILE="$REPO_DIR/relayer/tests/test_transport.go"
NEW_URL="http://$TARGET_IP:$TARGET_PORT"
LOG_DIR="$REPO_DIR/logs"
LOG_FILE="$LOG_DIR/bubi.log"
GATEWAY_PID_FILE="/tmp/bubi_gateway.pid"
MONITOR_PID_FILE="/tmp/bubi_monitor.pid"
CHECK_PORT=8089  # 需要检查的端口

# 4. 定义日志函数
log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $1" >> "$LOG_FILE"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $1"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >> "$LOG_FILE"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $1" >> "$LOG_FILE"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $1"
}

# 5. 检查指定端口是否被占用，如果被占用则杀死进程
check_port() {
    local port=$1
    log_info "检查端口 $port 是否被占用..."
    
    # 查找占用端口的进程
    local pid=$(lsof -ti:$port)
    
    if [ -n "$pid" ]; then
        log_info "端口 $port 被进程 $pid 占用，正在终止该进程..."
        kill -9 $pid
        sleep 2
        
        # 再次检查确认进程已被终止
        if lsof -ti:$port > /dev/null; then
            log_error "无法终止占用端口 $port 的进程，请手动检查"
            return 1
        else
            log_success "已成功终止占用端口 $port 的进程"
        fi
    else
        log_info "端口 $port 未被占用"
    fi
    
    return 0
}

# 6. 清理旧进程和日志
cleanup() {
    if [ -f "$GATEWAY_PID_FILE" ]; then
        OLD_PID=$(cat "$GATEWAY_PID_FILE")
        if ps -p $OLD_PID > /dev/null; then
            log_info "发现旧的网关进程 (PID: $OLD_PID)，正在终止..."
            kill $OLD_PID
            sleep 2
        fi
    fi
    
    if [ -f "$MONITOR_PID_FILE" ]; then
        OLD_MONITOR_PID=$(cat "$MONITOR_PID_FILE")
        if ps -p $OLD_MONITOR_PID > /dev/null; then
            log_info "发现旧的监控进程 (PID: $OLD_MONITOR_PID)，正在终止..."
            kill $OLD_MONITOR_PID
            sleep 2
        fi
    fi

    if [ -d "$LOG_DIR" ]; then
        if [ -f "$LOG_FILE" ]; then
            mv "$LOG_FILE" "${LOG_FILE}.$(date +"%Y%m%d_%H%M%S").bak"
        fi
        touch "$LOG_FILE"
        chmod 666 "$LOG_FILE"
    fi
}

# 7. 记录环境信息
log_info "环境变量："
log_info "REPO_DIR=$REPO_DIR"
log_info "TARGET_FILE=$TARGET_FILE"
log_info "NEW_URL=$NEW_URL"
log_info "PATH=$PATH"
log_info "GOPATH=$GOPATH"
log_info "GOROOT=$GOROOT"

# 8. 创建日志目录
mkdir -p "$LOG_DIR"
if [ $? -ne 0 ]; then
    log_error "创建日志目录失败"
    exit 1
fi

# 9. 进入指定目录
cd "$REPO_DIR"
if [ $? -ne 0 ]; then
    log_error "进入目录 $REPO_DIR 失败"
    log_error "当前目录: $(pwd)"
    exit 1
fi

log_info "当前工作目录: $(pwd)"

# 10. 备份原文件
cp "$TARGET_FILE" "${TARGET_FILE}.bak"
if [ $? -ne 0 ]; then
    log_error "备份文件失败"
    exit 1
fi

# 11. 更新配置文件
log_info "开始更新配置文件"

# 替换源链ID和目标链ID
sed -i "22s/source_chain_id int64 = [0-9]*/source_chain_id int64 = $SOURCE_CHAIN_ID/" "$TARGET_FILE"
if [ $? -ne 0 ]; then
    log_error "更新源链ID失败"
    mv "${TARGET_FILE}.bak" "$TARGET_FILE"
    exit 1
fi

sed -i "23s/target_chain_id int64 = [0-9]*/target_chain_id int64 = $TARGET_CHAIN_ID/" "$TARGET_FILE"
if [ $? -ne 0 ]; then
    log_error "更新目标链ID失败"
    mv "${TARGET_FILE}.bak" "$TARGET_FILE"
    exit 1
fi

# 替换目标URL
sed -i '29s|"http://[^"]*"|"'"$NEW_URL"'"|' "$TARGET_FILE"
if [ $? -ne 0 ]; then
    log_error "更新URL失败"
    mv "${TARGET_FILE}.bak" "$TARGET_FILE"
    exit 1
fi

# 12. 检查配置更新
log_info "更新后的配置内容："
sed -n '22,23p;29p' "$TARGET_FILE" >> "$LOG_FILE"

# 13. 检查端口占用情况
check_port $CHECK_PORT
if [ $? -ne 0 ]; then
    log_error "端口 $CHECK_PORT 检查失败，脚本将退出"
    mv "${TARGET_FILE}.bak" "$TARGET_FILE"
    exit 1
fi

# 14. 清理旧进程和日志
cleanup

# 15. 启动布比链网关
log_info "开始启动布比链网关..."

# 检查环境
docker ps | grep crossbubi_container > "$LOG_FILE" 2>&1

# 启动网关并直接写入日志
nohup docker exec crossbubi_container go run main.go source > "$LOG_FILE" 2>&1 &
BUBI_PID=$!

# 保存PID
echo $BUBI_PID > "$GATEWAY_PID_FILE"
log_info "网关进程已启动 (PID: $BUBI_PID)"

# 16. 等待网关启动
sleep 5

# 17. 检查网关是否成功启动
if ! ps -p $BUBI_PID > /dev/null; then
    log_error "布比链网关启动失败"
    log_error "完整启动日志："
    cat "$LOG_FILE"
    mv "${TARGET_FILE}.bak" "$TARGET_FILE"
    exit 1
fi

# 18. 启动监控进程
(
    cleanup_monitor() {
        log_info "监控进程正在退出..."
        rm -f "$MONITOR_PID_FILE"
        exit 0
    }

    trap cleanup_monitor SIGINT SIGTERM

    echo $$ > "$MONITOR_PID_FILE"
    
    while true; do
        if ! ps -p $BUBI_PID > /dev/null; then
            log_error "网关进程已终止！"
            exit 1
        fi
        sleep 60
    done
) &

MONITOR_PID=$!
log_info "监控进程已启动 (PID: $MONITOR_PID)"

# 19. 删除备份文件
rm -f "${TARGET_FILE}.bak"

# 20. 输出最终启动信息
log_success "布比链网关已成功启动"
log_info "网关进程 PID: $BUBI_PID"
log_info "监控进程 PID: $MONITOR_PID"
log_info "日志文件: $LOG_FILE"
log_info "源链ID: $SOURCE_CHAIN_ID"
log_info "目标链ID: $TARGET_CHAIN_ID"
log_info "目标URL: $NEW_URL"

# 主进程退出
exit 0 