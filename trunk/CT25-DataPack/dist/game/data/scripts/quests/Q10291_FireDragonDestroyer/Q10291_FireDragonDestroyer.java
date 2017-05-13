package quests.Q10291_FireDragonDestroyer;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Fire Dragon Destroyer (10291)
 * @author malyelfik
 */
public class Q10291_FireDragonDestroyer extends Quest
{
	private static final String qn = "Q10291_FireDragonDestroyer";
	// NPC
	private static final int Klein = 31540;
	private static final int Valakas = 29028;
	// Item
	private static final int FloatingStone = 7267;
	private static final int PoorNecklace = 15524;
	private static final int ValorNecklace = 15525;
	private static final int ValakaSlayerCirclet = 8567;

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31540-07.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(PoorNecklace, 1);
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
				if (player.getLevel() >= 83 && st.getQuestItemsCount(FloatingStone) >= 1)
					htmltext = "31540-01.htm";
				else if (player.getLevel() < 83)
					htmltext = "31540-02.htm";
				else
					htmltext = "31540-04.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) >= 1)
					htmltext = "31540-08.htm";
				else if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) == 0)
				{
					st.giveItems(PoorNecklace, 1);
					htmltext = "31540-09.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					st.takeItems(ValorNecklace, 1);
					st.giveItems(57, 126549);
					st.addExpAndSp(717291, 77397);
					st.giveItems(ValakaSlayerCirclet, 1);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "31540-10.htm";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = "31540-03.htm";
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
			st.takeItems(PoorNecklace, 1);
			st.giveItems(ValorNecklace, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "2");
		}
	}

	public Q10291_FireDragonDestroyer(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Valakas);

		questItemIds = new int[]
		{
			PoorNecklace,
			ValorNecklace
		};
	}

	public static void main(final String[] args)
	{
		new Q10291_FireDragonDestroyer(10291, qn, "Fire Dragon Destroyer");
	}
}