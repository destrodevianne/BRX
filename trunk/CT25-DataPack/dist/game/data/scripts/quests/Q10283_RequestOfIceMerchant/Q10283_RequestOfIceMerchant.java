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
package quests.Q10283_RequestOfIceMerchant;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00115_TheOtherSideOfTruth.Q00115_TheOtherSideOfTruth;

/**
 ** @author Gnacik 2010-08-07 Based on Freya PTS update 12/06/2014 by @Browser
 */

public class Q10283_RequestOfIceMerchant extends Quest
{
	// NPC's
	private static final int RAFFORTY = 32020;
	private static final int KIER = 32022;
	private static final int JINIA = 32760;

	public Q10283_RequestOfIceMerchant(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
		addTalkId(KIER);
		addFirstTalkId(JINIA);
		addTalkId(JINIA);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());

		if (st == null)
			return htmltext;

		if (npc.getId() == RAFFORTY)
		{
			if (event.equalsIgnoreCase("32020-03.htm"))
				st.startQuest();
			else if (event.equalsIgnoreCase("32020-07.htm"))
				st.setCond(2, true);
		}
		else if (npc.getId() == KIER && event.equalsIgnoreCase("spawn"))
		{
			addSpawn(JINIA, 104322, -107669, -3680, 44954, false, 60000);
			return null;
		}
		else if (npc.getId() == JINIA && event.equalsIgnoreCase("32760-04.html"))
		{
			st.giveItems(57, 190000);
			st.addExpAndSp(627000, 50300);
			st.exitQuest(false, true);
			npc.deleteMe();
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

		if (npc.getId() == RAFFORTY)
			switch (st.getState())
			{
				case State.CREATED:
					final QuestState _prev = player.getQuestState(Q00115_TheOtherSideOfTruth.class.getSimpleName());
					if (_prev != null && _prev.getState() == State.COMPLETED && player.getLevel() >= 82)
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-00.htm";
					break;
				case State.STARTED:
					if (st.getCond() == 1)
						htmltext = "32020-04.htm";
					else if (st.getCond() == 2)
						htmltext = "32020-08.htm";
					break;
				case State.COMPLETED:
					htmltext = getAlreadyCompletedMsg(player);
					break;
			}
		else if (npc.getId() == KIER && st.getCond() == 2)
			htmltext = "32022-01.html";
		else if (npc.getId() == JINIA && st.getCond() == 2)
			htmltext = "32760-02.html";
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;
		if (npc.getId() == JINIA && st.getCond() == 2)
			return "32760-01.html";

		npc.showChatWindow(player);
		return null;
	}

	public static void main(final String[] args)
	{
		new Q10283_RequestOfIceMerchant(10283, Q10283_RequestOfIceMerchant.class.getSimpleName(), "Request of Ice Merchant");
	}
}
