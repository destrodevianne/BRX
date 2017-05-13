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
package quests.Q00157_RecoverSmuggledGoods;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Recover Smuggled Goods (157)
 * @author xban1x
 */
public class Q00157_RecoverSmuggledGoods extends Quest
{
	// NPC
	private static final int WILFORD = 30005;
	// Monster
	private static final int GIANT_TOAD = 20121;
	// Items
	private static final int BUCKLER = 20;
	private static final int ADAMANTITE_ORE = 1024;
	// Misc
	private static final int MIN_LVL = 5;

	public Q00157_RecoverSmuggledGoods(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(WILFORD);
		addTalkId(WILFORD);
		addKillId(GIANT_TOAD);
		registerQuestItems(ADAMANTITE_ORE);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st != null)
			switch (event)
			{
				case "30005-03.htm":
				{
					htmltext = event;
					break;
				}
				case "30005-04.htm":
				{
					st.startQuest();
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
		if (st != null && st.isCond(1) && getRandom(10) < 4 && st.getQuestItemsCount(ADAMANTITE_ORE) < 20)
		{
			st.giveItems(ADAMANTITE_ORE, 1);
			if (st.getQuestItemsCount(ADAMANTITE_ORE) >= 20)
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
		String htmltext = getNoQuestMsg(player);
		if (st != null)
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = player.getLevel() >= MIN_LVL ? "30005-02.htm" : "30005-01.htm";
					break;
				}
				case State.STARTED:
				{
					if (st.isCond(2) && st.getQuestItemsCount(ADAMANTITE_ORE) >= 20)
					{
						st.giveItems(BUCKLER, 1);
						st.exitQuest(false, true);
						htmltext = "30005-06.html";
					}
					else
						htmltext = "30005-05.html";
					break;
				}
				case State.COMPLETED:
				{
					htmltext = getAlreadyCompletedMsg(player);
					break;
				}
			}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00157_RecoverSmuggledGoods(157, Q00157_RecoverSmuggledGoods.class.getSimpleName(), "Recover Smuggled Goods");
	}
}
