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
package quests.Q00122_OminousNews;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Ominous News (122)<br>
 * Original Jython script by Polo.
 * @author malyelfik
 */
public class Q00122_OminousNews extends Quest
{
	// NPCs
	private static final int MOIRA = 31979;
	private static final int KARUDA = 32017;

	public Q00122_OminousNews(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(MOIRA);
		addTalkId(MOIRA, KARUDA);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return getNoQuestMsg(player);

		switch (event)
		{
			case "31979-02.htm":
				st.startQuest();
				break;
			case "32017-02.html":
				st.giveAdena(8923, true);
				st.addExpAndSp(45151, 2310);
				st.exitQuest(false, true);
				break;
		}
		return event;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (npc.getId())
		{
			case MOIRA:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = player.getLevel() >= 20 ? "31979-01.htm" : "31979-00.htm";
						break;
					case State.STARTED:
						htmltext = "31979-03.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case KARUDA:
				if (st.isStarted())
					htmltext = "32017-01.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00122_OminousNews(122, Q00122_OminousNews.class.getSimpleName(), "Ominous News");
	}
}
