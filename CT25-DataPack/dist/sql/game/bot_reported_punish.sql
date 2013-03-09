-- -------------------------------------
-- Table structure for bot_reported_punish
-- --------------------------------------
DROP TABLE IF EXISTS `bot_reported_punish`;
CREATE TABLE `bot_reported_punish` (
  `charId` int(11) NOT NULL default '0',
  `punish_type` varchar(45) default NULL,
  `time_left` bigint(20) default NULL,
  PRIMARY KEY  (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;