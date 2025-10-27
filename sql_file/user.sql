SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for user
-- new from zzy
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `user_id` bigint(200) NOT NULL AUTO_INCREMENT COMMENT '用户唯一注册序号',
    `user_mobile` varchar(100) NOT NULL COMMENT '手机号',
    `user_email` varchar(100) NOT NULL COMMENT '邮箱',
    `user_pswd` varchar(100) NOT NULL COMMENT '密码',
    `nick_name` varchar(100) NOT NULL COMMENT '昵称',
    `link_url` varchar(100) NOT NULL COMMENT '所在链',
    `register_date` DATETIME NOT NULL COMMENT '注册时间',
    `token` varchar(255) COMMENT '用户登陆token',
    `authority` tinyint(1) NOT NULL COMMENT'用户权限，0为管理员，1为用户',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个人信息表';