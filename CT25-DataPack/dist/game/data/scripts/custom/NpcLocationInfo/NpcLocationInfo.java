package custom.NpcLocationInfo;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import javolution.util.FastList;
import javolution.util.FastMap;

public class NpcLocationInfo extends Quest
{
	private static final String qn = "NpcLocationInfo";

	private static class Location
	{
		public final int x, y, z;

		public Location(final int _x, final int _y, final int _z)
		{
			x = _x;
			y = _y;
			z = _z;
		}
	}

	private static FastMap<Integer, Location> locations = new FastMap<>();
	static
	{
		// Talking Island
		locations.put(30006, new Location(-84108, 244604, -3729)); // Gatekeeper Roxxy
		locations.put(30039, new Location(-82236, 241573, -3728)); // Captain Gilbert
		locations.put(30040, new Location(-82515, 241221, -3728)); // Guard Leon
		locations.put(30041, new Location(-82319, 244709, -3727)); // Guard Arnold
		locations.put(30042, new Location(-82659, 244992, -3717)); // Guard Abellos
		locations.put(30043, new Location(-86114, 244682, -3727)); // Guard Johnstone
		locations.put(30044, new Location(-86328, 244448, -3724)); // Guard Chiperan
		locations.put(30045, new Location(-86322, 241215, -3727)); // Guard Kenyos
		locations.put(30046, new Location(-85964, 240947, -3727)); // Guard Hanks
		locations.put(30283, new Location(-85026, 242689, -3729)); // Blacksmith Altran
		locations.put(30003, new Location(-83789, 240799, -3717)); // Trader Silvia
		locations.put(30004, new Location(-84204, 240403, -3717)); // Trader Katerina
		locations.put(30001, new Location(-86385, 243267, -3717)); // Trader Lector
		locations.put(30002, new Location(-86733, 242918, -3717)); // Trader Jackson
		locations.put(30031, new Location(-84516, 245449, -3714)); // High Priest Biotin
		locations.put(30033, new Location(-84729, 245001, -3726)); // Magister Baulro
		locations.put(30035, new Location(-84965, 245222, -3726)); // Magister Harrys
		locations.put(30032, new Location(-84981, 244764, -3726)); // Priest Yohanes
		locations.put(30036, new Location(-85186, 245001, -3726)); // Priest Petron
		locations.put(30026, new Location(-83326, 242964, -3718)); // Grand Master Bitz
		locations.put(30027, new Location(-83020, 242553, -3718)); // Master Gwinter
		locations.put(30029, new Location(-83175, 243065, -3718)); // Master Minia
		locations.put(30028, new Location(-82809, 242751, -3718)); // Master Pintage
		locations.put(30054, new Location(-81895, 243917, -3721)); // Warehouse Keeper Rant
		locations.put(30055, new Location(-81840, 243534, -3721)); // Warehouse Keeper Rolfe
		locations.put(30005, new Location(-81512, 243424, -3720)); // Warehouse Keeper Wilford
		locations.put(30048, new Location(-84436, 242793, -3729)); // Darin
		locations.put(30312, new Location(-78939, 240305, -3443)); // Lighthouse Keeper Rockswell
		locations.put(30368, new Location(-85301, 244587, -3725)); // Lilith
		locations.put(30049, new Location(-83163, 243560, -3728)); // Bonnie
		locations.put(30047, new Location(-97131, 258946, -3622)); // Wharf Manager Firon
		locations.put(30497, new Location(-114685, 222291, -2925)); // Edmond
		locations.put(30050, new Location(-84057, 242832, -3729)); // Elias
		locations.put(30311, new Location(-100332, 238019, -3573)); // Sir Collin Windawood
		locations.put(30051, new Location(-82041, 242718, -3725)); // Cristel

		// Dark Elf Village
		locations.put(30134, new Location(9670, 15537, -4499)); // Gatekeeper Jasmine
		locations.put(30224, new Location(15120, 15656, -4301)); // Sentry Knight Rayla
		locations.put(30348, new Location(17306, 13592, -3649)); // Sentry Nelsya
		locations.put(30355, new Location(15272, 16310, -4302)); // Sentry Roselyn
		locations.put(30347, new Location(6449, 19619, -3619)); // Sentry Marion
		locations.put(30432, new Location(-15404, 71131, -3370)); // Sentry Irene
		locations.put(30356, new Location(7490, 17397, -4378)); // Sentry Altima
		locations.put(30349, new Location(17102, 13002, -3668)); // Sentry Jenna
		locations.put(30346, new Location(6532, 19903, -3618)); // Sentry Kayleen
		locations.put(30433, new Location(-15648, 71405, -3376)); // Sentry Kathaway
		locations.put(30357, new Location(7634, 18047, -4378)); // Sentry Kristin
		locations.put(30431, new Location(-1301, 75883, -3491)); // Sentry Eriel
		locations.put(30430, new Location(-1152, 76125, -3491)); // Sentry Trionell
		locations.put(30307, new Location(10584, 17574, -4557)); // Blacksmith Karrod
		locations.put(30138, new Location(12009, 15704, -4555)); // Trader Minaless
		locations.put(30137, new Location(11951, 15661, -4555)); // Trader Vollodos
		locations.put(30135, new Location(10761, 17970, -4558)); // Trader Iria
		locations.put(30136, new Location(10823, 18013, -4558)); // Trader Payne
		locations.put(30143, new Location(11283, 14226, -4167)); // Master Trudy
		locations.put(30360, new Location(10447, 14620, -4167)); // Master Harant
		locations.put(30145, new Location(11258, 14431, -4167)); // Master Vlasty
		locations.put(30135, new Location(10761, 17970, -4558)); // Magister Harne
		locations.put(30144, new Location(10344, 14445, -4167)); // Tetrarch Vellior
		locations.put(30358, new Location(10775, 14190, -4167)); // Tetrarch Thifiell
		locations.put(30359, new Location(11235, 14078, -4167)); // Tetrarch Kaitar
		locations.put(30141, new Location(11012, 14128, -4167)); // Tetrarch Talloth
		locations.put(30139, new Location(13380, 17430, -4544)); // Warehouse Keeper Dorankus
		locations.put(30140, new Location(13464, 17751, -4544)); // Warehouse Keeper Erviante
		locations.put(30350, new Location(13763, 17501, -4544)); // Warehouse Freightman Carlon
		locations.put(30421, new Location(-44225, 79721, -3577)); // Varika
		locations.put(30419, new Location(-44015, 79683, -3577)); // Arkenia
		locations.put(30130, new Location(25856, 10832, -3649)); // Abyssal Celebrant Undrias
		locations.put(30351, new Location(12328, 14947, -4499)); // Astaron
		locations.put(30353, new Location(13081, 18444, -4498)); // Jughead
		locations.put(30354, new Location(12311, 17470, -4499)); // Jewel

		// Elven Village
		locations.put(30146, new Location(46926, 51511, -2977)); // Gatekeeper Mirabel
		locations.put(30285, new Location(44995, 51706, -2803)); // Sentinel Gartrandell
		locations.put(30284, new Location(45727, 51721, -2803)); // Sentinel Knight Alberius
		locations.put(30221, new Location(42812, 51138, -2996)); // Sentinel Rayen
		locations.put(30217, new Location(45487, 46511, -2996)); // Sentinel Berros
		locations.put(30219, new Location(47401, 51764, -2996)); // Sentinel Veltress
		locations.put(30220, new Location(42971, 51372, -2996)); // Sentinel Starden
		locations.put(30218, new Location(47595, 51569, -2996)); // Sentinel Kendell
		locations.put(30216, new Location(45778, 46534, -2996)); // Sentinel Wheeler
		locations.put(30363, new Location(44476, 47153, -2984)); // Blacksmith Aios
		locations.put(30149, new Location(42700, 50057, -2984)); // Trader Creamees
		locations.put(30150, new Location(42766, 50037, -2984)); // Trader Herbiel
		locations.put(30148, new Location(44683, 46952, -2981)); // Trader Ariel
		locations.put(30147, new Location(44667, 46896, -2982)); // Trader Unoren
		locations.put(30155, new Location(45725, 52105, -2795)); // Master Ellenia
		locations.put(30156, new Location(44823, 52414, -2795)); // Master Cobendell
		locations.put(30157, new Location(45000, 52101, -2795)); // Magister Greenis
		locations.put(30158, new Location(45919, 52414, -2795)); // Magister Esrandell
		locations.put(30154, new Location(44692, 52261, -2795)); // Hierarch Asterios
		locations.put(30153, new Location(47780, 49568, -2983)); // Warehouse Keeper Markius
		locations.put(30152, new Location(47912, 50170, -2983)); // Warehouse Keeper Julia
		locations.put(30151, new Location(47868, 50167, -2983)); // Warehouse Freightman Chad
		locations.put(30423, new Location(28928, 74248, -3773)); // Northwind
		locations.put(30414, new Location(43673, 49683, -3046)); // Rosella
		locations.put(31853, new Location(50592, 54896, -3376)); // Treant Bremec
		locations.put(30223, new Location(42978, 49115, -2994)); // Arujien
		locations.put(30362, new Location(46475, 50495, -3058)); // Andellia
		locations.put(30222, new Location(45859, 50827, -3058)); // Alshupes
		locations.put(30371, new Location(51210, 82474, -3283)); // Thalia
		locations.put(31852, new Location(49262, 53607, -3216)); // Pixy Murika

		// Dwarven Village
		locations.put(30540, new Location(115072, -178176, -906)); // Gatekeeper Wirphy
		locations.put(30541, new Location(117847, -182339, -1537)); // Protector Paion
		locations.put(30542, new Location(116617, -184308, -1569)); // Defender Runant
		locations.put(30543, new Location(117826, -182576, -1537)); // Defender Ethan
		locations.put(30544, new Location(116378, -184308, -1571)); // Defender Cromwell
		locations.put(30545, new Location(115183, -176728, -791)); // Defender Proton
		locations.put(30546, new Location(114969, -176752, -790)); // Defender Dinkey
		locations.put(30547, new Location(117366, -178725, -1118)); // Defender Tardyon
		locations.put(30548, new Location(117378, -178914, -1120)); // Defender Nathan
		locations.put(30531, new Location(116226, -178529, -948)); // Iron Gate's Lockirin
		locations.put(30532, new Location(116190, -178441, -948)); // Golden Wheel's Spiron
		locations.put(30533, new Location(116016, -178615, -948)); // Silver Scale's Balanki
		locations.put(30534, new Location(116190, -178615, -948)); // Bronze Key's Keef
		locations.put(30535, new Location(116103, -178407, -948)); // Filaur of the Gray Pillar
		locations.put(30536, new Location(116103, -178653, -948)); // Black Anvil's Arin
		locations.put(30525, new Location(115468, -182446, -1434)); // Head Blacksmith Bronk
		locations.put(30526, new Location(115315, -182155, -1444)); // Blacksmith Brunon
		locations.put(30527, new Location(115271, -182692, -1445)); // Blacksmith Silvera
		locations.put(30518, new Location(115900, -177316, -915)); // Trader Garita
		locations.put(30519, new Location(116268, -177524, -914)); // Trader Mion
		locations.put(30516, new Location(115741, -181645, -1344)); // Trader Reep
		locations.put(30517, new Location(116192, -181072, -1344)); // Trader Shari
		locations.put(30520, new Location(115205, -180024, -870)); // Warehouse Chief Reed
		locations.put(30521, new Location(114716, -180018, -871)); // Warehouse Freightman Murdoc
		locations.put(30522, new Location(114832, -179520, -871)); // Warehouse Keeper Airy
		locations.put(30523, new Location(115717, -183488, -1483)); // Collector Gouph
		locations.put(30524, new Location(115618, -183265, -1483)); // Collector Pippi
		locations.put(30537, new Location(114348, -178537, -813)); // Daichir, Priest of the Eart
		locations.put(30650, new Location(114990, -177294, -854)); // Priest of the Earth Gerald
		locations.put(30538, new Location(114426, -178672, -812)); // Priest of the Earth Zimenf
		locations.put(30539, new Location(114409, -178415, -812)); // Priestess of the Earth Chichirin
		locations.put(30671, new Location(117061, -181867, -1413)); // Captain Croto
		locations.put(30651, new Location(116164, -184029, -1507)); // Wanderer Dorf
		locations.put(30550, new Location(115563, -182923, -1448)); // Gauri Twinklerock
		locations.put(30554, new Location(112656, -174864, -611)); // Miner Bolter
		locations.put(30553, new Location(116852, -183595, -1566)); // Maryse Redbonnet

		// Orc Village
		locations.put(30576, new Location(-45264, -112512, -235)); // Gatekeeper Tamil
		locations.put(30577, new Location(-46576, -117311, -242)); // Praetorian Rukain
		locations.put(30578, new Location(-47360, -113791, -237)); // Centurion Nakusin
		locations.put(30579, new Location(-47360, -113424, -235)); // Centurion Tamai
		locations.put(30580, new Location(-45744, -117165, -236)); // Centurion Parugon
		locations.put(30581, new Location(-46528, -109968, -250)); // Centurion Orinak
		locations.put(30582, new Location(-45808, -110055, -255)); // Centurion Tiku
		locations.put(30583, new Location(-45731, -113844, -237)); // Centurion Petukai
		locations.put(30584, new Location(-45728, -113360, -237)); // Centurion Vapook
		locations.put(30569, new Location(-45952, -114784, -199)); // Prefect Brukurse
		locations.put(30570, new Location(-45952, -114496, -199)); // Prefect Karukia
		locations.put(30571, new Location(-45863, -112621, -200)); // Seer Tanapi
		locations.put(30572, new Location(-45864, -112540, -199)); // Seer Livina
		locations.put(30564, new Location(-43264, -112532, -220)); // Blacksmith Sumari
		locations.put(30560, new Location(-43910, -115518, -194)); // Trader Uska
		locations.put(30561, new Location(-43950, -115457, -194)); // Trader Papuma
		locations.put(30558, new Location(-44416, -111486, -222)); // Trader Jakal
		locations.put(30559, new Location(-43926, -111794, -222)); // Trader Kunai
		locations.put(30562, new Location(-43109, -113770, -221)); // Warehouse Keeper Grookin
		locations.put(30563, new Location(-43114, -113404, -221)); // Warehouse Keeper Imantu
		locations.put(30565, new Location(-46768, -113610, -3)); // Flame Lord Kakai
		locations.put(30566, new Location(-46802, -114011, -112)); // Atuba Chief Varkees
		locations.put(30567, new Location(-46247, -113866, -21)); // Neruga Chief Tantus
		locations.put(30568, new Location(-46808, -113184, -112)); // Urutu Chief Hatos
		locations.put(30585, new Location(-45328, -114736, -237)); // Tataru Zu Hestui
		locations.put(30587, new Location(-44624, -111873, -238)); // Gantaki Zu Urutu

		// Kamael Village
		locations.put(32163, new Location(-116879, 46591, 360)); // Gatekeeper Ragara
		locations.put(32173, new Location(-119378, 49242, 8)); // Zerstorer Marcela
		locations.put(32174, new Location(-119774, 49245, 8)); // Marksman Maddy
		locations.put(32175, new Location(-119830, 51860, -792)); // Marksman Bixon
		locations.put(32176, new Location(-119362, 51862, -792)); // Marksman Ambra
		locations.put(32177, new Location(-112872, 46850, 48)); // Marksman Syzar
		locations.put(32178, new Location(-112352, 47392, 48)); // Guard Karba
		locations.put(32179, new Location(-110544, 49040, -1128)); // Marksman Putin
		locations.put(32180, new Location(-110536, 45162, -1128)); // Marksman Kato
		locations.put(32164, new Location(-115888, 43568, 524)); // Weapons Trader Erinu
		locations.put(32165, new Location(-115486, 43567, 525)); // Armor Trader Zacon
		locations.put(32168, new Location(-116920, 47792, 456)); // Magic Trader Janis
		locations.put(32166, new Location(-116749, 48077, 462)); // Accessory Trader Treavi
		locations.put(32167, new Location(-117153, 48075, 456)); // Consumption Goods Trader Neazel
		locations.put(32141, new Location(-119104, 43280, 544)); // Master Nerga
		locations.put(32142, new Location(-119104, 43152, 544)); // Master Tenor
		locations.put(32143, new Location(-117056, 43168, 544)); // Master Belkis
		locations.put(32144, new Location(-117060, 43296, 544)); // Master Sonya
		locations.put(32145, new Location(-118192, 42384, 824)); // Grand Master Maynard
		locations.put(32146, new Location(-117968, 42384, 824)); // Grand Master Valpor
		locations.put(32139, new Location(-118132, 42788, 712)); // Hierarch Casca
		locations.put(32140, new Location(-118028, 42778, 712)); // Hierarch Zenya
		locations.put(32138, new Location(-118080, 42835, 712)); // Hierarch Kekropus
		locations.put(32171, new Location(-114802, 44821, 524)); // Warehouse Chief Hoffa
		locations.put(32170, new Location(-114975, 44658, 512)); // Warehouse Keeper Benis
		locations.put(32172, new Location(-114801, 45031, 525)); // Warehouse Freightman Saylem
		locations.put(32153, new Location(-120432, 45296, 408)); // High Priest Prana
		locations.put(32154, new Location(-120706, 45079, 408)); // Grand Master Aldenia
		locations.put(32155, new Location(-120356, 45293, 408)); // Priest Nabot
		locations.put(32156, new Location(-120604, 44960, 408)); // Master Talbot
		locations.put(32150, new Location(-120294, 46013, 384)); // Hight Prefect Took
		locations.put(32151, new Location(-120157, 45813, 344)); // Prefect Harz
		locations.put(32152, new Location(-120158, 46221, 344)); // Seer Henri
		locations.put(32147, new Location(-120400, 46921, 400)); // Grand Master Libian
		locations.put(32148, new Location(-120407, 46755, 408)); // Master Sydnet
		locations.put(32149, new Location(-120442, 47125, 408)); // Magister Enea
		locations.put(32160, new Location(-118720, 48062, 464)); // Grand Magister Devon
		locations.put(32162, new Location(-118918, 47956, 464)); // Magister Martika
		locations.put(32161, new Location(-118527, 47955, 464)); // Master Black
		locations.put(32158, new Location(-117605, 48079, 456)); // Warehouse Chief Fisler
		locations.put(32157, new Location(-117824, 48080, 464)); // Head Blacksmith Moka
		locations.put(32159, new Location(-118030, 47930, 456)); // Blacksmith Kincaid
		locations.put(32169, new Location(-119237, 46587, 360)); // Spellbook Trader Mifren
	}

	private final FastList<Integer> npcIds = new FastList<>();

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.endsWith(".htm"))
			return event;
		String htmltext = "";
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return "";
		final int npcId = Integer.parseInt(event);
		if (locations.keySet().contains(npcId))
		{
			final Location loc = locations.get(npcId);
			st.addRadar(loc.x, loc.y, loc.z);
			htmltext = "MoveToLoc.htm";
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final int npcId = npc.getId();
		if (npcIds.contains(npcId))
			htmltext = String.valueOf(npcId) + ".htm";
		return htmltext;
	}

	public NpcLocationInfo(final int id, final String name, final String desc)
	{
		super(id, name, desc);
		final int[] NPC_IDS =
		{
			30598,
			30599,
			30600,
			30601,
			30602,
			32135
		};
		for (final int i : NPC_IDS)
		{
			addStartNpc(i);
			addTalkId(i);
			npcIds.add(i);
		}
	}

	public static void main(final String[] args)
	{
		new NpcLocationInfo(-1, qn, "custom");
	}
}
