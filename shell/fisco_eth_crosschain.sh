#!/bin/bash

################################################################################
# FISCO ↔ 以太坊 跨链测试脚本
# 功能：测试FISCO和以太坊之间的双向跨链交易
# 作者：自动生成
# 日期：2025-10-23
################################################################################

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置路径
FISCO_DIR="/root/CIPS-Gemini-v1/CIPS-Gemini-Fisco"
ETH_DIR="/root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum"
FISCO_LOG="/root/fisco_relayer.log"
ETH_LOG="/root/CIPS-Gemini-v1/CIPS-Gemini-Ethereum/logs/eth.log"

# 链配置
FISCO_CHAIN_ID=16002
ETH_CHAIN_ID=12002
FISCO_GATEWAY="http://0.0.0.0:8091"
ETH_GATEWAY="http://192.168.0.6:8086"

################################################################################
# 函数定义
################################################################################

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
}

# 检查relayer状态
check_relayer_status() {
    print_header "检查Relayer运行状态"
    
    # 检查FISCO relayer
    if pgrep -f "CIPS-Gemini-Fisco.*main.go start" > /dev/null; then
        print_success "FISCO relayer 正在运行 (PID: $(pgrep -f 'CIPS-Gemini-Fisco.*main.go start'))"
    else
        print_error "FISCO relayer 未运行"
        return 1
    fi
    
    # 检查以太坊relayer
    if pgrep -f "CIPS-Gemini-Ethereum.*helper.sh" > /dev/null; then
        print_success "以太坊 relayer 正在运行 (PID: $(pgrep -f 'CIPS-Gemini-Ethereum.*helper.sh'))"
    else
        print_error "以太坊 relayer 未运行"
        return 1
    fi
    
    # 检查网关端口
    print_info "检查网关端口..."
    if netstat -tlnp 2>/dev/null | grep -q ":8091.*LISTEN"; then
        print_success "FISCO网关 (8091) 正在监听"
    else
        print_error "FISCO网关 (8091) 未监听"
        return 1
    fi
    
    if netstat -tlnp 2>/dev/null | grep -q ":8086.*LISTEN"; then
        print_success "以太坊网关 (8086) 正在监听"
    else
        print_error "以太坊网关 (8086) 未监听"
        return 1
    fi
    
    echo ""
    return 0
}

# 启动FISCO relayer
start_fisco_relayer() {
    print_info "启动FISCO relayer..."
    cd "$FISCO_DIR" || exit 1
    nohup go run main.go start > "$FISCO_LOG" 2>&1 &
    sleep 5
    
    if pgrep -f "CIPS-Gemini-Fisco.*main.go start" > /dev/null; then
        print_success "FISCO relayer 启动成功"
        return 0
    else
        print_error "FISCO relayer 启动失败，请检查日志: $FISCO_LOG"
        return 1
    fi
}

# 启动以太坊relayer
start_eth_relayer() {
    print_info "启动以太坊 relayer..."
    cd "$ETH_DIR" || exit 1
    
    # 检查是否已经运行
    if pgrep -f "CIPS-Gemini-Ethereum.*helper.sh" > /dev/null; then
        print_warning "以太坊 relayer 已经在运行"
        return 0
    fi
    
    nohup bash helper.sh > /dev/null 2>&1 &
    sleep 5
    
    if pgrep -f "CIPS-Gemini-Ethereum.*helper.sh" > /dev/null; then
        print_success "以太坊 relayer 启动成功"
        return 0
    else
        print_error "以太坊 relayer 启动失败"
        return 1
    fi
}

# FISCO -> 以太坊跨链
fisco_to_eth() {
    local message="${1:-fisco_to_eth_test_$(date +%s)}"
    
    print_header "FISCO → 以太坊 跨链测试"
    print_info "消息内容: $message"
    print_info "源链: FISCO ($FISCO_CHAIN_ID)"
    print_info "目标链: 以太坊 ($ETH_CHAIN_ID)"
    echo ""
    
    cd "$FISCO_DIR" || exit 1
    
    # 发送跨链交易
    print_info "发送跨链交易..."
    ./crossFab test "$ETH_CHAIN_ID" 1 1 "$message"
    
    if [ $? -eq 0 ]; then
        echo ""
        print_success "跨链交易发送成功！"
        
        # 等待处理
        print_info "等待跨链处理 (5秒)..."
        sleep 5
        
        # 检查日志
        print_info "检查FISCO relayer日志..."
        if tail -20 "$FISCO_LOG" | grep -q "successfully get response"; then
            print_success "✅ FISCO端确认：成功发送到以太坊网关并收到响应"
            
            # 提取cmhash
            cmhash=$(tail -50 "$FISCO_LOG" | grep "Parsed CmHash" | tail -1 | grep -oP 'hash=[a-f0-9]+' | cut -d'=' -f2)
            if [ -n "$cmhash" ]; then
                print_info "CmHash: $cmhash"
            fi
        else
            print_warning "未在日志中找到成功响应"
        fi
        
        print_info "检查以太坊 relayer日志..."
        if tail -30 "$ETH_LOG" | grep -q "get req txhash"; then
            txhash=$(tail -30 "$ETH_LOG" | grep "get req txhash" | tail -1 | grep -oP '0x[a-f0-9]+')
            if [ -n "$txhash" ]; then
                print_success "✅ 以太坊端确认：接收到跨链交易"
                print_info "以太坊交易Hash: $txhash"
            fi
        else
            print_warning "未在以太坊日志中找到接收记录"
        fi
        
        echo ""
        print_success "========== 跨链测试完成 =========="
        echo ""
    else
        print_error "跨链交易发送失败"
        return 1
    fi
}

