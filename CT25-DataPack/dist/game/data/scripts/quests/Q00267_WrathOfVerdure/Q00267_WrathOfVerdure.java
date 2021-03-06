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
package quests.Q00267_WrathOfVerdure;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Wrath of Verdure (267)
 * @author xban1x
 */
public final class Q00267_WrathOfVerdure extends Quest
{
	// NPC
	private static final int TREANT_BREMEC = 31853;
	// Item
	private static final int GOBLIN_CLUB = 1335;
	// Monster
	private static final int GOBLIN_RAIDER = 20325;
	// Reward
	private static final int SILVERY_LEAF = 1340;
	// Misc
	private static final int MIN_LVL = 4;

	private Q00267_WrathOfVerdure(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(TREANT_BREMEC);
		addTalkId(TREANT_BREMEC);
		addKillId(GOBLIN_RAIDER);
		registerQuestItems(GOBLIN_CLUB);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st != null)
			switch (event)
			{
				case "31853-04.htm":
				{
					st.startQuest();
					htmltext = event;
					break;
				}
				case "31853-07.html":
				{
					st.exitQuest(true, true);
					htmltext = event;
					break;
				}
				case "31853-08.html":
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
		if (st != null && getRandom(10) < 5)
		{
			st.giveItems(GOBLIN_CLUB, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st != null)
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = player.getRace() == Race.Elf ? player.getLevel() >= MIN_LVL ? "31853-03.htm" : "31853-02.htm" : "31853-01.htm";
					break;
				}
				case State.STARTED:
				{
					if (st.hasQuestItems(GOBLIN_CLUB))
					{
						final long count = st.getQuestItemsCount(GOBLIN_CLUB);
						st.rewardItems(SILVERY_LEAF, count);
						if (count >= 10)
							st.giveAdena(600, true);
						st.takeItems(GOBLIN_CLUB, -1);
						htmltext = "31853-06.html";
					}
					else
						htmltext = "31853-05.html";
					break;
				}
			}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00267_WrathOfVerdure(267, Q00267_WrathOfVerdure.class.getSimpleName(), "Wrath of Verdure");
	}
}
