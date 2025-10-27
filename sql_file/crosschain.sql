SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cross
-- ----------------------------
DROP TABLE IF EXISTS `crosschain`;
CREATE TABLE `crosschain` (
    `tx_id` BIGINT(100) PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `tx_hash` varchar(100) NOT NULL,
    `src_ip` varchar(100) NOT NULL,
    `src_port` bigint(100) NOT NULL,
    `dst_ip` varchar(100) NOT NULL,
    `dst_port` bigint(100) NOT NULL,
    `src_chain_type` varchar(100) NOT NULL,
    `dst_chain_type` varchar(100) NOT NULL,
    `src_hash` varchar(100) NOT NULL,
    `dst_hash` varchar(100) NOT NULL,
    `response_hash` varchar(100) NOT NULL,
    `tx_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
--    `cross_from` BIGINT(255) NOT NULL,
--    `cross_to` BIGINT(255) NOT NULL,
--    `cross_param` BIGINT(255) NOT NULL COMMENT'参数',
--
--    `cross_type` tinyint(1) NOT NULL COMMENT'跨链类型',
--    `cross_result` tinyint(1) NOT NULL COMMENT'交易结果',
--    `cross_time` DATETIME NOT NULL COMMENT'交易时间'

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跨链信息表';