# 以太坊 -> FISCO跨链
eth_to_fisco() {
    local message="${1:-eth_to_fisco_test_$(date +%s)}"
    
    print_header "以太坊 → FISCO 跨链测试"
    print_info "消息内容: $message"
    print_info "源链: 以太坊 ($ETH_CHAIN_ID)"
    print_info "目标链: FISCO ($FISCO_CHAIN_ID)"
    echo ""
    
    print_warning "注意：以太坊到FISCO的跨链功能需要以太坊端支持发送跨链交易"
    print_warning "当前脚本暂不支持此功能，需要手动操作"
    echo ""
    print_info "手动操作步骤："
    print_info "1. cd $ETH_DIR"
    print_info "2. 使用以太坊的发送跨链交易命令"
    echo ""
}

# 查看日志
view_logs() {
    local log_type="$1"
    local lines="${2:-30}"
    
    case "$log_type" in
        fisco|f)
            print_header "FISCO Relayer 日志 (最近${lines}行)"
            tail -n "$lines" "$FISCO_LOG"
            ;;
        eth|e)
            print_header "以太坊 Relayer 日志 (最近${lines}行)"
            tail -n "$lines" "$ETH_LOG"
            ;;
        both|b)
            print_header "FISCO Relayer 日志 (最近${lines}行)"
            tail -n "$lines" "$FISCO_LOG"
            echo ""
            print_header "以太坊 Relayer 日志 (最近${lines}行)"
            tail -n "$lines" "$ETH_LOG"
            ;;
        *)
            print_error "未知的日志类型: $log_type"
            print_info "可用选项: fisco|f, eth|e, both|b"
            return 1
            ;;
    esac
}

# 停止relayers
stop_relayers() {
    print_header "停止Relayers"
    
    # 停止FISCO relayer
    if pgrep -f "CIPS-Gemini-Fisco.*main.go start" > /dev/null; then
        print_info "停止FISCO relayer..."
        pkill -f "CIPS-Gemini-Fisco.*main.go start"
        sleep 2
        print_success "FISCO relayer 已停止"
    else
        print_info "FISCO relayer 未运行"
    fi
    
    # 停止以太坊relayer
    if pgrep -f "CIPS-Gemini-Ethereum.*helper.sh" > /dev/null; then
        print_info "停止以太坊 relayer..."
        pkill -f "CIPS-Gemini-Ethereum.*helper.sh"
        sleep 2
        print_success "以太坊 relayer 已停止"
    else
        print_info "以太坊 relayer 未运行"
    fi
}

# 显示帮助信息
show_help() {
    cat << EOF
${BLUE}========================================${NC}
${BLUE}FISCO ↔ 以太坊 跨链测试脚本${NC}
${BLUE}========================================${NC}

用法: $0 [命令] [参数]

${GREEN}命令:${NC}
  status              检查relayer运行状态
  start-fisco         启动FISCO relayer
  start-eth           启动以太坊 relayer
  start-all           启动所有relayers
  
  test-f2e [消息]     测试 FISCO → 以太坊 跨链
  test-e2f [消息]     测试 以太坊 → FISCO 跨链
  
  logs <类型> [行数]  查看日志
                      类型: fisco|f, eth|e, both|b
                      行数: 默认30
  
  stop                停止所有relayers
  help                显示此帮助信息

${GREEN}示例:${NC}
  $0 status                    # 检查状态
  $0 start-all                 # 启动所有relayers
  $0 test-f2e "Hello ETH"      # FISCO发送消息到以太坊
  $0 logs fisco 50             # 查看FISCO日志最近50行
  $0 logs both                 # 查看两条链的日志
  $0 stop                      # 停止所有relayers

${GREEN}配置信息:${NC}
  FISCO链ID:        $FISCO_CHAIN_ID
  以太坊链ID:       $ETH_CHAIN_ID
  FISCO网关:        $FISCO_GATEWAY
  以太坊网关:       $ETH_GATEWAY
  
  FISCO目录:        $FISCO_DIR
  以太坊目录:       $ETH_DIR
  
  FISCO日志:        $FISCO_LOG
  以太坊日志:       $ETH_LOG

EOF
}

################################################################################
# 主程序
################################################################################

# 检查参数
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

# 处理命令
case "$1" in
    status)
        check_relayer_status
        ;;
    start-fisco)
        start_fisco_relayer
        ;;
    start-eth)
        start_eth_relayer
        ;;
    start-all)
        start_fisco_relayer
        start_eth_relayer
        sleep 2
        check_relayer_status
        ;;
    test-f2e)
        check_relayer_status || {
            print_error "请先启动relayers: $0 start-all"
            exit 1
        }
        fisco_to_eth "$2"
        ;;
    test-e2f)
        eth_to_fisco "$2"
        ;;
    logs)
        view_logs "$2" "$3"
        ;;
    stop)
        stop_relayers
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "未知命令: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

exit 0
