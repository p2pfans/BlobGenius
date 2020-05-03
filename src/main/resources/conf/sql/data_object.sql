/*
Navicat MySQL Data Transfer

Source Server         : localhost3309
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_object

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-05-02 11:42:48
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for data_object
-- ----------------------------
DROP TABLE IF EXISTS `data_object`;
CREATE TABLE `data_object` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(16) DEFAULT NULL,
  `doid` varchar(16) DEFAULT NULL,
  `state` smallint(6) DEFAULT '0',
  `flags` smallint(6) DEFAULT '0',
  `oid` varchar(16) DEFAULT NULL,
  `pid` varchar(16) DEFAULT NULL,
  `rid` varchar(16) DEFAULT NULL,
  `uid` varchar(16) DEFAULT NULL,
  `name` varchar(512) DEFAULT NULL,
  `path` varchar(512) DEFAULT NULL,
  `size` int(11) DEFAULT '0',
  `hash` varchar(72) DEFAULT NULL,
  `blobSize` int(11) DEFAULT '1048576',
  `mimeType` varchar(128) DEFAULT NULL,
  `tag` varchar(24) DEFAULT NULL,
  `version` int(11) DEFAULT '0',
  `download` bigint(20) DEFAULT '0',
  `ipAddr` varchar(20) DEFAULT NULL,
  `timeCreate` datetime DEFAULT NULL,
  `lastAccess` datetime DEFAULT NULL,
  `lastModify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuidIndex` (`uuid`) USING HASH,
  KEY `doidIndex` (`doid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;
