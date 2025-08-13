#!/bin/bash

# 0. 加载环境变量
source /etc/profile
source ~/.bashrc

# 1. 检查是否传入了参数
if [ $# -ne 1 ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: 参数数量不正确"
    echo "Usage: $0 <dst_chain_id>"
    echo "Received parameters: $@"
    exit 1
fi

# 2. 获取传入的参数
DST_CHAIN_ID=$1

# 3. 定义变量
H2CHAIN_DIR=/root/CIPS-Gemini-H2Chain
CONFIG_FILE="$H2CHAIN_DIR/config.yml"
LOG_DIR="$H2CHAIN_DIR/logs"
LOG_FILE="$LOG_DIR/h2chain.log"
GATEWAY_PID_FILE="/tmp/h2chain_gateway.pid"
MONITOR_PID_FILE="/tmp/h2chain_monitor.pid"

# 4. 定义日志函数
log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $1" >> "$LOG_FILE"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >> "$LOG_FILE"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $1" >> "$LOG_FILE"
}

# 5. 清理旧进程和日志
cleanup() {
    # 检查端口8087是否被占用
    if netstat -tnlp 2>/dev/null | grep -q ":8087"; then
        log_info "端口8087已被占用，正在清理..."
        PORT_PID=$(netstat -tnlp 2>/dev/null | grep ":8087" | awk '{print $7}' | cut -d'/' -f1)
        if [ ! -z "$PORT_PID" ]; then
            kill -9 $PORT_PID
            log_info "已终止占用端口8087的进程 (PID: $PORT_PID)"
            sleep 2
        fi
    fi

    # 检查是否有crossH2C进程在运行
    if pgrep -f "crossH2C" > /dev/null; then
        log_info "发现crossH2C进程，正在清理..."
        pkill -f "crossH2C"
        sleep 2
    fi

    # 清理旧进程
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

    # 清理旧日志
    log_info "清理旧日志文件..."
    if [ -d "$LOG_DIR" ]; then
        # 如果旧日志存在，将其重命名为备份
        if [ -f "$LOG_FILE" ]; then
            mv "$LOG_FILE" "${LOG_FILE}.$(date +"%Y%m%d_%H%M%S").bak"
        fi
        # 创建新的空日志文件
        touch "$LOG_FILE"
        chmod 666 "$LOG_FILE"
    fi
}

# 6. 记录环境信息
log_info "环境变量："
log_info "H2CHAIN_DIR=$H2CHAIN_DIR"
log_info "CONFIG_FILE=$CONFIG_FILE"
log_info "PATH=$PATH"
log_info "GOPATH=$GOPATH"
log_info "GOROOT=$GOROOT"

# 7. 创建日志目录
mkdir -p "$LOG_DIR"
if [ $? -ne 0 ]; then
    log_error "创建日志目录失败"
    exit 1
fi

# 8. 进入指定目录
cd "$H2CHAIN_DIR"
if [ $? -ne 0 ]; then
    log_error "进入目录 $H2CHAIN_DIR 失败"
    log_error "当前目录: $(pwd)"
    exit 1
fi

log_info "当前工作目录: $(pwd)"

# 9. 备份配置文件
cp "$CONFIG_FILE" "${CONFIG_FILE}.bak"
if [ $? -ne 0 ]; then
    log_error "备份配置文件失败"
    exit 1
fi

# 10. 更新配置文件
log_info "开始更新配置文件"
sed -i "/^test:/,/^[^ ]/ s/dst_chain_id: [0-9]*/dst_chain_id: $DST_CHAIN_ID/" "$CONFIG_FILE"
if [ $? -ne 0 ]; then
    log_error "更新配置文件失败"
    mv "${CONFIG_FILE}.bak" "$CONFIG_FILE"
    exit 1
fi

# 11. 清理旧进程和日志
cleanup

# 12. 启动海河链网关
log_info "开始启动海河链网关..."

# 检查环境
which go > "$LOG_FILE" 2>&1
go version >> "$LOG_FILE" 2>&1

# 启动网关并直接写入日志
nohup ./crossH2C start > "$LOG_FILE" 2>&1 &
H2CHAIN_PID=$!

# 保存PID
echo $H2CHAIN_PID > "$GATEWAY_PID_FILE"
log_info "网关进程已启动 (PID: $H2CHAIN_PID)"

# 13. 等待网关启动
sleep 5

# 14. 检查网关是否成功启动
if ! ps -p $H2CHAIN_PID > /dev/null; then
    log_error "海河链网关启动失败"
    log_error "完整启动日志："
    cat "$LOG_FILE"
    mv "${CONFIG_FILE}.bak" "$CONFIG_FILE"
    exit 1
fi

# 15. 启动监控进程（在后台运行）
(
    # 监控进程的清理函数
    cleanup_monitor() {
        log_info "监控进程正在退出..."
        rm -f "$MONITOR_PID_FILE"
        exit 0
    }

    # 注册信号处理
    trap cleanup_monitor SIGINT SIGTERM

    # 记录监控进程PID
    echo $$ > "$MONITOR_PID_FILE"
    
    # 进程状态监控循环
    while true; do
        # 使用pgrep检查进程
        if ! pgrep -f "crossH2C start" > /dev/null && ! ps -p $H2CHAIN_PID > /dev/null; then
            log_error "网关进程已终止！"
            exit 1
        fi
        sleep 60
    done
) &

MONITOR_PID=$!
log_info "监控进程已启动 (PID: $MONITOR_PID)"

# 16. 删除备份文件
rm -f "${CONFIG_FILE}.bak"

# 17. 输出最终启动信息
log_success "海河链网关已成功启动"
log_info "网关进程 PID: $H2CHAIN_PID"
log_info "监控进程 PID: $MONITOR_PID"
log_info "日志文件: $LOG_FILE"
log_info "目标链ID: $DST_CHAIN_ID"

# 主进程退出
exit 0 