#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
生成 function 表 SQL 插入语句
每条链 13 个合约，每个合约若干方法
共 120 条链 (192.168.0.2~21, 每台6种链)
输出到 function_insert.sql
"""

# 定义链信息
chains = []
for i in range(2, 22):  # 192.168.0.2 ~ 192.168.0.21
    ip = f"192.168.0.{i}"
    chains.append((11000 + i, ip, 8088, "changanchain"))
    chains.append((12000 + i, ip, 8086, "ethereum"))
    chains.append((13000 + i, ip, 8087, "haihechain"))
    chains.append((14000 + i, ip, 8089, "bubi"))
    chains.append((15000 + i, ip, 8090, "fabric"))
    chains.append((16000 + i, ip, 8091, "fisco"))

# 合约与对应方法（函数名 → 参数列表）
contracts_functions = {
    "UniversalKVStore": [
        ("set", ["key", "value"]),
        ("get", ["key"])
    ],
    "DataStorage": [
        ("setData", ["key", "value"]),
        ("getData", ["key"]),
        ("keyExists", ["key"])
    ],
    "PrivateDataStorage": [
        ("setPlainData", ["key", "value"]),
        ("setEncryptedData", ["key", "cipherValue"]),
        ("updateTag", ["key", "newTag"]),
        ("getData", ["key"]),
        ("getDataRecord", ["key"]),
        ("keyExists", ["key"])
    ],
    "DataStorageAccess": [
        ("setData", ["key", "value"]),
        ("getData", ["key"]),
        ("keyExists", ["key"])
    ],
    "PrivateDataStorageAccess": [
        ("setPlainData", ["key", "value"]),
        ("setEncryptedData", ["key", "cipherValue"]),
        ("updateTag", ["key", "newTag"]),
        ("getData", ["key"]),
        ("getDataRecord", ["key"]),
        ("keyExists", ["key"])
    ],
    "DataQuery": [
        ("getValue", ["contractAddress", "key"])
    ],
    "PrivateDataQuery": [
        ("queryEncryptedData", ["storageContract", "key"])
    ],
    "DataQueryAccess": [
        ("getValue", ["contractAddress", "key"])
    ],
    "PrivateDataQueryAccess": [
        ("queryEncryptedData", ["storageContract", "key"])
    ],
    "Calculator": [
        ("calculate", ["a", "b"])
    ],
    "HomomorphicCalculator": [
        ("homomorphicAdd", ["encryptedA", "encryptedB"]),
        ("homomorphicMultiply", ["encryptedA", "scalarK"])
    ],
    "CalculatorAccess": [
        ("calculate", ["a", "b"])
    ],
    "HomomorphicCalculatorAccess": [
        ("homomorphicAdd", ["encryptedA", "encryptedB"]),
        ("homomorphicMultiply", ["encryptedA", "scalarK"])
    ]
}

# 输出 SQL 文件
output_file = "function_insert.sql"
with open(output_file, "w", encoding="utf-8") as f:
    f.write("INSERT INTO `function` (ip_chain, port, contract_name, contract_address, function_name, func_arg_des) VALUES\n")

    entries = []
    for chain_id, ip, port, chain_type in chains:
        for contract_name, funcs in contracts_functions.items():
            for func_name, args in funcs:
                arg_list = [contract_name, func_name] + args
                func_arg_des = str(arg_list).replace("'", '"')  # 转成 ["xx", "xx", ...]
                entry = f"('{ip}', {port}, '{contract_name}', '1', '{func_name}', '{func_arg_des}')"
                entries.append(entry)

    f.write(",\n".join(entries) + ";")

print(f"✅ SQL 文件已生成: {output_file}")
print(f"共 {len(chains)} 条链, 每链 {len(contracts_functions)} 个合约, 共 {len(entries)} 条函数记录。")
