#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# 生成 contract 表的 SQL 插入语句
# 每条链部署 13 个合约
# 共 120 条链 (192.168.0.2 ~ 192.168.0.21, 每台6条链)
# 输出到 contract_insert.sql 文件

chains = []
for i in range(2, 22):  # 192.168.0.2 ~ 192.168.0.21
    ip = f"192.168.0.{i}"
    chains.append((11000 + i, ip, 8088, "chainmaker"))
    chains.append((12000 + i, ip, 8086, "ethereum"))
    chains.append((13000 + i, ip, 8087, "h2chain"))
    chains.append((14000 + i, ip, 8089, "bubi"))
    chains.append((15000 + i, ip, 8090, "fabric"))
    chains.append((16000 + i, ip, 8091, "fisco"))

contracts = [
    "UniversalKVStore",
    "DataStorage",
    "PrivateDataStorage",
    "DataStorageAccess",
    "PrivateDataStorageAccess",
    "DataQuery",
    "PrivateDataQuery",
    "DataQueryAccess",
    "PrivateDataQueryAccess",
    "Calculator",
    "HomomorphicCalculator",
    "CalculatorAccess",
    "HomomorphicCalculatorAccess"
]

output_file = "contract_insert.sql"
with open(output_file, "w", encoding="utf-8") as f:
    f.write("INSERT INTO contract (ip_chain, port, chain_id, contract_name, contract_address) VALUES\n")

    entries = []
    for chain_id, ip, port, chain_type in chains:
        for cname in contracts:
            entry = f"('{ip}', {port}, {chain_id}, '{cname}', '1')"
            entries.append(entry)

    # 合并成多行 SQL，一次性插入
    sql = ",\n".join(entries) + ";"
    f.write(sql)

print(f"✅ SQL 文件已生成: {output_file}")
print(f"共 {len(chains)} 条链, {len(contracts)} 个合约/链, 共 {len(chains) * len(contracts)} 条记录。")
