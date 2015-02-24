INSERT INTO `npc` (`id`, `idTemplate`, `name`, `serverSideName`, `title`, `serverSideTitle`, `class`, `collision_radius`, `collision_height`, `level`, `sex`, `type`, `attackrange`, `hp`, `mp`, `hpreg`, `mpreg`, `str`, `con`, `dex`, `int`, `wit`, `men`, `exp`, `sp`, `patk`, `pdef`, `matk`, `mdef`, `atkspd`, `aggro`, `matkspd`, `rhand`, `lhand`, `enchant`, `walkspd`, `runspd`, `dropHerbGroup`,`basestats`) VALUES
(9101, 30026, 'Simon', 1, 'Event', 1, 'NPC.a_fighterguild_master_Mhuman', 8.00, 23.50, 70, 'male', 'L2Npc', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 55, 132, 0, 1),
(9102, 18284, 'Treasure Chest', 0, '', 0, 'LineageMonster.mimic', 8.50, 8.50, 78, 'male', 'L2Npc', 40, 4428, 1784, 13.43, 3.09, 40, 43, 30, 21, 20, 10, 7300, 799, 1715, 555, 1069, 451, 278, 0, 333, 0, 0, 0, 88, 181, 0, 1),
(9103, 31772, 'Zone', 1, '', 0, 'LineageNPC.heroes_obelisk_dwarf', 23.00, 80.00, 70, 'etc', 'L2Npc', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 50, 120, 0, 1),
(9104, 35062, 'Blue Flag', 1, '', 0, 'LineageDeco.flag_a', 21.00, 82.00, 1, 'etc', 'L2Npc', 40, 158000, 989, 3.16, 0.91, 40, 43, 30, 21, 20, 10, 0, 0, 652, 753, 358, 295, 423, 0, 333, 0, 0, 0, 55, 132, 0, 1),
(9105, 35062, 'Red Flag', 1, '', 0, 'LineageDeco.flag_a', 21.00, 82.00, 1, 'etc', 'L2Npc', 40, 158000, 989, 3.16, 0.91, 40, 43, 30, 21, 20, 10, 0, 0, 652, 753, 358, 295, 423, 0, 333, 0, 0, 0, 55, 132, 0, 1),
(9106, 32027, 'Blue Flag Holder', 1, '', 0, 'LineageNpcEV.grail_brazier_b', 30.00, 31.00, 70, 'male', 'L2Npc', 40, 3862, 1494, 0.00, 0.00, 40, 43, 30, 21, 20, 20, 0, 0, 1303, 471, 607, 382, 253, 0, 333, 0, 0, 0, 88, 132, 0, 1),
(9107, 32027, 'Red Flag Holder', 1, '', 0, 'LineageNpcEV.grail_brazier_b', 30.00, 31.00, 70, 'male', 'L2Npc', 40, 3862, 1494, 0.00, 0.00, 40, 43, 30, 21, 20, 20, 0, 0, 1303, 471, 607, 382, 253, 0, 333, 0, 0, 0, 88, 132, 0, 1),
(9108, 18284, 'Russian', 1, '', 0, 'LineageMonster.mimic', 8.50, 8.50, 78, 'male', 'L2Npc', 40, 4428, 1784, 13.43, 3.09, 40, 43, 30, 21, 20, 10, 7300, 799, 1715, 555, 1069, 451, 278, 0, 333, 0, 0, 0, 88, 181, 0, 1),
(9109, 18284, 'Bomb', 1, '', 0, 'LineageMonster.mimic', 8.50, 8.50, 78, 'male', 'L2Npc', 40, 4428, 1784, 13.43, 3.09, 40, 43, 30, 21, 20, 10, 7300, 799, 1715, 555, 1069, 451, 278, 0, 333, 0, 0, 0, 88, 181, 0, 1),
(9110, 35062, 'Base', 1, '', 0, 'LineageDeco.flag_a', 21.00, 82.00, 1, 'etc', 'L2Npc', 40, 158000, 989, 3.16, 0.91, 40, 43, 30, 21, 20, 10, 0, 0, 652, 753, 358, 295, 423, 0, 333, 0, 0, 0, 55, 132, 0, 1),
(9999, 30026, 'Phoenix', 1, 'Event Manager', 1, 'NPC.a_fighterguild_master_Mhuman', 8.00, 23.50, 70, 'male', 'L2Npc', 40, 3862, 1493, 11.85, 2.78, 40, 43, 30, 21, 20, 10, 0, 0, 1314, 470, 780, 382, 278, 0, 333, 0, 0, 0, 55, 132, 0, 1);

CREATE TABLE IF NOT EXISTS `event_buffs` (
  `player` varchar(30) NOT NULL,
  `buffs` varchar(200) NOT NULL,
  PRIMARY KEY (`player`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
