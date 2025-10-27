#!/bin/bash

# FISCO区块链单链查询脚本 - 简化版本
# 作者：GitHub Copilot
# 版本：v1.3 (添加 latest_block 中的交易哈希列表)

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

FISCO_DIR="~/CIPS-Gemini-v1/CIPS-Gemini-Fisco"
FISCO_DIR_EXPANDED="${FISCO_DIR/#\~/$HOME}"

# 快速查询区块高度
quick_query() {
    log_info "执行快速区块高度查询..."
JSON_FILE="$(pwd)/fisco_blockchain_info.json"
    cd "$FISCO_DIR_EXPANDED"
    
    cat > temp_query.go << 'GOEOF'
package main
import (
    "context"
    "encoding/hex"
    "fmt"
    "log"
    "github.com/FISCO-BCOS/go-sdk/v3/client"
)
func main() {
    privateKey, _ := hex.DecodeString("145e247e170ba3afd6ae97e88f00dbc976c2345d511b0f6713355d19d8b80b58")
    config := &client.Config{
        IsSMCrypto: false, GroupID: "group0", PrivateKey: privateKey,
        Host: "127.0.0.1", Port: 20200,
        TLSCaFile: "./ca.crt", TLSKeyFile: "./sdk.key", TLSCertFile: "./sdk.crt",
    }
    client, err := client.DialContext(context.Background(), config)
    if err != nil { log.Fatalf("❌ 连接失败: %v", err) }
    defer client.Close()
    blockNumber, err := client.GetBlockNumber(context.Background())
    if err != nil { log.Fatalf("❌ 查询失败: %v", err) }
    fmt.Printf("📦 当前区块高度: %d\n", blockNumber)
}
GOEOF
    
    go run temp_query.go && rm temp_query.go
}

