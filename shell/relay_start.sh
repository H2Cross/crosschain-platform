#!/bin/bash

# 0. 加载环境变量
source /etc/profile
source ~/.bashrc

# 1. 定义变量
RELAY_DIR=/root/CIPS-Gemini-RelayChain
LOG_DIR="$RELAY_DIR/logs"
LOG_FILE="$LOG_DIR/relay.log"
GATEWAY_PID_FILE="/tmp/relay_gateway.pid"
MONITOR_PID_FILE="/tmp/relay_monitor.pid"

# 2. 定义日志函数
log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $1" >> "$LOG_FILE"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >> "$LOG_FILE"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $1" >> "$LOG_FILE"
}

# 3. 清理旧进程和日志
cleanup() {
    # 检查端口8300是否被占用
    if netstat -tnlp 2>/dev/null | grep -q ":8300"; then
        log_info "端口8300已被占用，正在清理..."
        PORT_PID=$(netstat -tnlp 2>/dev/null | grep ":8300" | awk '{print $7}' | cut -d'/' -f1)
        if [ ! -z "$PORT_PID" ]; then
            kill -9 $PORT_PID
            log_info "已终止占用端口8300的进程 (PID: $PORT_PID)"
            sleep 2
        fi
    fi

    # 检查是否有中继链网关进程在运行
    if pgrep -f "CIPS-Gemini-RelayChain" > /dev/null; then
        log_info "发现中继链网关进程，正在清理..."
        pkill -f "CIPS-Gemini-RelayChain"
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

# 4. 记录环境信息
log_info "环境变量："
log_info "RELAY_DIR=$RELAY_DIR"
log_info "PATH=$PATH"
log_info "GOPATH=$GOPATH"
log_info "GOROOT=$GOROOT"

# 5. 创建日志目录
mkdir -p "$LOG_DIR"
if [ $? -ne 0 ]; then
    log_error "创建日志目录失败"
    exit 1
fi

# 6. 进入指定目录
cd "$RELAY_DIR"
if [ $? -ne 0 ]; then
    log_error "进入目录 $RELAY_DIR 失败"
    log_error "当前目录: $(pwd)"
    exit 1
fi

log_info "当前工作目录: $(pwd)"

# 7. 清理旧进程和日志
cleanup

# 8. 启动中继链网关
log_info "开始启动中继链网关..."

# 检查环境
which go > "$LOG_FILE" 2>&1
go version >> "$LOG_FILE" 2>&1

# 启动网关并直接写入日志
nohup go run main.go start > "$LOG_FILE" 2>&1 &
RELAY_PID=$!

# 保存PID
echo $RELAY_PID > "$GATEWAY_PID_FILE"

# 9. 等待网关启动
sleep 5

# 10. 检查网关是否成功启动
if ! ps -p $RELAY_PID > /dev/null; then
    log_error "中继链网关启动失败"
    exit 1
fi

# 11. 启动监控进程（在后台运行）
(
    # 监控进程的清理函数
    cleanup_monitor() {
        rm -f "$MONITOR_PID_FILE"
        exit 0
    }

    # 注册信号处理
    trap cleanup_monitor SIGINT SIGTERM

    # 记录监控进程PID
    echo $$ > "$MONITOR_PID_FILE"
    
    # 进程状态监控循环
    while true; do
        # 使用pgrep检查是否有相关进程在运行
        if ! pgrep -f "go run main.go start" > /dev/null; then
            # 再次确认PID文件中的进程
            if [ -f "$GATEWAY_PID_FILE" ]; then
                CURRENT_PID=$(cat "$GATEWAY_PID_FILE")
                if ! ps -p $CURRENT_PID > /dev/null; then
                    exit 1
                fi
            fi
        fi
        sleep 60
    done
) &

MONITOR_PID=$!

# 12. 执行注册流程
go run main.go register >> "$LOG_FILE" 2>&1
if [ $? -ne 0 ]; then
    log_error "注册流程执行失败"
else
    log_success "注册流程执行成功"
fi

# 13. 输出最终启动信息
log_success "中继链网关已成功启动"
log_info "网关进程 PID: $RELAY_PID"
log_info "监控进程 PID: $MONITOR_PID"
log_info "日志文件: $LOG_FILE"

# 主进程退出
exit 0 