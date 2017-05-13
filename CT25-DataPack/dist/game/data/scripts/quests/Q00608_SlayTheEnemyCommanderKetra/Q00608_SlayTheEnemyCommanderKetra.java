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
package quests.Q00608_SlayTheEnemyCommanderKetra;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;

/**
 * Slay the Enemy Commander! (Ketra) (608)
 * @author malyelfik
 */
public class Q00608_SlayTheEnemyCommanderKetra extends Quest
{
	// NPC
	private static final int KADUN = 31370;
	// Monster
	private static final int MOS = 25312;
	// Items
	private static final int MOS_HEAD = 7236;
	private static final int WISDOM_TOTEM = 7220;
	private static final int KETRA_ALLIANCE_FOUR = 7214;
	// Misc
	private static final int MIN_LEVEL = 75;

	private Q00608_SlayTheEnemyCommanderKetra(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(KADUN);
		addTalkId(KADUN);
		addKillId(MOS);
		registerQuestItems(MOS_HEAD);
	}

	@Override
	public void actionForEachPlayer(final L2PcInstance player, final L2Npc npc, final boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && st.isCond(1) && Util.checkIfInRange(1500, npc, player, false))
		{
			st.giveItems(MOS_HEAD, 1);
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
			case "31370-04.htm":
				st.startQuest();
				break;
			case "31370-07.html":
				if (st.hasQuestItems(MOS_HEAD) && st.isCond(2))
				{
					st.giveItems(WISDOM_TOTEM, 1);
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
				htmltext = player.getLevel() >= MIN_LEVEL ? st.hasQuestItems(KETRA_ALLIANCE_FOUR) ? "31370-01.htm" : "31370-02.htm" : "31370-03.htm";
				break;
			case State.STARTED:
				htmltext = st.isCond(2) && st.hasQuestItems(MOS_HEAD) ? "31370-05.html" : "31370-06.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00608_SlayTheEnemyCommanderKetra(608, Q00608_SlayTheEnemyCommanderKetra.class.getSimpleName(), "Slay the Enemy Commander! (Ketra)");
	}
}