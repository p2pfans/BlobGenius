/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_conf

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-04-12 22:49:08
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for data_resource
-- ----------------------------
DROP TABLE IF EXISTS `data_resource`;
CREATE TABLE `data_resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `state` int(11) DEFAULT NULL,
  `rid` varchar(36) DEFAULT NULL,
  `oid` varchar(36) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `tags` varchar(255) DEFAULT NULL,
  `brief` mediumtext,
  `maxCount` bigint(20) DEFAULT '0',
  `maxSpace` bigint(20) DEFAULT '0',
  `totalVisit` bigint(20) DEFAULT '0',
  `totalCount` bigint(20) DEFAULT '0',
  `totalSpace` bigint(20) DEFAULT '0',
  `ipAddr` varchar(20) DEFAULT NULL,
  `uidCreate` varchar(36) DEFAULT NULL,
  `timeCreate` datetime DEFAULT NULL,
  `lastAccess` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rid` (`rid`) USING HASH,
  KEY `codeName` (`code`,`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
