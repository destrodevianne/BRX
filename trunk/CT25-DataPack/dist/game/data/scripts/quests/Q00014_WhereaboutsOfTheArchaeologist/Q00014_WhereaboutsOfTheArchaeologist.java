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
package quests.Q00014_WhereaboutsOfTheArchaeologist;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Whereabouts of the Archaeologist (14)<br>
 * Original Jython script by disKret.
 * @author nonom
 */
public class Q00014_WhereaboutsOfTheArchaeologist extends Quest
{
	// NPCs
	private static final int LIESEL = 31263;
	private static final int GHOST_OF_ADVENTURER = 31538;
	// Item
	private static final int LETTER = 7253;

	private Q00014_WhereaboutsOfTheArchaeologist(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(LIESEL);
		addTalkId(LIESEL, GHOST_OF_ADVENTURER);
		registerQuestItems(LETTER);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (event)
		{
			case "31263-02.html":
				st.startQuest();
				st.giveItems(LETTER, 1);
				break;
			case "31538-01.html":
				if (st.isCond(1) && st.hasQuestItems(LETTER))
				{
					st.giveAdena(136928, true);
					st.addExpAndSp(325881, 32524);
					st.exitQuest(false, true);
				}
				else
					htmltext = "31538-02.html";
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		final int npcId = npc.getId();
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == LIESEL)
					htmltext = player.getLevel() < 74 ? "31263-01.html" : "31263-00.htm";
				break;
			case State.STARTED:
				if (st.isCond(1))
					switch (npcId)
					{
						case LIESEL:
							htmltext = "31263-02.html";
							break;
						case GHOST_OF_ADVENTURER:
							htmltext = "31538-00.html";
							break;
					}
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00014_WhereaboutsOfTheArchaeologist(14, Q00014_WhereaboutsOfTheArchaeologist.class.getSimpleName(), "Whereabouts of the Archaeologist");
	}
}
