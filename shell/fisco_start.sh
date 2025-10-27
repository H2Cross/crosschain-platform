#!/bin/bash

# FISCO网关启动脚本（增强版）
# 功能：自动检查和启动FISCO节点，启动网关，验证查询功能

# 0. 加载环境变量
source /etc/profile
source ~/.bashrc

# 1. 检查是否传入了参数
if [ $# -ne 2 ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: 参数数量不正确"
    echo "Usage: $0 <target_chain_id> <target_url>"
    echo "Example: $0 11002 http://192.168.0.2:8088"
    echo "Received parameters: $@"
    exit 1
fi

# 2. 获取传入的参数
TARGET_CHAIN_ID=$1
TARGET_URL=$2

# 3. 定义变量
REPO_DIR="/root/CIPS-Gemini-v1/CIPS-Gemini-Fisco"
CONFIG_FILE="$REPO_DIR/config.yml"
LOG_DIR="$REPO_DIR/logs"
LOG_FILE="$LOG_DIR/fisco.log"
GATEWAY_PID_FILE="/tmp/fisco_gateway.pid"
FISCO_PORT=8091
FISCO_NODE_DIR="/root/CIPS-Gemini-v1/CIPS-Gemini-Fisco/fisco/nodes/127.0.0.1"

# 4. 定义日志函数
log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $1" | tee -a "$LOG_FILE"
}

# 5. 检查和启动FISCO节点函数
check_and_start_fisco_nodes() {
    log_info "检查FISCO节点状态..."
    
    # 检查节点端口是否在监听
    if netstat -tln 2>/dev/null | grep -q ":20200"; then
        log_success "✅ FISCO节点已在运行"
        return 0
    fi
    
    log_info "FISCO节点未运行，尝试启动..."
    
    # 检查启动脚本是否存在
    if [ -f "$FISCO_NODE_DIR/start_all.sh" ]; then
        log_info "使用start_all.sh启动节点..."
        cd "$FISCO_NODE_DIR"
        chmod +x start_all.sh
        ./start_all.sh
        
        # 等待节点启动
        log_info "等待节点启动..."
        for i in {1..10}; do
            sleep 2
            if netstat -tln 2>/dev/null | grep -q ":20200"; then
                log_success "✅ FISCO节点启动成功"
                return 0
            fi
            log_info "等待节点启动... ($i/10)"
        done
        
        log_error "❌ FISCO节点启动失败"
        return 1
    else
        log_error "❌ 启动脚本不存在: $FISCO_NODE_DIR/start_all.sh"
        return 1
    fi
}

# 6. 验证FISCO查询功能
verify_fisco_query() {
    log_info "========== 开始FISCO查询验证 =========="
    
    cd "$REPO_DIR"
    
    log_info "等待5秒让网关完全启动..."
    sleep 5
    
    # 检查网关API状态
    log_info "检查网关API状态..."
    if netstat -tlnp | grep -q ":$FISCO_PORT"; then
        log_success "✅ FISCO网关API正在监听端口 $FISCO_PORT"
        log_info "支持的API端点: /cm/req, /cm/resp, /register/gateway"
    else
        log_error "❌ FISCO网关API未监听端口 $FISCO_PORT"
    fi
    
    log_info "========== FISCO查询验证完成 =========="
}

# ====== 主执行流程开始 ======

log_info "环境变量："
log_info "REPO_DIR=$REPO_DIR"
log_info "CONFIG_FILE=$CONFIG_FILE"
log_info "TARGET_URL=$TARGET_URL"

# 7. 进入工作目录
cd "$REPO_DIR" || {
    log_error "无法进入目录: $REPO_DIR"
    exit 1
}
log_info "当前工作目录: $(pwd)"

# 8. 检查并启动FISCO节点
log_info "开始检查FISCO节点..."
check_and_start_fisco_nodes || {
    log_error "FISCO节点检查失败，退出"
    exit 1
}

# 9. 更新配置文件中的目标URL
log_info "开始更新配置文件"
if [ -f "$CONFIG_FILE" ]; then
    # 更新targets部分的URL
    sed -i "s|url: .*|url: $TARGET_URL|g" "$CONFIG_FILE"
    log_info "配置文件已更新，目标URL: $TARGET_URL"
else
    log_error "配置文件不存在: $CONFIG_FILE"
    exit 1
fi

# 10. 检查端口是否被占用
log_info "检查端口 $FISCO_PORT 是否被占用..."
if netstat -tnlp 2>/dev/null | grep -q ":$FISCO_PORT"; then
    log_error "端口 $FISCO_PORT 已被占用，请检查是否有其他FISCO网关在运行"
    netstat -tnlp 2>/dev/null | grep ":$FISCO_PORT"
    exit 1
else
    log_info "端口 $FISCO_PORT 未被占用"
fi

# 11. 创建日志目录
mkdir -p "$LOG_DIR"

# 12. 启动FISCO网关
log_info "开始启动FISCO网关..."
cd "$REPO_DIR" || {
    log_error "无法进入FISCO目录: $REPO_DIR"
    exit 1
}
log_info "当前启动目录: $(pwd)"
nohup go run main.go start >> "$LOG_FILE" 2>&1 &
GATEWAY_PID=$!
echo $GATEWAY_PID > "$GATEWAY_PID_FILE"
log_info "网关进程已启动 (PID: $GATEWAY_PID)"

# 13. 等待网关启动
sleep 5

# 14. 检查网关是否启动成功
if ps -p $GATEWAY_PID > /dev/null 2>&1 && netstat -tnlp 2>/dev/null | grep -q ":$FISCO_PORT"; then
    log_success "🎉 FISCO网关启动成功！"
    
    # 执行单链查询验证
    log_info "执行FISCO单链查询验证..."
    verify_fisco_query
    
    log_success "🎉 FISCO完整启动和验证完成！"
else
    log_error "FISCO网关启动失败"
    exit 1
fi
