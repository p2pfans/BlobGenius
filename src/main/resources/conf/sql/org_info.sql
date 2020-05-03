/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_conf

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-04-12 22:49:59
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for org_info
-- ----------------------------
DROP TABLE IF EXISTS `org_info`;
CREATE TABLE `org_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `state` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT '0',
  `oid` varchar(36) DEFAULT NULL,
  `pid` varchar(36) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `abbr` varchar(64) DEFAULT NULL,
  `contact` varchar(64) DEFAULT NULL,
  `title` varchar(64) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `phone` varchar(64) DEFAULT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `uidAdmin` varchar(36) DEFAULT NULL,
  `maxCount` bigint(20) DEFAULT '0',
  `maxSpace` bigint(20) DEFAULT '0',
  `ipAddr` varchar(20) DEFAULT NULL,
  `uidCreate` varchar(36) DEFAULT NULL,
  `timeCreate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `oidIndex` (`oid`) USING HASH,
  KEY `pidIndex` (`pid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
