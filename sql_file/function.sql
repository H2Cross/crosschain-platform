SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cross
-- ----------------------------
DROP TABLE IF EXISTS `function`;
CREATE TABLE `function` (
    `fun_id` BIGINT(100) PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `ip_chain` varchar(100) NOT NULL,
    `port` bigint(100) NOT NULL,
    `contract_name` varchar(100) NOT NULL,
    `contract_address` varchar(100) NOT NULL,
    `function_name` varchar(100) NOT NULL,
    `func_arg_des` varchar(100) NOT NULL

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
