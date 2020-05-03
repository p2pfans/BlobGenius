/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_conf

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-04-14 18:00:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cache_node
-- ----------------------------
DROP TABLE IF EXISTS `cache_node`;
CREATE TABLE `cache_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `state` int(11) DEFAULT NULL,
  `host` varchar(64) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `dbPass` varchar(36) DEFAULT NULL,
  `dbIndex` int(11) DEFAULT '0',
  `master` bit(1) DEFAULT b'0',
  `distKey` varchar(255) DEFAULT NULL,
  `distRule` varchar(255) DEFAULT NULL,
  `totalVisit` bigint(20) DEFAULT NULL,
  `timeCreate` datetime DEFAULT NULL,
  `lastAccess` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of cache_node
-- ----------------------------
INSERT INTO `cache_node` VALUES ('1', '0', 'localhost:6379', 'users', null, '0', '\0', null, null, '0', '2020-04-14 17:56:56', '2020-04-14 17:56:56');
INSERT INTO `cache_node` VALUES ('2', '0', 'localhost:6379', 'orgs', null, '1', '\0', null, null, '0', '2020-04-14 17:57:13', '2020-04-14 17:57:13');
INSERT INTO `cache_node` VALUES ('3', '0', 'localhost:6379', 'objects', null, '2', '\0', null, null, '0', '2020-04-14 17:57:31', '2020-04-14 17:57:31');
INSERT INTO `cache_node` VALUES ('4', '0', 'localhost:6379', 'resources', null, '3', '\0', null, null, '0', '2020-04-14 17:57:51', '2020-04-14 17:57:51');
