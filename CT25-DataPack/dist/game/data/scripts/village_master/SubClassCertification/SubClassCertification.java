package village_master.SubClassCertification;

import ct25.xtreme.gameserver.datatables.CharTemplateTable;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2VillageMasterInstance;
import ct25.xtreme.gameserver.model.base.ClassType;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.util.ArrayUtils;

public class SubClassCertification extends Quest
{
	private static final String qn = "SubClassCertification";
	
	private static final int[] NPC =
	{
		30026,
		30031,
		30037,
		30066,
		30070,
		30109,
		30115,
		30120,
		30154,
		30174,
		30175,
		30176,
		30187,
		30191,
		30195,
		30288,
		30289,
		30290,
		30297,
		30358,
		30373,
		30462,
		30474,
		30498,
		30499,
		30500,
		30503,
		30504,
		30505,
		30508,
		30511,
		30512,
		30513,
		30520,
		30525,
		30565,
		30594,
		30595,
		30676,
		30677,
		30681,
		30685,
		30687,
		30689,
		30694,
		30699,
		30704,
		30845,
		30847,
		30849,
		30854,
		30857,
		30862,
		30865,
		30894,
		30897,
		30900,
		30905,
		30910,
		30913,
		31269,
		31272,
		31276,
		31279,
		31285,
		31288,
		31314,
		31317,
		31321,
		31324,
		31326,
		31328,
		31331,
		31334,
		31336,
		31755,
		31958,
		31961,
		31965,
		31968,
		31974,
		31977,
		31996,
		32092,
		32093,
		32094,
		32095,
		32096,
		32097,
		32098,
		32145,
		32146,
		32147,
		32150,
		32153,
		32154,
		32157,
		32158,
		32160,
		32171,
		32193,
		32199,
		32202,
		32213,
		32214,
		32221,
		32222,
		32229,
		32230,
		32233,
		32234
	};
	
	private static final int[] WARRIORCLASSES =
	{
		3,
		88,
		2,
		89,
		46,
		48,
		113,
		114,
		55,
		117,
		56,
		118,
		127,
		131,
		128,
		129,
		132,
		133
	};
	private static final int[] ROGUECLASSES =
	{
		9,
		92,
		24,
		102,
		37,
		109,
		130,
		134,
		8,
		93,
		23,
		101,
		36,
		108
	};
	private static final int[] KNIGHTCLASSES =
	{
		5,
		90,
		6,
		91,
		20,
		99,
		33,
		106
	};
	private static final int[] SUMMONERCLASSES =
	{
		14,
		96,
		28,
		104,
		41,
		111
	};
	private static final int[] WIZARDCLASSES =
	{
		12,
		94,
		13,
		95,
		27,
		103,
		40,
		110
	};
	private static final int[] HEALERCLASSES =
	{
		16,
		97,
		30,
		105,
		43,
		112
	};
	private static final int[] ENCHANTERCLASSES =
	{
		17,
		98,
		21,
		100,
		34,
		107,
		51,
		115,
		52,
		116,
		135,
		136
	};
	
	private static final int COMMONITEM = 10280;
	private static final int ENHANCEDITEM = 10612;
	
	private static final int[] CLASSITEMS =
	{
		10281, // Warriors
		10282, // Knights
		10283, // Rogues
		10287, // Enchanters
		10284, // Wizards
		10286, // Summoners
		10285 // Healers
	};
	private static final int[] TRANSFORMITEMS =
	{
		10289, // Warriors
		10288, // Knights
		10290, // Rogues
		10293, // Enchanters
		10292, // Wizards
		10294, // Summoners
		10291 // Healers
	};
	
	public SubClassCertification(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		
		for (final int Id : NPC)
		{
			addStartNpc(Id);
			addTalkId(Id);
		}
	}
	
