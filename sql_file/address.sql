SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for beef
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE address (
    `address_ip` BIGINT(200) PRIMARY KEY AUTO_INCREMENT NOT NULL,
    `walletKey` VARCHAR(255) NOT NULL,
    `user_id` VARCHAR(255) NOT NULL

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';
