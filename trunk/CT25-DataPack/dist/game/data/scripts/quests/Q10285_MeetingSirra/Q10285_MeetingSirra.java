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
package quests.Q10285_MeetingSirra;

import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q10284_AcquisitionOfDivineSword.Q10284_AcquisitionOfDivineSword;

/**
 * Meeting Sirra (10285)
 * @author Browser / Others
 */
public class Q10285_MeetingSirra extends Quest
{
	// NPC's
	private static final int RAFFORTY = 32020;
	private static final int STEWARD = 32029;
	private static final int JINIA = 32781;
	private static final int JINIA2 = 32760;
	private static final int KEGOR = 32761;
	private static final int SIRRA = 32762;

	// MISC
	private static final int MIN_LEVEL = 82;
	
	public Q10285_MeetingSirra(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA, JINIA2, KEGOR, SIRRA, STEWARD);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());

		if (st == null)
			return htmltext;
		
		if (npc.getId() == RAFFORTY)
		{
			if (event.equalsIgnoreCase("32020-05.htm"))
			{
				st.startQuest();
				st.setProgress(1);
				st.set("Ex", "0");
			}
		}

		else if (npc.getId() == JINIA2)
		{
			if (event.equalsIgnoreCase("32760-02.htm"))
			{
				st.set("Ex", "1");
				st.setCond(3, true);
			}
			
			else if (event.equalsIgnoreCase("32760-06.htm"))
			{
				st.set("Ex", "3");
				addSpawn(SIRRA, -23905, -8790, -5384, 56238, false, 0, false, npc.getInstanceId());
				st.setCond(5, true);
			}
			
			else if (event.equalsIgnoreCase("32760-12.htm"))
			{
				st.set("Ex", "5");
				st.setCond(7, true);
			}
			
			else if (event.equalsIgnoreCase("32760-14.htm"))
			{
				st.set("Ex", "0");
				st.setProgress(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				
				// destroy instance after 1 min
				final Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);
			}
		}
		
		else if (npc.getId() == KEGOR)
		{
			if (event.equalsIgnoreCase("32761-02.htm"))
			{
				st.set("Ex", "2");
				st.setCond(4, true);
			}
		}
		
		else if (npc.getId() == SIRRA)
		{
			if (event.equalsIgnoreCase("32762-08.htm"))
			{
				st.set("Ex", "4");
				st.setCond(6, true);
			}
		}
		
		else if (npc.getId() == STEWARD)
			if (event.equalsIgnoreCase("go"))
				if (player.getLevel() >= MIN_LEVEL)
				{
					player.teleToLocation(103045, -124361, -2768);
					htmltext = "";
				}
				else
					htmltext = "32029-01a";
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
					final QuestState _prev = player.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
					if (_prev != null && _prev.getState() == State.COMPLETED && player.getLevel() >= MIN_LEVEL)
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case State.STARTED:
					if (st.getProgress() == 1)
						htmltext = "32020-06.htm";
					else if (st.getProgress() == 2)
						htmltext = "32020-09.htm";
					else if (st.getProgress() == 3)
					{
						st.giveItems(57, 283425);
						st.addExpAndSp(939075, 83855);
						st.exitQuest(false, true);
						htmltext = "32020-10.htm";
					}
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
					return "32760-03.htm";
				case 2:
					return "32760-04.htm";
				case 3:
					return "32760-07.htm";
				case 4:
					return "32760-08.htm";
				case 5:
					return "32760-13.htm";
			}
		else if (npc.getId() == KEGOR && st.getProgress() == 1)
			switch (st.getInt("Ex"))
			{
				case 1:
					return "32761-01.htm";
				case 2:
					return "32761-03.htm";
				case 3:
					return "32761-04.htm";
			}
		else if (npc.getId() == SIRRA && st.getProgress() == 1)
			switch (st.getInt("Ex"))
			{
				case 3:
					return "32762-01.htm";
				case 4:
					return "32762-09.htm";
			}
		else if (npc.getId() == STEWARD && st.getProgress() == 2)
		{
			htmltext = "32029-01.htm";
			st.setCond(8, true);
		}
		
		else if (npc.getId() == JINIA && st.getProgress() == 2)
		{
			htmltext = "32781-01.htm";
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new Q10285_MeetingSirra(10285, Q10285_MeetingSirra.class.getSimpleName(), "Meeting Sirra");
	}
}