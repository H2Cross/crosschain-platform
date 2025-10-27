#!/bin/bash

# 0. 加载环境变量
source /etc/profile
source ~/.bashrc

# 1. 定义变量
REPO_DIR="/root/CIPS-Gemini-Ethereum"
LOG_DIR="$REPO_DIR/logs"
LOG_FILE="$LOG_DIR/eth.log"
GATEWAY_PID_FILE="/tmp/eth_gateway.pid"
MONITOR_PID_FILE="/tmp/eth_monitor.pid"

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
    # 检查端口8086是否被占用
    if netstat -tnlp 2>/dev/null | grep -q ":8086"; then
        log_info "端口8086已被占用，正在清理..."
        PORT_PID=$(netstat -tnlp 2>/dev/null | grep ":8086" | awk '{print $7}' | cut -d'/' -f1)
        if [ ! -z "$PORT_PID" ]; then
            kill -9 $PORT_PID
            log_info "已终止占用端口8086的进程 (PID: $PORT_PID)"
            sleep 2
        fi
    fi

    # 检查是否有以太坊网关进程在运行
    if pgrep -f "CIPS-Gemini-Ethereum" > /dev/null; then
        log_info "发现以太坊网关进程，正在清理..."
        pkill -f "CIPS-Gemini-Ethereum"
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

# 4. 创建日志目录
mkdir -p "$LOG_DIR"
if [ $? -ne 0 ]; then
    log_error "创建日志目录失败"
    exit 1
fi

# 5. 进入指定目录
cd "$REPO_DIR"
if [ $? -ne 0 ]; then
    log_error "进入目录 $REPO_DIR 失败"
    log_error "当前目录: $(pwd)"
    exit 1
fi

log_info "当前工作目录: $(pwd)"

# 6. 检查必要文件
if [ ! -f "helper.sh" ] || [ ! -f "config.toml" ]; then
    log_error "必要文件不存在 (helper.sh 或 config.toml)"
    exit 1
fi

# 7. 确保helper.sh有执行权限
chmod +x helper.sh

# 8. 清理旧进程和日志
cleanup

# 9. 启动网关
log_info "开始启动以太坊网关..."

# 记录环境信息
which go > "$LOG_FILE" 2>&1
go version >> "$LOG_FILE" 2>&1

# 启动网关并直接写入日志
./helper.sh start_relayer config.toml > "$LOG_FILE" 2>&1 &
ETH_PID=$!

# 保存PID
echo $ETH_PID > "$GATEWAY_PID_FILE"
log_info "网关进程已启动 (PID: $ETH_PID)"

# 10. 等待网关启动
sleep 5

# 11. 检查网关是否成功启动
if ! ps -p $ETH_PID > /dev/null; then
    log_error "以太坊网关启动失败"
    log_error "完整启动日志："
    cat "$LOG_FILE"
    exit 1
fi

log_success "以太坊网关已成功启动"
log_info "PID: $ETH_PID"
log_info "日志文件: $LOG_FILE"

# 12. 启动监控进程（在后台运行）
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
        # 检查进程名、PID和端口
        if (! pgrep -f "helper.sh start_relayer" > /dev/null && ! ps -p $ETH_PID > /dev/null) && ! netstat -tnlp 2>/dev/null | grep -q ":8086"; then
            log_error "网关进程可能已终止！请检查进程和端口状态"
            # 不立即退出，只记录警告
            sleep 300  # 降低检查频率，5分钟检查一次
            continue
        fi
        sleep 60
    done
) &

MONITOR_PID=$!
log_info "监控进程已启动 (PID: $MONITOR_PID)"

# 主进程退出
exit 0