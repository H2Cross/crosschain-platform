SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cross
-- ----------------------------
DROP TABLE IF EXISTS `contract`;
CREATE TABLE `contract` (
    `contract_id` BIGINT(100) PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `ip_chain` varchar(100) NOT NULL,
    `port` bigint(100) NOT NULL,
    `chain_id` bigint(100) NOT NULL,
    `contract_name` varchar(100) NOT NULL,
    `contract_address` varchar(100) NOT NULL

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
