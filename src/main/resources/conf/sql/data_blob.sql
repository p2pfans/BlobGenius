/*
Navicat MySQL Data Transfer

Source Server         : localhost3309
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_blob

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-05-02 18:23:16
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for data_blob
-- ----------------------------
DROP TABLE IF EXISTS `data_blob`;
CREATE TABLE `data_blob` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `boid` varchar(16) DEFAULT NULL,
  `flags` int(11) DEFAULT '0',
  `size` int(11) DEFAULT '0',
  `copy` int(11) DEFAULT '0',
  `refs` int(11) DEFAULT '0',
  `hash` varchar(72) DEFAULT NULL,
  `download` bigint(20) DEFAULT '0',
  `timeCreate` datetime DEFAULT NULL,
  `lastAccess` datetime DEFAULT NULL,
  `data` mediumblob,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bidIndex` (`boid`) USING HASH,
  KEY `refLastIndex` (`refs`,`lastAccess`) USING BTREE,
  KEY `downIndex` (`download`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
