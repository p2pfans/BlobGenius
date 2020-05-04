/*
Navicat MySQL Data Transfer

Source Server         : localhost3309
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_conf

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-05-04 23:11:02
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for name_node
-- ----------------------------
DROP TABLE IF EXISTS `name_node`;
CREATE TABLE `name_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hid` varchar(16) DEFAULT NULL,
  `state` smallint(6) DEFAULT '0',
  `host` varchar(64) DEFAULT NULL,
  `master` bit(1) DEFAULT b'0',
  `resid` varchar(36) DEFAULT NULL,
  `dbName` varchar(64) DEFAULT NULL,
  `timeCreate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of name_node
-- ----------------------------
INSERT INTO `name_node` VALUES ('1', '4wTIqfMf', '0', 'http://localhost:9200', '\0', 'Ky4bJyO3', 'data_objects_v2', '2020-05-04 23:10:39');
