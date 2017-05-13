/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00313_CollectSpores;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;

/**
 * Collect Spores (313)
 * @author ivantotov
 */
public final class Q00313_CollectSpores extends Quest
{
	// NPC
	private static final int HERBIEL = 30150;
	// Item
	private static final int SPORE_SAC = 1118;
	// Misc
	private static final int MIN_LEVEL = 8;
	private static final int REQUIRED_SAC_COUNT = 10;
	// Monster
	private static final int SPORE_FUNGUS = 20509;

	private Q00313_CollectSpores(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(HERBIEL);
		addTalkId(HERBIEL);
		addKillId(SPORE_FUNGUS);
		registerQuestItems(SPORE_SAC);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;
		String htmltext = null;
		switch (event)
		{
			case "30150-05.htm":
			{
				if (st.isCreated())
				{
					st.startQuest();
					htmltext = event;
				}
				break;
			}
			case "30150-04.htm":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && st.isCond(1) && Util.checkIfInRange(1500, npc, killer, false))
			if (st.giveItemRandomly(npc, SPORE_SAC, 1, REQUIRED_SAC_COUNT, 0.4, true))
				st.setCond(2);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getLevel() >= MIN_LEVEL ? "30150-03.htm" : "30150-02.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						if (st.getQuestItemsCount(SPORE_SAC) < REQUIRED_SAC_COUNT)
							htmltext = "30150-06.html";
						break;
					}
					case 2:
					{
						if (st.getQuestItemsCount(SPORE_SAC) >= REQUIRED_SAC_COUNT)
						{
							st.giveAdena(3500, true);
							st.exitQuest(true, true);
							htmltext = "30150-07.html";
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00313_CollectSpores(313, Q00313_CollectSpores.class.getSimpleName(), "Collect Spores");
	}
}