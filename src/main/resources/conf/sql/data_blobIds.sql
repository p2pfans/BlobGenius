/*
Navicat MySQL Data Transfer

Source Server         : localhost3309
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_oids

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-05-01 22:57:09
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for data_blobids
-- ----------------------------
DROP TABLE IF EXISTS `data_blobids`;
CREATE TABLE `data_blobids` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `doid` varchar(16) NOT NULL,
  `boid` varchar(16) NOT NULL,
  `serial` int(11) DEFAULT '0',
  `offset` int(11) DEFAULT '0',
  `hash` varchar(72) DEFAULT NULL,
  `timeCreate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `boidIndex` (`boid`) USING BTREE,
  KEY `doidIndex` (`doid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;
