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
package quests.Q00170_DangerousSeduction;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.clientpackets.Say2;

/**
 * Dangerous Seduction (170)
 * @author malyelfik
 */
public class Q00170_DangerousSeduction extends Quest
{
	// NPC
	private static final int VELLIOR = 30305;

	// Monster
	private static final int MERKENIS = 27022;

	// Item
	private static final int NIGHTMARE_CRYSTAL = 1046;

	// Misc
	private static final int MIN_LEVEL = 21;

	public Q00170_DangerousSeduction(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(VELLIOR);
		addTalkId(VELLIOR);
		addKillId(MERKENIS);

		registerQuestItems(NIGHTMARE_CRYSTAL);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		if (event.equalsIgnoreCase("30305-04.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && st.isCond(1))
		{
			st.setCond(2, true);
			st.giveItems(NIGHTMARE_CRYSTAL, 1);
			npc.broadcastNpcSay(Say2.NPC_ALL, "Send my soul to Lich King Icarus...");
		}
		return super.onKill(npc, player, isPet);
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
				htmltext = player.getRace() == Race.DarkElf ? player.getLevel() >= MIN_LEVEL ? "30305-01.htm" : "30305-02.htm" : "30305-03.htm";
				break;
			case State.STARTED:
				if (st.isCond(1))
					htmltext = "30305-05.html";
				else
				{
					st.giveAdena(102680, true);
					st.addExpAndSp(38607, 4018);
					st.exitQuest(false, true);
					htmltext = "30305-06.html";
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00170_DangerousSeduction(170, Q00170_DangerousSeduction.class.getSimpleName(), "Dangerous Seduction");
	}
}