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
package quests.Q00614_SlayTheEnemyCommanderVarka;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;

/**
 * Slay the Enemy Commander! (Varka) (614)
 * @author malyelfik
 */
public class Q00614_SlayTheEnemyCommanderVarka extends Quest
{
	// NPC
	private static final int ASHAS = 31377;
	// Monster
	private static final int TAYR = 25302;
	// Items
	private static final int TAYR_HEAD = 7241;
	private static final int WISDOM_FEATHER = 7230;
	private static final int VARKA_ALLIANCE_FOUR = 7224;
	// Misc
	private static final int MIN_LEVEL = 75;

	private Q00614_SlayTheEnemyCommanderVarka(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(ASHAS);
		addTalkId(ASHAS);
		addKillId(TAYR);
		registerQuestItems(TAYR_HEAD);
	}

	@Override
	public void actionForEachPlayer(final L2PcInstance player, final L2Npc npc, final boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && st.isCond(1) && Util.checkIfInRange(1500, npc, player, false))
		{
			st.giveItems(TAYR_HEAD, 1);
			st.setCond(2, true);
		}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		String htmltext = event;
		switch (event)
		{
			case "31377-04.htm":
				st.startQuest();
				break;
			case "31377-07.html":
				if (st.hasQuestItems(TAYR_HEAD) && st.isCond(2))
				{
					st.giveItems(WISDOM_FEATHER, 1);
					st.addExpAndSp(10000, 0);
					st.exitQuest(true, true);
				}
				else
					htmltext = getNoQuestMsg(player);
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		executeForEachPlayer(killer, npc, isPet, true, false);
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
				htmltext = player.getLevel() >= MIN_LEVEL ? st.hasQuestItems(VARKA_ALLIANCE_FOUR) ? "31377-01.htm" : "31377-02.htm" : "31377-03.htm";
				break;
			case State.STARTED:
				htmltext = st.isCond(2) && st.hasQuestItems(TAYR_HEAD) ? "31377-05.html" : "31377-06.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00614_SlayTheEnemyCommanderVarka(614, Q00614_SlayTheEnemyCommanderVarka.class.getSimpleName(), "Slay the Enemy Commander! (Varka)");
	}
}