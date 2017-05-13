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
package quests.Q00297_GatekeepersFavor;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Gatekeeper's Favor (297)
 * @author malyelfik
 */
public class Q00297_GatekeepersFavor extends Quest
{
	// NPC
	private static final int WIRPHY = 30540;
	// Monster
	private static final int WHINSTONE_GOLEM = 20521;
	// Items
	private static final int STARSTONE = 1573;
	private static final int GATEKEEPER_TOKEN = 1659;
	// Misc
	private static final int MIN_LEVEL = 15;
	private static final int STARSTONE_COUT = 20;

	private Q00297_GatekeepersFavor(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(WIRPHY);
		addTalkId(WIRPHY);
		addKillId(WHINSTONE_GOLEM);
		registerQuestItems(STARSTONE);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equalsIgnoreCase("30540-03.htm"))
		{
			if (player.getLevel() < MIN_LEVEL)
				return "30540-01.htm";
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && st.isStarted() && st.getQuestItemsCount(STARSTONE) < STARSTONE_COUT)
		{
			st.giveItems(STARSTONE, 1);
			if (st.getQuestItemsCount(STARSTONE) >= STARSTONE_COUT)
				st.setCond(2, true);
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
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
				htmltext = "30540-02.htm";
				break;
			case State.STARTED:
				if (st.isCond(1))
					htmltext = "30540-04.html";
				else if (st.isCond(2) && st.getQuestItemsCount(STARSTONE) >= STARSTONE_COUT)
				{
					st.giveItems(GATEKEEPER_TOKEN, 2);
					st.exitQuest(true, true);
					htmltext = "30540-05.html";
				}
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00297_GatekeepersFavor(297, Q00297_GatekeepersFavor.class.getSimpleName(), "Gatekeeper's Favor");
	}
}