package quests.Q10290_LandDragonConqueror;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Land Dragon Conqueror (10290)
 * @author malyelfik
 */
public class Q10290_LandDragonConqueror extends Quest
{
	private static final String qn = "Q10290_LandDragonConqueror";
	// NPC
	private static final int Theodoric = 30755;
	private static final int[] Antharas =
	{
		29019,
		29066,
		29067,
		29068
	}; // Old, Weak, Normal, Strong
	// Item
	private static final int PortalStone = 3865;
	private static final int ShabbyNecklace = 15522;
	private static final int MiracleNecklace = 15523;
	private static final int AntharaSlayerCirclet = 8568;

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30755-07.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(ShabbyNecklace, 1);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() >= 83 && st.getQuestItemsCount(PortalStone) >= 1)
					htmltext = "30755-01.htm";
				else if (player.getLevel() < 83)
					htmltext = "30755-02.htm";
				else
					htmltext = "30755-04.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(ShabbyNecklace) >= 1)
					htmltext = "30755-08.htm";
				else if (st.getInt("cond") == 1 && st.getQuestItemsCount(ShabbyNecklace) == 0)
				{
					st.giveItems(ShabbyNecklace, 1);
					htmltext = "30755-09.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					st.takeItems(MiracleNecklace, 1);
					st.giveItems(57, 131236);
					st.addExpAndSp(702557, 76334);
					st.giveItems(AntharaSlayerCirclet, 1);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "30755-10.htm";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = "30755-03.htm";
				break;
			}
		}

		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (player.getParty() != null)
			for (final L2PcInstance partyMember : player.getParty().getPartyMembers())
				rewardPlayer(partyMember);
		else
			rewardPlayer(player);
		return null;
	}

	private void rewardPlayer(final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);

		if (st != null && st.getInt("cond") == 1)
		{
			st.takeItems(ShabbyNecklace, 1);
			st.giveItems(MiracleNecklace, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "2");
		}
	}

	public Q10290_LandDragonConqueror(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(Theodoric);
		addTalkId(Theodoric);
		for (final int i : Antharas)
			addKillId(i);

		questItemIds = new int[]
		{
			MiracleNecklace,
			ShabbyNecklace
		};
	}

	public static void main(final String[] args)
	{
		new Q10290_LandDragonConqueror(10290, qn, "Land Dragon Conqueror");
	}
}