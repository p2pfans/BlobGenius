/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50717
Source Host           : localhost:3309
Source Database       : data_conf

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2020-04-21 10:35:07
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for data_node
-- ----------------------------
DROP TABLE IF EXISTS `data_node`;
CREATE TABLE `data_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hid` varchar(36) DEFAULT NULL,
  `state` int(11) unsigned zerofill DEFAULT NULL,
  `host` varchar(64) DEFAULT NULL,
  `hostid` varchar(64) DEFAULT NULL,
  `protocol` varchar(16) DEFAULT 'jdbc',
  `dataType` varchar(128) DEFAULT NULL,
  `rid` varchar(36) DEFAULT NULL,
  `dbType` varchar(64) DEFAULT NULL,
  `distKey` varchar(64) DEFAULT NULL,
  `master` bit(1) DEFAULT NULL,
  `useUnicode` bit(1) DEFAULT NULL,
  `dbUser` varchar(64) DEFAULT NULL,
  `dbPass` varchar(64) DEFAULT NULL,
  `filePath` varchar(255) DEFAULT NULL,
  `fileHost` varchar(64) DEFAULT NULL,
  `timeZone` varchar(64) DEFAULT NULL,
  `driverClass` varchar(255) DEFAULT NULL,
  `totalVisit` bigint(20) DEFAULT '0',
  `tmCreate` datetime DEFAULT NULL,
  `lastAlive` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of data_node
-- ----------------------------
INSERT INTO `data_node` VALUES ('1', 'hidf232ce6ce21a7229c028090aabd73966', '00000000000', 'localhost:3309', 'localhost', 'jdbc', 'objects', 'Ky4bJyO3', 'mysql', 'all', '\0', '', 'root', 'jsmart+1', null, null, 'Asia/Shanghai', 'com.mysql.cj.jdbc.Driver', '0', '2020-04-02 10:15:54', '2020-04-02 10:15:54');
INSERT INTO `data_node` VALUES ('2', 'hidc10644ce6b315f366316630aafc08cc6', '00000000000', 'localhost:3309', 'localhost', 'jdbc', 'blobs', 'Ky4bJyO3', 'mysql', 'all', '\0', '', 'root', 'jsmart+1', 'g:\\data', '127.0.0.1:35168', 'Asia/Shanghai', 'com.mysql.cj.jdbc.Driver', '0', '2020-04-02 20:39:37', '2020-04-02 20:39:37');
INSERT INTO `data_node` VALUES ('3', 'hid4eabfc362e02e806c6586d38b2435773', '00000000000', 'localhost:3309', 'localhost', 'jdbc', 'blobIds', 'Ky4bJyO3', 'mysql', 'all', '\0', '', 'root', 'jsmart+1', null, null, 'Asia/Shanghai', 'com.mysql.cj.jdbc.Driver', '0', '2020-04-02 20:39:54', '2020-04-02 20:39:54');
INSERT INTO `data_node` VALUES ('4', 'hid4eabfc362e02e806c6586d38b2435773', '00000000000', 'localhost:3306', 'localhost', 'jdbc', 'blobs', 'Ky4bJyO3', 'mysql', 'all', '\0', '\0', 'root', 'qv7OCj2r0tv', null, null, 'Asia/Shanghai', 'com.mysql.cj.jdbc.Driver', '0', '2020-04-02 20:39:54', '2020-04-02 20:39:54');