	private boolean isCorrectMaster(final L2Npc npc, final L2PcInstance player)
	{
		final Race currentRace = CharTemplateTable.getInstance().getTemplate(player.getClassId()).race;
		
		if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Human && (currentRace == Race.Human || currentRace == Race.Elf) && ((L2VillageMasterInstance) npc).getVillageMasterTeachType() == ClassType.Fighter && !player.isMageClass())
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Human && (currentRace == Race.Human || currentRace == Race.Elf) && ((L2VillageMasterInstance) npc).getVillageMasterTeachType() == ClassType.Mystic && ArrayUtils.isIntInArray(player.getClassId().getId(), WIZARDCLASSES))
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Human && (currentRace == Race.Human || currentRace == Race.Elf) && ((L2VillageMasterInstance) npc).getVillageMasterTeachType() == ClassType.Mystic && ArrayUtils.isIntInArray(player.getClassId().getId(), SUMMONERCLASSES))
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Human && (currentRace == Race.Human || currentRace == Race.Elf) && ((L2VillageMasterInstance) npc).getVillageMasterTeachType() == ClassType.Priest && ArrayUtils.isIntInArray(player.getClassId().getId(), HEALERCLASSES))
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Human && (currentRace == Race.Human || currentRace == Race.Elf) && ((L2VillageMasterInstance) npc).getVillageMasterTeachType() == ClassType.Priest && ArrayUtils.isIntInArray(player.getClassId().getId(), ENCHANTERCLASSES)
			&& player.isMageClass())
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.DarkElf && currentRace == Race.DarkElf)
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Orc && currentRace == Race.Orc)
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Dwarf && currentRace == Race.Dwarf)
			return true;
		else if (((L2VillageMasterInstance) npc).getVillageMasterRace() == Race.Kamael && currentRace == Race.Kamael)
			return true;
		return false;
	}
	
	private int getClassIndex(final L2PcInstance player)
	{
		if (ArrayUtils.isIntInArray(player.getClassId().getId(), WARRIORCLASSES))
			return 0;
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), KNIGHTCLASSES))
			return 1;
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), ROGUECLASSES))
			return 2;
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), ENCHANTERCLASSES))
			return 3;
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), WIZARDCLASSES))
			return 4;
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), SUMMONERCLASSES))
			return 5;
		else if (ArrayUtils.isIntInArray(player.getClassId().getId(), HEALERCLASSES))
			return 6;
		return -1;
	}
	
	private void getCertified(final L2PcInstance player, final int itemId, final String var)
	{
		final QuestState st = player.getQuestState(qn);
		final String qvar = st.getGlobalQuestVar(var);
		if (!qvar.equals("") && !qvar.equals("0"))
			return;
		
		final L2ItemInstance item = player.getInventory().addItem("Quest", itemId, 1, player, player.getTarget());
		st.saveGlobalQuestVar(var, "" + item.getObjectId());
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(item));
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		String htmltext = event;
		
		if (event.equals("GetCertified"))
		{
			if (player.isSubClassActive())
			{
				if (isCorrectMaster(npc, player))
				{
					if (player.getLevel() >= 65)
						return "CertificationList.htm";
					
					return "9002-08.htm";
				}
				return "9002-04.htm";
			}
			return "9002-03.htm";
		}
		
		else if (event.equals("Obtain65"))
		{
			final String html = "<html><body>Subclass Skill Certification:<br>You are trying to obtain level %level% certification of %class%, %skilltype%. Remember that once this subclass is certified, it cannot be re-certified -- even if you delete this class and develop another one -- without a special and expensive cancellation process.<br>Do you still want to be certified?<br><a action=\"bypass -h Quest SubClassCertification %event%\">Obtain certification.</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">Do not obtain certification.</a></body></html>";
			htmltext = html.replace("%level%", "65").replace("%class%", CharTemplateTable.getInstance().getClassNameById(player.getActiveClass())).replace("%skilltype%", "common skill").replace("%event%", "lvl65Emergent");
		}
		
		else if (event.equals("Obtain70"))
		{
			final String html = "<html><body>Subclass Skill Certification:<br>You are trying to obtain level %level% certification of %class%, %skilltype%. Remember that once this subclass is certified, it cannot be re-certified -- even if you delete this class and develop another one -- without a special and expensive cancellation process.<br>Do you still want to be certified?<br><a action=\"bypass -h Quest SubClassCertification %event%\">Obtain certification.</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">Do not obtain certification.</a></body></html>";
			
			htmltext = html.replace("%level%", "70").replace("%class%", CharTemplateTable.getInstance().getClassNameById(player.getActiveClass())).replace("%skilltype%", "common skill").replace("%event%", "lvl70Emergent");
		}
		
		else if (event.equals("Obtain75"))
		{
			final String html = "<html><body>Subclass Skill Certification:<br>You are trying to obtain level %level% certification of %class%, %skilltype%. Remember that once this subclass is certified, it cannot be re-certified -- even if you delete this class and develop another one -- without a special and expensive cancellation process.<br>Do you still want to be certified?<br><a action=\"bypass -h Quest SubClassCertification %event1%\">Obtain class specific skill certification.</a><br><a action=\"bypass -h Quest SubClassCertification %event2%\">Obtain master skill certification.</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">Do not obtain certification.</a></body></html>";
			
			htmltext = html.replace("%level%", "75").replace("%class%", CharTemplateTable.getInstance().getClassNameById(player.getActiveClass())).replace("%skilltype%", "common skill or special skill").replace("%event1%", "lvl75Class").replace("%event2%", "lvl75Master");
		}
		
		else if ("Obtain80".equals(event))
		{
			final String html = "<html><body>Subclass Skill Certification:<br>You are trying to obtain level %level% certification of %class%, %skilltype%. Remember that once this subclass is certified, it cannot be re-certified -- even if you delete this class and develop another one -- without a special and expensive cancellation process.<br>Do you still want to be certified?<br><a action=\"bypass -h Quest SubClassCertification %event%\">Obtain certification.</a><br><a action=\"bypass -h Quest SubClassCertification 9002-05.htm\">Do not obtain certification.</a></body></html>";
			htmltext = html.replace("%level%", "80").replace("%class%", CharTemplateTable.getInstance().getClassNameById(player.getActiveClass())).replace("%skilltype%", "transformation skill").replace("%event%", "lvl80Class");
		}
		
		else if (event.startsWith("lvl"))
		{
			final int level = Integer.parseInt(event.substring(3, 5));
			final String type = event.substring(5);
			final String prefix = "-" + player.getClassIndex();
			
			if (type.equals("Emergent"))
			{
				final String isAvailable65 = st.getGlobalQuestVar("EmergentAbility65" + prefix);
				final String isAvailable70 = st.getGlobalQuestVar("EmergentAbility70" + prefix);
				
				if (event.equals("lvl65Emergent"))
				{
					if (isAvailable65.equals("") || isAvailable65.equals("0"))
					{
						if (player.getLevel() > 64)
						{
							getCertified(player, COMMONITEM, "EmergentAbility" + level + prefix);
							return "9002-07.htm";
						}
						final String html = "<html><body>Сертификация Навыков Подкласса:<br>Вы еще не готовы получить свой %level% уровень сертификации. Трудитесь упорно и возвращайтесь позже.</body></html>";
						htmltext = html.replace("%level%", event.substring(3, 5));
						return htmltext;
						
					}
					return "9002-06.htm";
				}
				
				else if (event.equals("lvl70Emergent"))
				{
					if (isAvailable70.equals("") || isAvailable70.equals("0"))
					{
						if (player.getLevel() > 69)
						{
							getCertified(player, COMMONITEM, "EmergentAbility" + level + prefix);
							return "9002-07.htm";
						}
						final String html = "<html><body>Сертификация Навыков Подкласса:<br>Вы еще не готовы получить свой %level% уровень сертификации. Трудитесь упорно и возвращайтесь позже.</body></html>";
						htmltext = html.replace("%level%", event.substring(3, 5));
						return htmltext;
					}
					return "9002-06.htm";
				}
			}
			
			else if (type.equals("Master"))
			{
				final String isAvailable = st.getGlobalQuestVar("ClassAbility75" + prefix);
				if (isAvailable.equals("") || isAvailable.equals("0"))
				{
					if (player.getLevel() > 74)
					{
						getCertified(player, ENHANCEDITEM, "ClassAbility" + level + prefix);
						return "9002-07.htm";
					}
					final String html = "<html><body>Сертификация Навыков Подкласса:<br>Вы еще не готовы получить свой %level% уровень сертификации. Трудитесь упорно и возвращайтесь позже.</body></html>";
					htmltext = html.replace("%level%", event.substring(3, 5));
					return htmltext;
				}
				return "9002-06.htm";
			}
			
			else if (type.equals("Class"))
				if (level == 75)
				{
					final String isAvailable = st.getGlobalQuestVar("ClassAbility75" + prefix);
					if (isAvailable.equals("") || isAvailable.equals("0"))
					{
						if (player.getLevel() > 74)
						{
							getCertified(player, CLASSITEMS[getClassIndex(player)], "ClassAbility" + level + prefix);
							return "9002-07.htm";
						}
						final String html = "<html><body>Сертификация Навыков Подкласса:<br>Вы еще не готовы получить свой %level% уровень сертификации. Трудитесь упорно и возвращайтесь позже.</body></html>";
						htmltext = html.replace("%level%", event.substring(3, 5));
						return htmltext;
					}
					return "9002-06.htm";
				}
				
				else if (level == 80)
				{
					final String isAvailable = st.getGlobalQuestVar("ClassAbility80" + prefix);
					if (isAvailable.equals("") || isAvailable.equals("0"))
					{
						if (player.getLevel() > 79)
						{
							getCertified(player, TRANSFORMITEMS[getClassIndex(player)], "ClassAbility" + level + prefix);
							return "9002-07.htm";
						}
						final String html = "<html><body>Сертификация Навыков Подкласса:<br>Вы еще не готовы получить свой %level% уровень сертификации. Трудитесь упорно и возвращайтесь позже.</body></html>";
						htmltext = html.replace("%level%", event.substring(3, 5));
						return htmltext;
					}
					return "9002-06.htm";
				}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		st.set("cond", "0");
		st.setState(State.STARTED);
		return "9002-01.htm";
	}
	
	public static void main(final String[] args)
	{
		new SubClassCertification(-1, qn, "village_master");
	}
}