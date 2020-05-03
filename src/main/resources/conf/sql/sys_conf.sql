/*
Navicat MySQL Data Transfer

Source Server         : localhost3309
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_conf

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-05-03 10:02:46
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for sys_conf
-- ----------------------------
DROP TABLE IF EXISTS `sys_conf`;
CREATE TABLE `sys_conf` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `value` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nameIndex` (`name`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of sys_conf
-- ----------------------------
INSERT INTO `sys_conf` VALUES ('2', 'secretKey', 'string', '476dfc8e6b05a89cc4c6fc4b2073b1fa');
INSERT INTO `sys_conf` VALUES ('3', 'es.server', 'string', 'http://localhost:9200');
INSERT INTO `sys_conf` VALUES ('4', 'es.collection', 'string', 'data_objects_v2');
