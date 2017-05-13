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
package quests.Q00272_WrathOfAncestors;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Wrath of Ancestors (272)
 * @author xban1x
 */
public final class Q00272_WrathOfAncestors extends Quest
{
	// NPC
	private static final int LIVINA = 30572;
	// Items
	private static final int GRAVE_ROBBERS_HEAD = 1474;
	// Monsters
	private static final int[] MONSTERS = new int[]
	{
		20319, // Goblin Grave Robber
		20320, // Goblin Tomb Raider Leader
	};
	// Misc
	private static final int MIN_LVL = 5;

	private Q00272_WrathOfAncestors(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(LIVINA);
		addTalkId(LIVINA);
		addKillId(MONSTERS);
		registerQuestItems(GRAVE_ROBBERS_HEAD);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equalsIgnoreCase("30572-04.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && st.isCond(1))
		{
			st.giveItems(GRAVE_ROBBERS_HEAD, 1);
			if (st.getQuestItemsCount(GRAVE_ROBBERS_HEAD) >= 50)
				st.setCond(2, true);
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st != null)
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = player.getRace() == Race.Orc ? player.getLevel() >= MIN_LVL ? "30572-03.htm" : "30572-02.htm" : "30572-01.htm";
					break;
				}
				case State.STARTED:
				{
					switch (st.getCond())
					{
						case 1:
						{
							htmltext = "30572-05.html";
							break;
						}
						case 2:
						{
							st.giveAdena(1500, true);
							st.exitQuest(true, true);
							htmltext = "30572-06.html";
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
		new Q00272_WrathOfAncestors(272, Q00272_WrathOfAncestors.class.getSimpleName(), "Wrath of Ancestors");
	}
}