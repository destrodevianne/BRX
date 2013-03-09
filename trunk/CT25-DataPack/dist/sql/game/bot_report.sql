-- -------------------------------------
-- Table structure for bot_report
-- --------------------------------------
DROP TABLE IF EXISTS `bot_report`;
CREATE TABLE `bot_report` (
  `report_id` int(10) NOT NULL auto_increment,
  `reported_name` varchar(45) default NULL,
  `reported_objectId` int(10) default NULL,
  `reporter_name` varchar(45) default NULL,
  `reporter_objectId` int(10) default NULL,
  `date` decimal(20,0) NOT NULL default '0',
  `read` enum('true','false') NOT NULL default 'false',
  PRIMARY KEY  (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;