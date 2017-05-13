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
package quests.Q00110_ToThePrimevalIsle;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * To the Primeval Isle (110)
 * @author Adry_85
 */
public class Q00110_ToThePrimevalIsle extends Quest
{
	// NPCs
	private static final int ANTON = 31338;
	private static final int MARQUEZ = 32113;
	// Item
	private static final int ANCIENT_BOOK = 8777;

	public Q00110_ToThePrimevalIsle(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		addStartNpc(ANTON);
		addTalkId(ANTON, MARQUEZ);
		registerQuestItems(ANCIENT_BOOK);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return getNoQuestMsg(player);

		switch (event)
		{
			case "31338-1.html":
				st.giveItems(ANCIENT_BOOK, 1);
				st.startQuest();
				break;
			case "32113-2.html":
			case "32113-2a.html":
				st.giveAdena(191678, true);
				st.addExpAndSp(251602, 25245);
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
			case ANTON:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = player.getLevel() < 75 ? "31338-0a.htm" : "31338-0b.htm";
						break;
					case State.STARTED:
						htmltext = "31338-1a.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case MARQUEZ:
				if (st.isCond(1))
					htmltext = "32113-1.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00110_ToThePrimevalIsle(110, Q00110_ToThePrimevalIsle.class.getSimpleName(), "To the Primeval Isle");
	}
}
