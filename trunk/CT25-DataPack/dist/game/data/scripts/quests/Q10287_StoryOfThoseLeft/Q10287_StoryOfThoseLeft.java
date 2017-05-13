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
package quests.Q10287_StoryOfThoseLeft;

import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q10286_ReunionWithSirra.Q10286_ReunionWithSirra;

/**
 * Story Of Those Left (10287)
 * @author Browser / Others
 */
public class Q10287_StoryOfThoseLeft extends Quest
{
	// NPC's
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	private static final int KEGOR = 32761;

	// MISC
	private static final int MIN_LEVEL = 82;
	
	public Q10287_StoryOfThoseLeft(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA, KEGOR);
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
			if (event.equalsIgnoreCase("32020-04.htm"))
			{
				st.startQuest(); // set cond "1" stat started playsound quest acepted
				st.setProgress(1);
				st.set("Ex1", "0");
				st.set("Ex2", "0");
			}

			else if (event.startsWith("reward_") && st.getProgress() == 2)
				try
				{
					final int itemId = Integer.parseInt(event.substring(7));
					
					if (itemId >= 10549 && itemId <= 10553 || itemId == 14219)
						st.giveItems(itemId, 1);

					st.exitQuest(false, true);
					htmltext = "32020-11.htm";
				}
				catch (final Exception e)
				{
					
				}
		}
		
		else if (npc.getId() == JINIA)
		{
			if (event.equalsIgnoreCase("32760-03.htm") && st.getProgress() == 1 && st.getInt("Ex1") == 0)
			{
				st.set("Ex1", "1");
				st.setCond(3, true);
			}
		}

		else if (npc.getId() == KEGOR)
			if (event.equalsIgnoreCase("32761-04.htm") && st.getProgress() == 1 && st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
			{
				st.set("Ex2", "1");
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
					final QuestState _prev = player.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
					if (_prev != null && _prev.getState() == State.COMPLETED && player.getLevel() >= MIN_LEVEL)
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-03.htm";
					break;
				case State.STARTED:
					if (st.getProgress() == 1)
						htmltext = "32020-05.htm";
					else if (st.getProgress() == 2)
						htmltext = "32020-09.htm";
					break;
				case State.COMPLETED:
					htmltext = "32020-02.htm";
					break;
			}
		else if (npc.getId() == JINIA && st.getProgress() == 1)
		{
			if (st.getInt("Ex1") == 0)
				return "32760-01.htm";

			else if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
				return "32760-04.htm";
			
			else if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 1)
			{
				st.setCond(5, true);
				st.setProgress(2);
				st.set("Ex1", "0");
				st.set("Ex2", "0");
				
				// destroy instance after 1 min
				final Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				inst.setDuration(60000);
				inst.setEmptyDestroyTime(0);

				return "32760-05.htm";
			}
			
		}

		else if (npc.getId() == KEGOR && st.getProgress() == 1)
			if (st.getInt("Ex1") == 1 && st.getInt("Ex2") == 0)
				htmltext = "32761-01.htm";

			else if (st.getInt("Ex1") == 0 && st.getInt("Ex2") == 0)
				htmltext = "32761-02.htm";
			
			else if (st.getInt("Ex2") == 1)
				htmltext = "32761-05.htm";
			
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new Q10287_StoryOfThoseLeft(10287, Q10287_StoryOfThoseLeft.class.getSimpleName(), "Story of Those Left");
	}
}