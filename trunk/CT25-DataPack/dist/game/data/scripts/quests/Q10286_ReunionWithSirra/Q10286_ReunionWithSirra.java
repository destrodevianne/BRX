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
package quests.Q10286_ReunionWithSirra;

import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q10285_MeetingSirra.Q10285_MeetingSirra;

/**
 * Reunion With Sirra (10286)
 * @author Browser / Others
 */
public class Q10286_ReunionWithSirra extends Quest
{
	// NPC's
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32781;
	private static final int JINIA2 = 32760;
	private static final int SIRRA = 32762;

	// ITEM's
	private static final int BLACK_FROZEN_CORE = 15470;

	// MISC
	private static final int MIN_LEVEL = 82;
	
	public Q10286_ReunionWithSirra(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA, JINIA2, SIRRA);
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
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.startQuest();
				st.setProgress(1);
			}

			else if (event.equalsIgnoreCase("32020-05.htm") && st.getProgress() == 1)
				st.set("Ex", "0");
		}

		else if (npc.getId() == JINIA2)
		{
			if (event.equalsIgnoreCase("32760-06.htm"))
			{
				addSpawn(SIRRA, -23905, -8790, -5384, 56238, false, 0, false, npc.getInstanceId());
				st.set("Ex", "1");
				st.setCond(3, true);
			}

			else if (event.equalsIgnoreCase("32760-09.htm") && st.getProgress() == 1 && st.getInt("Ex") == 2)
			{
				st.setProgress(2);
				// destroy instance after 1 min
				final Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
			}
		}
		
		else if (npc.getId() == SIRRA)
			if (event.equalsIgnoreCase("32762-04.htm") && st.getProgress() == 1 && st.getInt("Ex") == 1)
			{
				if (st.getQuestItemsCount(BLACK_FROZEN_CORE) == 0)
					st.giveItems(BLACK_FROZEN_CORE, 5);
				
				st.set("Ex", "2");
				st.setCond(4, true);
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
					final QuestState _prev = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
					if (_prev != null && _prev.getState() == State.COMPLETED && player.getLevel() >= MIN_LEVEL)
						htmltext = "32020-00.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case State.STARTED:
					if (st.getProgress() == 1)
						htmltext = "32020-06.htm";
					else if (st.getProgress() == 2)
						htmltext = "32020-09.htm";
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		else if (npc.getId() == JINIA2 && st.getProgress() == 1)
			switch (st.getInt("Ex"))
			{
				case 0:
					return "32760-01.htm";
				case 1:
					return "32760-07.htm";
				case 2:
					return "32760-08.htm";
			}
		else if (npc.getId() == SIRRA && st.getProgress() == 1)
			switch (st.getInt("Ex"))
			{
				case 1:
					return "32762-01.htm";
				case 2:
					return "32762-05.htm";
			}
		else if (npc.getId() == JINIA && st.getProgress() == 3)
		{
			st.addExpAndSp(2152200, 181070);
			st.exitQuest(false, true);
			htmltext = "32781-08.htm";
		}
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new Q10286_ReunionWithSirra(10286, Q10286_ReunionWithSirra.class.getSimpleName(), "Reunion With Sirra");
	}
}