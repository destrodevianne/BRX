CREATE TABLE IF NOT EXISTS `raidboss_spawnlist` (
  `boss_id` int NOT NULL default '0',
  `amount` int NOT NULL default '0',
  `loc_x` int NOT NULL default '0',
  `loc_y` int NOT NULL default '0',
  `loc_z` int NOT NULL default '0',
  `heading` int NOT NULL default '0',
  `respawn_min_delay` int(11) NOT NULL default '43200',
  `respawn_max_delay` int(11) NOT NULL default '129600',
  `respawn_time` bigint(13) unsigned NOT NULL DEFAULT '0',
  `currentHp` decimal(8,0) default NULL,
  `currentMp` decimal(8,0) default NULL,
  PRIMARY KEY (`boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `raidboss_spawnlist` VALUES
(25001,1,-54416,146480,-2887,0,43200,129600,0,95986,514), -- Greyclaw Kutus (23)
(25004,1,-94208,100240,-3520,0,43200,129600,0,168366,731), -- Turek Mercenary Captain (30)
(25007,1,124240,75376,-2800,0,43200,129600,0,331522,1178), -- Retreat Spider Cletu (42)
(25010,1,113920,52960,-3735,0,43200,129600,0,624464,1975), -- Furious Thieles (55)
(25013,1,169744,11920,-2732,0,43200,129600,0,507285,1660), -- Ghost Of Peasant Leader (50)
(25016,1,76787,245775,-10376,0,43200,129600,0,730956,2301), -- The 3rd Underwater Guardian (60)
(25019,1,7376,169376,-3600,0,43200,129600,0,206185,575), -- Pan Dryad (25)
(25020,1,90384,125568,-2128,0,43200,129600,0,156584,860), -- Breka Warlock Pastu (34)
(25023,1,27280,101744,-3696,0,43200,129600,0,208019,860), -- Stakato Queen Zyrnna (34)
(25026,1,92976,7920,-3914,0,43200,129600,0,352421,1598), -- Katu Van Leader Atui (49)
(25029,1,54941,206705,-3728,0,43200,129600,0,578190,1847), -- Atraiban (53)
(25032,1,88532,245798,-10376,0,43200,129600,0,690320,2169), -- Eva's Guardian Millenu (58)
(25035,1,180968,12035,-2720,0,43200,129600,0,888658,2987), -- Shilen's Messenger Cabrio (70)
(25038,1,-57360,186272,-4967,0,43200,129600,0,116581,668), -- Tirak (28)
(25041,1,10416,126880,-3676,0,43200,129600,0,165289,893), -- Remmel (35)
(25044,1,107792,27728,-3488,0,43200,129600,0,319791,1476), -- Barion (47)
(25047,1,116352,27648,-3319,0,43200,129600,0,352421,1598), -- Karte (49)
(25050,1,125520,27216,-3632,0,43200,129600,0,771340,1722), -- Verfa (51)
(25051,1,117760,-9072,-3264,0,43200,129600,0,818959,2639), -- Rahha (65)
(25054,1,113432,16403,3960,0,43200,129600,0,945900,3347), -- Kernon (75)
(25057,1,107056,168176,-3456,0,43200,129600,0,288415,1355), -- Biconne Of Blue Sky (45)
(25060,1,-60428,188264,-4512,0,43200,129600,0,99367,545), -- Unrequited Kael (24)
(25063,1,-91024,116304,-3466,0,43200,129600,0,330579,893), -- Chertuba Of Great Soul (35)
(25064,1,97223,94223,-3712,0,43200,129600,0,218810,1062), -- Wizard Of Storm Teruk (40)
(25067,1,94992,-23168,-2176,0,43200,129600,0,554640,1784), -- Captain Of Red Flag Shaka (52)
(25070,1,125600,50100,-3600,0,43200,129600,0,451391,1975), -- Enchanted Forest Watcher Ruell (55)
(25073,1,143265,110044,-3944,0,43200,129600,0,875948,2917), -- Bloody Priest Rudelto (69)
(25076,1,-60976,127552,-2960,0,43200,129600,0,103092,575), -- Princess Molrang (25)
(25079,1,53712,102656,-1072,0,43200,129600,0,168366,731), -- Cat's Eye Bandit (30)
(25082,1,88512,140576,-3483,0,43200,129600,0,206753,1028), -- Leader Of Cat Gang (39)
(25085,1,66944,67504,-3704,0,43200,129600,0,371721,1296), -- Timak Orc Chief Ranger (44)
(25088,1,90848,16368,-5296,0,43200,129600,0,702418,1237), -- Crazy Mechanic Golem (43)
(25089,1,165424,93776,-2992,0,43200,129600,0,512194,2235), -- Soulless Wild Boar (59)
(25092,1,116151,16227,1944,0,43200,129600,0,888658,2987), -- Korim (70)
(25095,1,-37856,198128,-2672,0,43200,129600,0,121941,699), -- Elf Renoa (29)
(25098,1,123536,133504,-3584,0,43200,129600,0,330579,893), -- Sejarr's Servitor (35)
(25099,1,64048,16048,-3536,0,43200,129600,0,273375,1296), -- Rotten Tree Repiro (44)
(25102,1,113840,84256,-2480,0,43200,129600,0,576831,1355), -- Shacram (45)
(25103,1,135872,94592,-3735,0,43200,129600,0,451391,1975), -- Sorcerer Isirr (55)
(25106,1,173880,-11412,-2880,0,43200,129600,0,526218,2301), -- Ghost Of The Well Lidia (60)
(25109,1,152660,110387,-5520,0,43200,129600,0,935092,3274), -- Antharas Priest Cloe (74)
(25112,1,116128,139392,-3640,0,43200,129600,0,127782,731), -- Agent Of Beres, Meana (30)
(25115,1,125789,207644,-3752,0,43200,129600,0,294846,1062), -- Icarus Sample 1 (40)
(25118,1,50896,146576,-3645,0,43200,129600,0,330579,893), -- Guilotine, Warden Of The Execution Grounds (35)
(25119,1,121872,64032,-3536,0,43200,129600,0,507285,1660), -- Messenger Of Fairy Queen Berun (50)
(25122,1,86300,-8200,-3000,0,43200,129600,0,467209,2039), -- Refugee Hopeful Leo (56)
(25125,1,170656,85184,-2000,0,43200,129600,0,1637918,2639), -- Fierce Tiger King Angel (65)
(25126,1,116263,15916,6992,0,43200,129600,0,1974940,3643), -- Longhorn Golkonda (79)
(25127,1,-47552,219232,-2413,0,43200,129600,0,198734,545), -- Langk Matriarch Rashkos (24)
(25128,1,17696,179056,-3520,0,43200,129600,0,148507,827), -- Vuku Grand Seer Gharmash (33)
(25131,1,75488,-9360,-2720,0,43200,129600,0,369009,1660), -- Carnage Lord Gato (50)
(25134,1,95260,82042,-3712,0,43200,129600,0,218810,1062), -- Leto Chief Talkin (40)
(25137,1,125280,102576,-3305,0,43200,129600,0,451391,1975), -- Beleth's Seer Sephia (55)
(25143,1,113102,16002,6992,0,43200,129600,0,977229,3568), -- Fire Of Wrath Shuriel (78)
(25146,1,-13056,215680,-3760,0,43200,129600,0,90169,455), -- Serpent Demon Bifrons (21)
(25149,1,-12656,138176,-3584,0,43200,129600,0,103092,575), -- Zombie Lord Crowl (25)
(25152,1,43872,123968,-2928,0,43200,129600,0,165289,893), -- Flame Lord Shadar (35)
(25155,1,73520,66912,-3728,0,43200,129600,0,294846,1053), -- Shaman King Selu (40)
(25158,1,77104,5408,-3088,0,43200,129600,0,920790,1523), -- King Tarlk (48)
(25159,1,124984,43200,-3625,0,43200,129600,0,435256,1911), -- Paniel The Unicorn (54)
(25163,1,130500,59098,3584,0,43200,129600,0,1777317,2987), -- Roaring Skylancer (70)
(25166,1,-21800,152000,-2900,0,43200,129600,0,134813,575), -- Ikuntai (25)
(25169,1,-54464,170288,-3136,0,43200,129600,0,336732,731), -- Ragraman (30)
(25170,1,26064,121808,-3738,0,43200,129600,0,195371,994), -- Lizardmen Leader Hellion (38)
(25173,1,75968,110784,-2512,0,43200,129600,0,288415,1355), -- Tiger King Karuta (45)
(25176,1,92544,115232,-3200,0,43200,129600,0,451391,1975), -- Black Lily (55)
(25179,1,167152,53120,-4148,0,43200,129600,0,526218,2301), -- Guardian Of The Statue Of Giant Karum (60)
(25182,1,41966,215417,-3728,0,43200,129600,0,512194,2235), -- Demon Kurikups (59)
(25185,1,99732,204331,-3784,0,43200,129600,0,165289,893), -- Tasaba Patriarch Hellena (35)
(25188,1,127544,215264,-2960,0,43200,129600,0,255564,731), -- Apepi (30)
(25189,1,127837,200661,-3792,0,43200,129600,0,156584,860), -- Cronos's Servitor Mumu (34)
(25192,1,125920,190208,-3291,0,43200,129600,0,258849,1237), -- Earth Protector Panathen (43)
(25198,1,102656,157424,-3735,0,43200,129600,0,1777317,2987), -- Fafurion's Herald Lokness (70)
(25199,1,108096,157408,-3688,0,43200,129600,0,912634,3130), -- Water Dragon Seer Sheshark (72)
(25202,1,119760,157392,-3744,0,43200,129600,0,935092,3274), -- Krokian Padisha Sobekk (74)
(25205,1,123808,153408,-3671,0,43200,129600,0,956490,3420), -- Ocean Flame Ashakiel (76)
(25208,1,109663,213615,-3624,0,43200,129600,0,218810,1062), -- Water Couatle Ateka (40)
(25211,1,113456,198118,-3689,0,43200,129600,0,174646,927), -- Sebek (36)
(25214,1,111582,209341,-3687,0,43200,129600,0,218810,1062), -- Fafurion's Page Sika (40)
(25217,1,89904,105712,-3292,0,43200,129600,0,369009,1660), -- Cursed Clara (50)
(25220,1,113551,17083,-2120,0,43200,129600,0,924022,3202), -- Death Lord Hallate (73)
(25223,1,43152,152352,-2848,0,43200,129600,0,165289,893), -- Soul Collector Acheron (35)
(25226,1,104240,-3664,-3392,0,43200,129600,0,768537,2435), -- Roaring Lord Kastor (62)
(25229,1,137568,-19488,-3552,0,43200,129600,0,1891801,3347), -- Storm Winged Naga (75)
(25230,1,66672,46704,-3920,0,43200,129600,0,482650,2104), -- Timak Seer Ragoth (57)
(25233,1,185800,-26500,-2000,0,43200,129600,0,1256671,2917), -- Spirit Of Andras, The Betrayer (69)
(25234,1,120080,111248,-3047,0,43200,129600,0,1052436,2301), -- Ancient Weird Drake (60)
(25235,1,116400,-62528,-3264,0,43200,129600,0,912634,3130), -- Vanor Chief Kandra (72)
(25238,1,155000,85400,-3200,0,43200,129600,0,512194,2235), -- Abyss Brukunt (59)
(25241,1,165984,88048,-2384,0,43200,129600,0,624464,1975), -- Harit Hero Tamash (55)
(25244,1,171880,54868,-5992,0,43200,129600,0,1891801,3347), -- Last Lesser Giant Olkuth (75)
(25245,1,188809,47780,-5968,0,43200,129600,0,977229,3568), -- Last Lesser Giant Glaki (78)
(25248,1,127903,-13399,-3720,0,43200,129600,0,1825269,3130), -- Doom Blade Tanatos (72)
(25249,1,147104,-20560,-3377,0,43200,129600,0,945900,3347), -- Palatanos Of Horrific Power (75)
(25252,1,192376,22087,-3608,0,43200,129600,0,888658,2987), -- Palibati Queen Themis (70)
(25255,1,170048,-24896,-3440,0,43200,129600,0,1637918,2639), -- Gargoyle Lord Tiphon (65)
(25256,1,170320,42640,-4832,0,43200,129600,0,526218,2301), -- Taik High Prefect Arak (60)
(25259,1,42050,208107,-3752,0,43200,129600,0,1248928,1975), -- Zaken's Butcher Krantz (55)
(25260,1,93120,19440,-3607,0,43200,129600,0,392985,1355), -- Iron Giant Totem (45)
(25263,1,144400,-28192,-1920,0,43200,129600,0,848789,2777), -- Kernon's Faithful Servant Kelone (67)
(25266,1,188983,13647,-2672,0,43200,129600,0,945900,3347), -- Bloody Empress Decarbia (75)
(25269,1,123504,-23696,-3481,0,43200,129600,0,888658,3058), -- Beast Lord Behemoth (70)
(25272,1,49248,127792,-3552,0,43200,129600,0,233163,1415), -- Partisan Leader Talakin (28)
-- (25273,1,23800,119500,-8976,0,43200,129600,0,507285,2104), -- Carnamakos (50) -- (Spawn by Quest)
(25276,1,154088,-14116,-3736,0,43200,129600,0,1891801,3347), -- Death Lord Ipos (75)
(25277,1,54651,180269,-4976,0,43200,129600,0,507285,1660), -- Lilith's Witch Marilion (50)
(25280,1,85622,88766,-5120,0,43200,129600,0,1248928,1975), -- Pagan Watcher Cerberon (55)
(25281,1,151053,88124,-5424,0,43200,129600,0,1777317,2987), -- Anakim's Nemesis Zakaron (70)
(25282,1,179311,-7632,-4896,0,43200,129600,0,1891801,3347), -- Death Lord Shax (75)
-- (25283,1,184410,-10111,-5488,0,43200,129600,0,1639146,3793), -- Lilith (80) -- (Spawn by Seven Signs)
-- (25286,1,185000,-13000,-5488,0,43200,129600,0,1639146,3793), -- Anakim (80) -- (Spawn by Seven Signs)
-- (25290,1,186304,-43744,-3193,0,43200,129600,0,977229,3718), -- Daimon The White-Eyed (78) -- (Spawn by Quest 604_DaimontheWhiteEyedPart2)
(25293,1,134672,-115600,-1216,0,43200,129600,0,977229,3568), -- Hestia, Guardian Deity Of The Hot Springs (78)
-- (25296,1,158352,-121088,-2240,0,43200,129600,0,935092,3718), -- Icicle Emperor Bumbalump (74) -- (Spawn by Quest 625_TheFinestIngredientsPart2)
(25299,1,148160,-73808,-4919,0,43200,129600,0,714778,3718), -- Ketra's Hero Hekaton (80)
(25302,1,145504,-81664,-6016,0,43200,129600,0,773553,4183), -- Ketra's Commander Tayr (80)
(25305,1,145008,-84992,-6240,0,43200,129600,0,1639965,4553), -- Ketra's Chief Brakki (80)
-- (25306,1,142368,-82512,-6487,0,43200,129600,0,534922,3718), -- Soul Of Fire Nastron (80) -- (Spawn by Quest 616_MagicalPowerOfFirePart2)
(25309,1,115552,-39200,-2480,0,43200,129600,0,714778,3718), -- Varka's Hero Shadith (80)
(25312,1,109216,-36160,-938,0,43200,129600,0,773553,4183), -- Varka's Commander Mos (80)
(25315,1,105584,-43024,-1728,0,43200,129600,0,1639965,4553), -- Varka's Chief Horus (80)
-- (25316,1,105452,-36775,-1050,0,43200,129600,0,534922,3718), -- Soul Of Water Ashutar (80) -- (Spawn by Quest 610_MagicalPowerOfWaterPart2)
(25319,1,184542,-106330,-6304,0,43200,129600,0,1100996,4304), -- Ember (80)
(25322,1,93296,-75104,-1824,0,43200,129600,0,834231,2707), -- Demon's Agent Falston (66)
(25325,1,91008,-85904,-2736,0,43200,129600,0,888658,2987), -- Flame Of Splendor Barakiel (70)
(25328,1,59331,-42403,-3003,0,43200,129600,0,900867,3058), -- Eilhalder Von Hellmann (71) -- (Only Spawn at Night)
-- (25339,1,0,0,0,0,43200,129600,0,96646,3718), -- Shadow Of Halisha (81) -- (Spawn by Four Sepulchers Instance)
-- (25342,1,0,0,0,0,43200,129600,0,96646,3718), -- Shadow Of Halisha (81) -- (Spawn by Four Sepulchers Instance)
-- (25346,1,0,0,0,0,43200,129600,0,96646,3718), -- Shadow Of Halisha (81) -- (Spawn by Four Sepulchers Instance)
-- (25349,1,0,0,0,0,43200,129600,0,96646,3718), -- Shadow Of Halisha (81) -- (Spawn by Four Sepulchers Instance)
(25352,1,-16912,174912,-3264,0,43200,129600,0,127782,731), -- Giant Wasteland Basilisk (30)
(25354,1,-16096,184288,-3817,0,43200,129600,0,165289,893), -- Gargoyle Lord Sirocco (35)
(25357,1,-3456,112864,-3456,0,43200,129600,0,90169,455), -- Sukar Wererat Chief (21)
(25360,1,29216,179280,-3624,0,43200,129600,0,107186,606), -- Tiger Hornet (26)
(25362,1,-55920,186768,-3336,0,43200,129600,0,95986,514), -- Tracker Leader Sharuk (23)
(25365,1,-62000,190256,-3687,0,43200,129600,0,214372,606), -- Patriarch Kuroboros (26)
(25366,1,-62368,179440,-3594,0,43200,129600,0,95986,514), -- Kuroboros' Priest (23)
(25369,1,-45616,111024,-3808,0,43200,129600,0,103092,575), -- Soul Scavenger (25)
(25372,1,48000,243376,-6611,0,43200,129600,0,175392,426), -- Discarded Guardian (20)
(25373,1,9649,77467,-3808,0,43200,129600,0,90169,455), -- Malex Herald Of Dagoniel (21)
(25375,1,22500,80300,-2772,0,43200,129600,0,87696,426), -- Zombie Lord Farakelsus (20)
(25378,1,-54096,84288,-3512,0,43200,129600,0,87696,426), -- Madness Beast (20)
(25380,1,-47367,51548,-5904,0,43200,129600,0,90169,455), -- Kaysha Herald Of Icarus (21)
(25383,1,51632,153920,-3552,0,43200,129600,0,156584,860), -- Revenant Of Sir Calibus (34)
(25385,1,53600,143472,-3872,0,43200,129600,0,174646,927), -- Evil Spirit Tempest (36)
(25388,1,40128,101920,-1241,0,43200,129600,0,165289,893), -- Red Eye Captain Trakia (35)
(25391,1,45600,120592,-2455,0,43200,129600,0,297015,827), -- Nurka's Messenger (33)
(25392,1,29928,107160,-3708,0,43200,129600,0,141034,795), -- Captain Of Queen's Royal Guards (32)
(25394,1,129481,219722,-3600,0,43200,129600,0,390743,994), -- Premo Prime (38)
(25395,1,15000,119000,-11900,0,43200,129600,0,288415,1355), -- Archon Suscepter (45)
(25398,1,5000,189000,-3728,0,43200,129600,0,165289,893), -- Eye Of Beleth (35)
(25401,1,117808,102880,-3600,0,43200,129600,0,141034,795), -- Skyla (32)
(25404,1,35992,191312,-3104,0,43200,129600,0,148507,827), -- Corsair Captain Kylon (33)
(25407,1,115072,112272,-3018,0,43200,129600,0,526218,2301), -- Lord Ishka (60)
(25410,1,72192,125424,-3657,0,43200,129600,0,218810,1062), -- Road Scavenger Leader (40)
(25412,1,81920,113136,-3056,0,43200,129600,0,319791,1476), -- Necrosentinel Royal Guard (47)
(25415,1,128352,138464,-3467,0,43200,129600,0,218810,1062), -- Nakondas (40)
(25418,1,62416,8096,-3376,0,43200,129600,0,273375,1296), -- Dread Avenger Kraven (44)
(25420,1,42032,24128,-4704,0,43200,129600,0,335987,1537), -- Orfen's Handmaiden (48)
(25423,1,113600,47120,-4640,0,43200,129600,0,539706,2368), -- Fairy Queen Timiniel (61)
(25426,1,-18048,-101264,-2112,0,43200,129600,0,103092,575), -- Betrayer Of Urutu Freki (25)
(25429,1,172064,-214752,-3565,0,43200,129600,0,103092,575), -- Mammon Collector Talos (25)
(25431,1,79648,18320,-5232,0,43200,129600,0,273375,1296), -- Flamestone Golem (44)
(25434,1,104096,-16896,-1803,0,43200,129600,0,451391,1975), -- Bandit Leader Barda (55)
(25437,1,67296,64128,-3723,0,43200,129600,0,576831,1355), -- Timak Orc Gosmos (45)
(25438,1,107000,92000,-2272,0,43200,129600,0,273375,1296), -- Thief Kelbar (44)
(25441,1,111440,82912,-2912,0,43200,129600,0,288415,1355), -- Evil Spirit Cyrion (45)
(25444,1,113232,17456,-4384,0,43200,129600,0,588136,2639), -- Enmity Ghost Ramdal (65)
(25447,1,113200,17552,-1424,0,43200,129600,0,645953,3058), -- Immortal Savior Mardil (71)
(25450,1,113600,15104,9559,0,43200,129600,0,987470,3643), -- Cherub Galaxia (79)
(25453,1,156704,-6096,-4185,0,43200,129600,0,888658,2987), -- Meanas Anor (70)
(25456,1,133632,87072,-3623,0,43200,129600,0,352421,1598), -- Mirror Of Oblivion (49)
(25460,1,150304,67776,-3688,0,43200,129600,0,385670,1722), -- Deadman Ereve (51)
(25463,1,166288,68096,-3264,0,43200,129600,0,467209,2039), -- Harit Guardian Garangky (56)
(25473,1,175712,29856,-3776,0,43200,129600,0,402319,1784), -- Grave Robber Kim (52)
(25475,1,183568,24560,-3184,0,43200,129600,0,451391,1975), -- Ghost Knight Kabed (55)
(25478,1,168288,28368,-3632,0,43200,129600,0,588136,2639), -- Shilen's Priest Hisilrome (65)
(25481,1,53517,205413,-3728,0,43200,129600,0,418874,1847), -- Magus Kenishee (53)
(25484,1,43160,220463,-3680,0,43200,129600,0,369009,1660), -- Zaken's Chief Mate Tillion (50)
(25493,1,83174,254428,-10873,0,43200,129600,0,451391,1975), -- Eva's Spirit Niniel (55)
(25496,1,88300,258000,-10200,0,43200,129600,0,402319,1784), -- Fafurion's Envoy Pingolpin (52)
(25498,1,126624,174448,-3056,0,43200,129600,0,288415,1355), -- Fafurion's Henchman Istary (45)
(25501,1,48575,-106191,-1568,0,43200,129600,0,127782,731), -- Boss Akata (30)
(25504,1,123000,-141000,-1100,0,43200,129600,0,206753,1028), -- Nellis' Vengeful Spirit (39)
(25506,1,127900,-160600,-1100,0,43200,129600,0,184670,960), -- Rayito The Looter (37)
-- (25514,79635,-55612,-5980,0,43200,129600,0,714778,3718), -- Queen Shyeed (80) -- spawns by script
(25523,1,170000,-60000,-3500,0,43200,129600,0,1848045,3202), -- Plague Golem (73)
(25524,1,144600,-5500,-4100,0,43200,129600,0,956490,3420), -- Flamestone Giant (76)
(25527,1,3776,-6768,-3276,0,43200,129600,0,1608553,451), -- Uruka (80)
-- (25528,1,0,0,0,0,43200,129600,0,49148,9999), -- Tiberias (22) -- handled by instance script
-- (25531,1,0,0,0,0,43200,129600,0,2140552,9999), -- Darnel (81) -- stats to be done
-- (25532,1,0,0,0,0,43200,129600,0,534278,9999), -- Kechi (82) -- stats to be done
-- (25534,1,0,0,0,0,43200,129600,0,2129066,9999), -- Tears (83) -- stats to be done
-- (25536,1,0,0,0,0,43200,129600,0,1027906,9999), -- Hannibal (83) -- stats to be done
(25539,1,-17475,253163,-3432,0,43200,129600,0,2076371,9999), -- Typhoon (84)
-- (25540,1,0,0,0,0,43200,129600,0,3524173,9999), -- Demon Prince (83) -- spawn by script
-- (25542,1,0,0,0,0,43200,129600,0,3524173,9999), -- Ranku (83) -- spawn by script
(25544,1,0,0,0,0,43200,129600,0,2682423,9999), -- Tully (83)
(25603,1,0,0,0,0,43200,129600,0,2308600,9999), -- Darion (87)
-- (25609,1,0,0,0,0,43200,129600,0,9999,9999), -- Epidos (?) -- stats to be done
-- (25610,1,0,0,0,0,43200,129600,0,9999,9999), -- Epidos (?) -- stats to be done
-- (25611,1,0,0,0,0,43200,129600,0,9999,9999), -- Epidos (?) -- stats to be done
-- (25612,1,0,0,0,0,43200,129600,0,9999,9999), -- Epidos (?) -- stats to be done
-- (25623,1,-192361,254528,15,0,43200,129600,0,99999,99999), -- Valdstone (?) -- stats to be done
-- (25624,1,-174600,219711,44,0,43200,129600,0,99999,99999), -- Rok (?) -- stats to be done
-- (25625,1,-181989,208968,44,0,43200,129600,0,99999,99999), -- Enira (?) -- stats to be done
-- (25626,1,-252898,235845,53,0,43200,129600,0,99999,99999), -- Dius (?) -- stats to be done
-- (25643,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Sentry (?) -- stats to be done
-- (25644,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Severer (?) -- stats to be done
-- (25645,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Soul Extractor (?) -- stats to be done
-- (25646,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Soul Devourer (?) -- stats to be done
-- (25647,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Fighter (?) -- stats to be done
-- (25648,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Fighter (?) -- stats to be done
-- (25649,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Executor (?) -- stats to be done
-- (25650,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Executor (?) -- stats to be done
-- (25651,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Prophet  (?) -- stats to be done
-- (25652,1,0,0,0,0,43200,129600,0,9999,9999), -- Awakened Ancient Prophet (?) -- stats to be done
-- (25665,1,0,0,0,0,43200,129600,0,9999,9999), -- Yehan Klodekus (?) -- stats to be done
-- (25666,1,0,0,0,0,43200,129600,0,9999,9999), -- Yehan Klanikus (?) -- stats to be done
-- (25667,0,0,0,0,43200,129600,0,9999,9999), -- Cannibalistic Stakato Chief (84) -- Spawns by script
-- (25668,0,0,0,0,43200,129600,0,9999,9999), -- Cannibalistic Stakato Chief (84) -- Spawns by script
-- (25669,0,0,0,0,43200,129600,0,9999,9999), -- Cannibalistic Stakato Chief (84) -- Spawns by script
-- (25670,0,0,0,0,43200,129600,0,9999,9999), -- Cannibalistic Stakato Chief (84) -- Spawns by script
(25674,1,86534,216888,-3176,0,86400,129600,0,736436,3945), -- Gwindorr (83)
(25677,1,83056,183232,-3616,0,43200,129600,0,743801,4022), -- Water Spirit Lian (84)
(25680,1,193902,54135,-4184,0,21600,21600,0,2035459,3869), -- Giant Marpanak (82)
(25681,1,186210,61479,-4000,0,21600,21600,0,729145,3869), -- Gorgolos (82)
(25684,1,186919,56297,-4480,0,21600,21600,0,736436,3945), -- Last Titan Utenus (83)
(25687,1,191777,56197,-7624,0,3600,3600,0,1027906,3945), -- Hekaton Prime (83)
(25701,1,112798,-76800,-10,-15544,86400,129600,0,2231403,48422), -- Anays (84)
(29040,1,189400,-105702,-782,0,604800,604800,0,520605,4140), -- Wings of Flame Ixion (84)
-- (29054,1,11882,-49216,-3008,0,43200,129600,0,1352750,1494), -- Benom (75) (spawn 1hr before siege in rune start)
-- (29060,1,106000,-128000,-3000,0,43200,129600,0,1566263,9999), -- Captain Of The Ice Queen's Royal Guard (59) (Spawn by Quest)
-- (29062,1,-16373,-53562,-10197,0,43200,129600,0,275385,9999), -- Andreas Van Halter (80) -- stats to be done (Spawn by Quest)
-- (29065,1,26528,-8244,-2007,0,43200,129600,0,1639965,9999), -- Sailren (80) -- stats to be done (Spawn by Quest)
(29095,1,141569,-45908,-2387,0,43200,129600,0,2289038,4553); -- Gordon (80) -- walking around Goddard
