/*
Navicat MySQL Data Transfer

Source Server         : localhost3309
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_conf

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-04-27 20:45:42
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for data_rules
-- ----------------------------
DROP TABLE IF EXISTS `data_rules`;
CREATE TABLE `data_rules` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rid` varchar(16) DEFAULT NULL,
  `oid` varchar(16) DEFAULT NULL,
  `resource` varchar(128) DEFAULT NULL,
  `dataType` varchar(64) DEFAULT NULL,
  `dbName` varchar(255) DEFAULT NULL,
  `tblName` varchar(255) DEFAULT NULL,
  `ruleHost` varchar(255) DEFAULT NULL,
  `ruleTable` varchar(255) DEFAULT NULL,
  `maxCopy` int(11) DEFAULT '0',
  `tblTag` varchar(64) DEFAULT NULL,
  `dbSql` mediumtext,
  `tblSql` mediumtext,
  PRIMARY KEY (`id`),
  KEY `resTypeIndex` (`resource`,`dataType`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of data_rules
-- ----------------------------
INSERT INTO `data_rules` VALUES ('2', 'Ky4bJyO3', 'DUrI9FMi', 'default', 'blobIds', 'data_oids', 'data_blobIds', 'doid=substr:3-2', 'doid=substr:3-2', '0', '{tableName}', 'create database if not exists data_oids', 'CREATE TABLE IF NOT EXISTS `{tableName}` (\r\n  `id` int(11) NOT NULL AUTO_INCREMENT,\r\n  `fid` varchar(36) DEFAULT NULL,\r\n  `blobCount` int(11) DEFAULT NULL,\r\n  `blobIds` mediumtext,\r\n  PRIMARY KEY (`id`),\r\n  UNIQUE KEY `fidIndex` (`fid`) USING HASH\r\n) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;');
INSERT INTO `data_rules` VALUES ('3', 'Ky4bJyO3', 'DUrI9FMi', 'default', 'blobs', 'data_blob', 'data_blob', 'doid=substr:3-2', 'doid=substr:3-2', '0', '{tableName}', 'create database if not exists data_blob', 'CREATE TABLE IF NOT EXISTS `{tableName}` (\r\n  `id` int(11) NOT NULL AUTO_INCREMENT,\r\n  `bid` varchar(36) DEFAULT NULL,\r\n  `size` int(11) DEFAULT \'0\',\r\n  `refs` int(11) DEFAULT \'0\',\r\n  `flags` int(11) DEFAULT \'0\',\r\n  `downTimes` bigint(20) DEFAULT \'0\',\r\n  `lastAccess` date DEFAULT NULL,\r\n  `data` mediumblob NOT NULL,\r\n  PRIMARY KEY (`id`),\r\n  UNIQUE KEY `bidIndex` (`bid`) USING HASH,\r\n  KEY `lastAccessIndex` (`lastAccess`) USING BTREE\r\n) ENGINE=InnoDB AUTO_INCREMENT=993 DEFAULT CHARSET=utf8mb4;');
INSERT INTO `data_rules` VALUES ('4', 'Ky4bJyO3', 'DUrI9FMi', 'default', 'objects', 'data_object', 'data_object', 'doid=substr:3-2', 'doid=substr:3-2', '0', '{tableName}', 'create database if not exists data_object', 'CREATE TABLE IF NOT EXISTS `{tableName}` (\r\n  `id` int(11) NOT NULL AUTO_INCREMENT,\r\n  `state` int(11) DEFAULT \'0\',\r\n  `flags` int(11) DEFAULT NULL,\r\n  `fid` varchar(36) DEFAULT NULL,\r\n  `oid` varchar(36) DEFAULT NULL,\r\n  `pid` varchar(36) DEFAULT NULL,\r\n  `rid` varchar(36) DEFAULT NULL,\r\n  `name` varchar(512) DEFAULT NULL,\r\n  `size` int(11) DEFAULT NULL,\r\n  `md5` varchar(36) DEFAULT NULL,\r\n  `blobSize` int(11) DEFAULT NULL,\r\n  `mimeType` varchar(64) DEFAULT NULL,\r\n  `download` bigint(20) DEFAULT NULL,\r\n  `ipAddr` varchar(20) DEFAULT NULL,\r\n  `tmUpload` datetime DEFAULT NULL,\r\n  `lastAccess` datetime DEFAULT NULL,\r\n  PRIMARY KEY (`id`),\r\n  UNIQUE KEY `fidIndex` (`fid`) USING HASH,\r\n  KEY `oidIndex` (`oid`) USING BTREE,\r\n  KEY `pidIndex` (`pid`) USING BTREE,\r\n  KEY `ridIndex` (`rid`) USING BTREE\r\n) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;');
