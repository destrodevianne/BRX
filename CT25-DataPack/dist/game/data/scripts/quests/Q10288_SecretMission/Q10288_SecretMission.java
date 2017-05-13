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
package quests.Q10288_SecretMission;

import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 ** @author Gnacik 2010-08-07 Based on Freya PTS
 */

public class Q10288_SecretMission extends Quest
{
	// NPC's
	private static final int DOMINIC = 31350;
	private static final int AQUILANE = 32780;
	private static final int GREYMORE = 32757;
	// Items
	private static final int LETTER = 15529;

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());

		if (st == null)
			return htmltext;

		if (npc.getId() == DOMINIC)
		{
			if (event.equalsIgnoreCase("31350-05.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.giveItems(LETTER, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getId() == GREYMORE && event.equalsIgnoreCase("32757-03.htm"))
		{
			st.unset("cond");
			st.takeItems(LETTER, -1);
			st.giveItems(57, 106583);
			st.addExpAndSp(417788, 46320);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if (npc.getId() == AQUILANE)
			if (st.getState() == State.STARTED)
			{
				if (event.equalsIgnoreCase("32780-05.html"))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (st.getState() == State.COMPLETED && event.equalsIgnoreCase("teleport"))
			{
				player.teleToLocation(118833, -80589, -2688);
				return null;
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

		if (npc.getId() == DOMINIC)
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
						htmltext = "31350-01.htm";
					else
						htmltext = "31350-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "31350-06.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "31350-07.htm";
					break;
				case State.COMPLETED:
					htmltext = "31350-08.htm";
					break;
			}
		else if (npc.getId() == AQUILANE)
		{
			if (st.getInt("cond") == 1)
				htmltext = "32780-03.html";
			else if (st.getInt("cond") == 2)
				htmltext = "32780-06.html";
		}
		else if (npc.getId() == GREYMORE && st.getInt("cond") == 2)
			return "32757-01.htm";
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		if (npc.getId() == AQUILANE)
		{
			if (st.getState() == State.COMPLETED)
				return "32780-01.html";
			return "32780-00.html";
		}
		return null;
	}

	public Q10288_SecretMission(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(DOMINIC);
		addStartNpc(AQUILANE);
		addTalkId(DOMINIC);
		addTalkId(GREYMORE);
		addTalkId(AQUILANE);
		addFirstTalkId(AQUILANE);
	}

	public static void main(final String[] args)
	{
		new Q10288_SecretMission(10288, Q10288_SecretMission.class.getSimpleName(), "Secret Mission");
	}
}