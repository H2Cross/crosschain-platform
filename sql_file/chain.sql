SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ox
-- ----------------------------
DROP TABLE IF EXISTS `chain_info`;
CREATE TABLE `chain_info` (
    `chain_id`      bigint(200) NOT NULL,
    `ip_address`    VARCHAR(255) NOT NULL,
    `port`          bigint(200) NOT NULL,
    `chain_type`    VARCHAR(255) NOT NULL,


    PRIMARY KEY (`chain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链信息表';