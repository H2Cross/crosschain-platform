/*
Navicat MySQL Data Transfer

Source Server         : mysql
Source Server Version : 80033
Source Host           : localhost:3306
Source Database       : crosschain_database

Target Server Type    : MYSQL
Target Server Version : 80033
File Encoding         : 65001

Date: 2025-08-13 15:56:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for contract
-- ----------------------------
DROP TABLE IF EXISTS `contract`;
CREATE TABLE `contract` (
  `contract_id` bigint NOT NULL AUTO_INCREMENT,
  `ip_chain` varchar(100) NOT NULL,
  `port` bigint NOT NULL,
  `chain_id` bigint NOT NULL,
  `contract_name` varchar(100) NOT NULL,
  `contract_address` varchar(100) NOT NULL,
  PRIMARY KEY (`contract_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of contract
-- ----------------------------
INSERT INTO `contract` VALUES ('1', '192.168.0.1', '8086', '12001', 'TokenContract', '0x7a16fF0Df3F8e8eB62c8C5a1727b6F506172839');
INSERT INTO `contract` VALUES ('2', '192.168.0.1', '8087', '13001', 'SwapContract', '0x6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c');
INSERT INTO `contract` VALUES ('3', '192.168.0.1', '8088', '11001', 'BridgeContract', '0x5e7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6');
INSERT INTO `contract` VALUES ('4', '192.168.0.1', '8089', '14001', 'NFTContract', '0x4d68fa09e1f7d5c4b3a2e1f0a9b8c7d6e5f4a3b2c');
INSERT INTO `contract` VALUES ('5', '192.168.0.1', '8090', '15001', 'OracleContract', '0x3c57e9f8d0e7c6b5a4d3e2f1a0b9c8d7e6f5a4b3c');
INSERT INTO `contract` VALUES ('6', '192.168.0.1', '8091', '16001', 'GovernanceContract', '0x2b46f8d7e9c6a5b4c3d2e1f0a9b8c7d6e5f4a3b2c');

-- ----------------------------
-- Table structure for crosschain
-- ----------------------------
DROP TABLE IF EXISTS `crosschain`;
CREATE TABLE `crosschain` (
  `tx_id` bigint NOT NULL AUTO_INCREMENT,
  `tx_hash` varchar(100) NOT NULL,
  `src_ip` varchar(100) NOT NULL,
  `src_port` bigint NOT NULL,
  `dst_ip` varchar(100) NOT NULL,
  `dst_port` bigint NOT NULL,
  `src_chain_type` varchar(100) NOT NULL,
  `dst_chain_type` varchar(100) NOT NULL,
  `src_hash` varchar(100) NOT NULL,
  `dst_hash` varchar(100) NOT NULL,
  `response_hash` varchar(100) NOT NULL,
  `tx_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`tx_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨链信息表';

-- ----------------------------
-- Records of crosschain
-- ----------------------------
INSERT INTO `crosschain` VALUES ('21', '7b3c09a47df76ff4038f5e0eeadf2c297523be60e1297fe95ff49f2ebb6e93c7', '192.168.0.1', '8086', '192.168.0.2', '8087', '以太坊', '海河智链', '1d5e8f7a9c3b2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9', '2e6f9a8b7c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d', '3f7a0b9c8d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('22', '4a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f', '192.168.0.3', '8087', '192.168.0.1', '8088', '海河智链', '长安链', '5b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f', '6c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b', '7d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('23', '8e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d', '192.168.0.2', '8088', '192.168.0.3', '8089', '长安链', '布比链', '9f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d', 'a08b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e', 'b19c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('24', 'c2ad9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b', '192.168.0.1', '8089', '192.168.0.2', '8090', '布比链', 'Fabric', 'd3beaf9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b', 'e4cfbfac9d8e7f6e5d4c3b2a1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a', 'f5d0c0bdae9f8e7d6c5b4a39281d0e9f8a7b6c5d4e3f2a1b0c9d8e7f6e5d', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('25', '06e1d1c2b3a495867768594a3b2c1d0e9f8a7b6c5d4e3f2a1b0c9d8e7f6e', '192.168.0.3', '8090', '192.168.0.1', '8091', 'Fabric', '金链盟', '17f2e2d3c4b5a69788796a5b4c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b', '2803f3e4d5c6b7a8998a7b6c5d4e3f2a1b0c9d8e7f6e5d4c3b2a1c0d9e8f', '391404f5e6d7c8b9a09b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0c9d', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('26', '4a251506f7e8d9c0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d', '192.168.0.2', '8091', '192.168.0.3', '8086', '金链盟', '以太坊', '5b36261708f9eac1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e', '6c473728190afbd2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f', '7d5848392a1b0ce3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('27', '8e69594a3b2c1d0f4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b', '192.168.0.1', '8086', '192.168.0.3', '8089', '以太坊', '布比链', '9f7a6a5b4c3d2e1g5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c', 'a08b7b6c5d4e3f2h6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d', 'b19c8c7d6e5f4a3i7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('28', 'c2ad9d8e7f6e5d4j8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f', '192.168.0.2', '8087', '192.168.0.1', '8090', '海河智链', 'Fabric', 'd3beae9f8e7d6c5k9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0', 'e4cfbfac9d8e7f6l0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1', 'f5d0c0bdae9f8e7m1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('29', '06e1d1c2b3a4958n2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3', '192.168.0.3', '8088', '192.168.0.2', '8091', '长安链', '金链盟', '17f2e2d3c4b5a69o3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3e4', '2803f3e4d5c6b7ap4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3e4f5', '391404f5e6d7c8bq5d6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3e4f506', '2025-07-06 17:26:55');
INSERT INTO `crosschain` VALUES ('30', '4a251506f7e8d9cr6e7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3e4f50617', '192.168.0.1', '8089', '192.168.0.3', '8086', '布比链', '以太坊', '5b36261708f9eas7f8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3e4f5061728', '6c473728190afbt8a9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3e4f506172839', '7d5848392a1b0ce9b0c1d2e3f4a5b6c7d8e9fa0b1c2d3e4f5061728394a', '2025-07-06 17:26:55');

-- ----------------------------
-- Table structure for function
-- ----------------------------
DROP TABLE IF EXISTS `function`;
CREATE TABLE `function` (
  `fun_id` bigint NOT NULL AUTO_INCREMENT,
  `ip_chain` varchar(100) NOT NULL,
  `port` bigint NOT NULL,
  `contract_name` varchar(100) NOT NULL,
  `contract_address` varchar(100) NOT NULL,
  `function_name` varchar(100) NOT NULL,
  `func_arg_des` varchar(100) NOT NULL,
  PRIMARY KEY (`fun_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of function
-- ----------------------------
INSERT INTO `function` VALUES ('1', '192.168.0.1', '8086', 'TokenContract', '0x7a16fF0Df3F8e8eB62c8C5a1727b6F506172839', 'add', '(int a, int b) 两整数相加');
INSERT INTO `function` VALUES ('2', '192.168.0.1', '8086', 'TokenContract', '0x7a16fF0Df3F8e8eB62c8C5a1727b6F506172839', 'sub', '(int a, int b) 两整数相减');
INSERT INTO `function` VALUES ('3', '192.168.0.1', '8087', 'SwapContract', '0x6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c', 'mul', '(int a, int b) 两整数相乘');
INSERT INTO `function` VALUES ('4', '192.168.0.1', '8087', 'SwapContract', '0x6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c', 'div', '(int a, int b) 两整数相除');
INSERT INTO `function` VALUES ('5', '192.168.0.1', '8088', 'BridgeContract', '0x5e7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6', 'add', '(int a, int b) 两整数相加');
INSERT INTO `function` VALUES ('6', '192.168.0.1', '8088', 'BridgeContract', '0x5e7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6', 'sub', '(int a, int b) 两整数相减');
INSERT INTO `function` VALUES ('7', '192.168.0.1', '8089', 'NFTContract', '0x4d68fa09e1f7d5c4b3a2e1f0a9b8c7d6e5f4a3b2c', 'mul', '(int a, int b) 两整数相乘');
INSERT INTO `function` VALUES ('8', '192.168.0.1', '8089', 'NFTContract', '0x4d68fa09e1f7d5c4b3a2e1f0a9b8c7d6e5f4a3b2c', 'div', '(int a, int b) 两整数相除');
INSERT INTO `function` VALUES ('9', '192.168.0.1', '8090', 'OracleContract', '0x3c57e9f8d0e7c6b5a4d3e2f1a0b9c8d7e6f5a4b3c', 'add', '(int a, int b) 两整数相加');
INSERT INTO `function` VALUES ('10', '192.168.0.1', '8090', 'OracleContract', '0x3c57e9f8d0e7c6b5a4d3e2f1a0b9c8d7e6f5a4b3c', 'sub', '(int a, int b) 两整数相减');
INSERT INTO `function` VALUES ('11', '192.168.0.1', '8091', 'GovernanceContract', '0x2b46f8d7e9c6a5b4c3d2e1f0a9b8c7d6e5f4a3b2c', 'mul', '(int a, int b) 两整数相乘');
INSERT INTO `function` VALUES ('12', '192.168.0.1', '8091', 'GovernanceContract', '0x2b46f8d7e9c6a5b4c3d2e1f0a9b8c7d6e5f4a3b2c', 'div', '(int a, int b) 两整数相除');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一注册序号',
  `user_mobile` varchar(100) NOT NULL COMMENT '手机号',
  `user_email` varchar(100) NOT NULL COMMENT '邮箱',
  `user_pswd` varchar(100) NOT NULL COMMENT '密码',
  `nick_name` varchar(100) NOT NULL COMMENT '昵称',
  `link_url` varchar(100) NOT NULL COMMENT '所在链',
  `register_date` datetime NOT NULL COMMENT '注册时间',
  `token` varchar(255) DEFAULT NULL COMMENT '用户登陆token',
  `authority` tinyint(1) NOT NULL COMMENT '用户权限，0为管理员，1为用户',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户个人信息表';

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', '18676449495', '506980247@qq.com', '123456789a?', '新用户1', '192.168.0.1:1010', '2025-07-06 16:00:02', 'e2dfe56d6bad1664', '1');
INSERT INTO `user` VALUES ('2', '18007923506', '1978750368@qq.com', '123456789a.', '新用户', '192.168.0.1:1010', '2025-07-07 01:25:02', 'a831b9682eca9b09', '1');