# 详细查询（保持原终端打印，同时生成 JSON，添加 latest_block.transactions）
detail_query() {
    log_info "执行详细区块链查询..."
JSON_FILE="$(pwd)/fisco_blockchain_info.json"
    cd "$FISCO_DIR_EXPANDED"
    
    # 临时文件捕获 Go 输出（纯文本数据）
    TEMP_DATA="$FISCO_DIR_EXPANDED/temp_detail_data.txt"
    
    cat > temp_detail.go << 'GOEOF'
package main
import (
    "context"
    "encoding/hex"
    "fmt"
    "log"
    "time"
    "github.com/FISCO-BCOS/go-sdk/v3/client"
)
func main() {
    privateKey, _ := hex.DecodeString("145e247e170ba3afd6ae97e88f00dbc976c2345d511b0f6713355d19d8b80b58")
    config := &client.Config{
        IsSMCrypto: false, GroupID: "group0", PrivateKey: privateKey,
        Host: "127.0.0.1", Port: 20200,
        TLSCaFile: "./ca.crt", TLSKeyFile: "./sdk.key", TLSCertFile: "./sdk.crt",
    }
    client, err := client.DialContext(context.Background(), config)
    if err != nil { log.Fatalf("❌ 连接失败: %v", err) }
    defer client.Close()
    
    // 查询区块高度
    blockNumber, err := client.GetBlockNumber(context.Background())
    if err != nil { log.Fatalf("❌ 查询失败: %v", err) }
    fmt.Printf("BLOCK_HEIGHT:%d\n", blockNumber)
    
    // 查询最新区块（包含交易哈希）
    if blockNumber > 0 {
        block, err := client.GetBlockByNumber(context.Background(), blockNumber, true, false)
        if err == nil {
            fmt.Printf("LATEST_HASH:%s\n", block.Hash)
            fmt.Printf("LATEST_SEALER:%s\n", block.Sealer)
            fmt.Printf("LATEST_TX_COUNT:%d\n", len(block.Transactions))
            fmt.Printf("LATEST_TIMESTAMP:%d\n", block.Timestamp)
            t := time.Unix(int64(block.Timestamp)/1000, 0)
            fmt.Printf("LATEST_TIMESTAMP_READABLE:%s\n", t.Format("2006-01-02 15:04:05"))
            // 输出交易哈希（如果有）
            for _, txHash := range block.Transactions {
                fmt.Printf("LATEST_TX_HASH:%s\n", txHash)
            }
        }
    }
    
    // 查询历史区块（不包含交易细节，以节省时间）
    start := blockNumber - 4; if start < 0 { start = 0 }
    for i := start; i <= blockNumber; i++ {
        block, err := client.GetBlockByNumber(context.Background(), i, false, false)
        if err == nil {
            fmt.Printf("RECENT_BLOCK:%d|%d|%s\n", i, len(block.Transactions), block.Hash)
        }
    }
    
    // 查询交易统计
    totalTx, err := client.GetTotalTransactionCount(context.Background())
    if err == nil {
        fmt.Printf("TOTAL_TX:%d\n", totalTx.TxSum)
        fmt.Printf("FAILED_TX:%d\n", totalTx.FailedTxSum)
        if totalTx.TxSum > 0 {
            rate := float64(totalTx.TxSum-totalTx.FailedTxSum) / float64(totalTx.TxSum) * 100
            fmt.Printf("SUCCESS_RATE:%.2f%%\n", rate)
        } else {
            fmt.Printf("SUCCESS_RATE:100.00%%\n")
        }
    }
}
GOEOF
    
    # 运行 Go 并捕获结构化输出
    if ! go run temp_detail.go > "$TEMP_DATA" 2>/dev/null; then
        log_error "Go 查询失败"
        rm -f temp_detail.go "$TEMP_DATA"
        return 1
    fi
    rm temp_detail.go
    
    # 提取数据（简单解析）
    CURRENT_HEIGHT=$(grep '^BLOCK_HEIGHT:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    LATEST_HASH=$(grep '^LATEST_HASH:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    BLOCK_CREATOR=$(grep '^LATEST_SEALER:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    TX_COUNT_LATEST=$(grep '^LATEST_TX_COUNT:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    TIMESTAMP_UNIX=$(grep '^LATEST_TIMESTAMP:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    TIMESTAMP_READABLE=$(grep '^LATEST_TIMESTAMP_READABLE:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    TOTAL_TX=$(grep '^TOTAL_TX:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    FAILED_TX=$(grep '^FAILED_TX:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    SUCCESS_RATE=$(grep '^SUCCESS_RATE:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r')
    
    # 提取最新块交易哈希（数组）
    LATEST_TX_HASHES=$(grep '^LATEST_TX_HASH:' "$TEMP_DATA" | cut -d: -f2 | tr -d '\r' | sed 's/^/"/;s/$/"/' | paste -sd, -)
    if [ -z "$LATEST_TX_HASHES" ]; then
        LATEST_TX_HASHES="[]"
    else
        LATEST_TX_HASHES="[$LATEST_TX_HASHES]"
    fi
    
    # 构建 recent_blocks 数组（多行格式）
    declare -a recent_blocks
    while IFS=':' read -r _ line; do
        IFS='|' read -r num tx hash <<< "$line"
        obj=$(printf '    {\n      "block_number": %d,\n      "transaction_count": %d,\n      "block_hash_short": "%s"\n    },' "$num" "$tx" "$hash")
        recent_blocks+=("$obj")
    done < <(grep '^RECENT_BLOCK:' "$TEMP_DATA")
    
    # 组合 recent_blocks 并移除最后一个逗号
    RECENT_BLOCKS=$(printf '%s\n' "${recent_blocks[@]}")
    RECENT_BLOCKS=$(echo "$RECENT_BLOCKS" | sed '$s/,$//')
    
    # 其他固定值
    QUERY_TIMESTAMP="2025-10-27"
    BLOCKCHAIN_STATUS="active"
    CHAIN_TYPE="FISCO BCOS"
    NODE_COUNT=$(ps aux | grep -E "fisco.*node" | grep -v grep | wc -l)
    CONNECTION_STATUS="connected"
    
    # 生成 JSON 文件（精确缩进和换行）
    cat > "$JSON_FILE" << EOF
{
  "query_timestamp": "$QUERY_TIMESTAMP",
  "blockchain_status": "$BLOCKCHAIN_STATUS",
  "current_block_height": $CURRENT_HEIGHT,
  "latest_block": {
    "block_hash": "$LATEST_HASH",
    "block_creator": "$BLOCK_CREATOR",
    "transaction_count": $TX_COUNT_LATEST,
    "timestamp_unix": $TIMESTAMP_UNIX,
    "timestamp_readable": "$TIMESTAMP_READABLE",
    "transactions": $LATEST_TX_HASHES
  },
  "recent_blocks": [
$RECENT_BLOCKS
  ],
  "transaction_statistics": {
    "total_transactions": $TOTAL_TX,
    "failed_transactions": $FAILED_TX,
    "success_rate": "$SUCCESS_RATE"
  },
  "network_info": {
    "chain_type": "$CHAIN_TYPE",
    "node_count": $NODE_COUNT,
    "connection_status": "$CONNECTION_STATUS"
  }
}
EOF
    
    # 打印原终端格式（基于提取数据，模拟原输出，添加交易哈希打印）
    echo "🎉 成功连接到FISCO区块链!"
    echo "========================================"
    echo "🔥 1. 区块链基础信息"
    echo "   📦 当前区块高度: $CURRENT_HEIGHT"
    echo ""
    echo "📦 2. 最新区块详情"
    echo "   🔗 区块哈希: $LATEST_HASH"
    echo "   👨‍💼 区块创建者: $BLOCK_CREATOR"
    echo "   📊 交易数量: $TX_COUNT_LATEST"
    echo "   ⏰ 区块时间戳: $TIMESTAMP_UNIX"
    echo "   📅 区块时间: $TIMESTAMP_READABLE"
    if [ "$TX_COUNT_LATEST" -gt 0 ]; then
        echo "   📝 交易哈希:"
        grep '^LATEST_TX_HASH:' "$TEMP_DATA" | cut -d: -f2 | sed 's/^/     - /'
    fi
    echo ""
    echo "📚 3. 最近区块历史"
    grep '^RECENT_BLOCK:' "$TEMP_DATA" | while IFS=':' read -r _ line; do
        IFS='|' read -r num tx hash <<< "$line"
        echo "   区块 $num: 交易数 $tx, 哈希 $hash"
    done
    echo ""
    echo "📈 4. 交易统计信息"
    echo "   📈 总交易数: $TOTAL_TX"
    echo "   📉 失败交易数: $FAILED_TX"
    echo "   ✅ 交易成功率: $SUCCESS_RATE"
    echo ""
    echo "========================================"
    echo "✅ 区块链查询完成!"
    
    rm "$TEMP_DATA"
    log_success "JSON 文件已生成: $JSON_FILE (包含最新块交易哈希)"
}

# 检查节点状态
check_nodes() {
    log_info "检查FISCO节点状态..."
    local node_count=$(ps aux | grep -E "fisco.*node" | grep -v grep | wc -l)
    if [ $node_count -gt 0 ]; then
        log_success "发现 $node_count 个FISCO节点正在运行"
        echo ""
        ps aux | grep -E "fisco.*node" | grep -v grep | head -4
        echo ""
        netstat -tulnp | grep -E "(30300|30301|30302|30303)"
        return 0
    else
        log_error "未发现FISCO节点"
        return 1
    fi
}

show_help() {
    echo "FISCO区块链单链查询脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示帮助信息"
    echo "  -c, --check    检查节点状态"
    echo "  -q, --quick    快速查询区块高度"
    echo "  -d, --detail   详细查询区块信息 (并生成 JSON)"
    echo ""
    echo "示例:"
    echo "  $0 --quick     # 快速查询区块高度"
    echo "  $0 --detail    # 详细查询区块信息 + 生成 fisco_blockchain_info.json"
}

main() {
    echo "🔍 FISCO区块链单链查询工具"
    echo "============================="
    
    case "${1:-}" in
        -h|--help) show_help; exit 0 ;;
        -c|--check) check_nodes; exit $? ;;
        -q|--quick) quick_query; exit $? ;;
        -d|--detail) detail_query; exit $? ;;
        "") detail_query ;;
        *) log_error "未知选项: $1"; show_help; exit 1 ;;
    esac
}

main "$@"