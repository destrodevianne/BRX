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
package quests.Q00694_BreakThroughTheHallOfSuffering;

import quests.Q10268_ToTheSeedOfInfinity.Q10268_ToTheSeedOfInfinity;
import quests.Q10273_GoodDayToFly.Q10273_GoodDayToFly;

import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * 
 * @author InsOmnia
 * Quest needed to obtain reward from Hall Of Suffering
 *
 */

public class Q00694_BreakThroughTheHallOfSuffering extends Quest
{
	
	// NPC
	private static final int TEPIOS = 32603;
	
	// Misc
	private static final int MIN_LV = 75;
	private static final int MAX_LV = 82;
	
	public Q00694_BreakThroughTheHallOfSuffering(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		QuestState qs = talker.getQuestState(getName());
		if (qs == null)
			qs = newQuestState(talker);
		QuestState reqQs = talker.getQuestState(Q10273_GoodDayToFly.class.getSimpleName());
		QuestState reqQs1 = talker.getQuestState(Q10268_ToTheSeedOfInfinity.class.getSimpleName());
		if(reqQs != null && reqQs.getState() == State.COMPLETED &&
				reqQs1 != null && reqQs1.getState() == State.COMPLETED)
		{
			long reentertime = InstanceManager.getInstance().getInstanceTime(talker.getObjectId(), 115);
			if (System.currentTimeMillis() >= reentertime)
			{
				if (qs.getCond() == 1)
					return "32603-3.htm";
				if (talker.getLevel() >= MIN_LV && talker.getLevel() <= MAX_LV)
					return "32603-0.htm";
				else
					return "32603-0a.htm";
			}
			else
			{
				if (qs.getState() == State.STARTED)
				{
					qs.unset("cond");
					qs.exitQuest(true);
				}
				return "32603-0b.htm";
			}
		}
		else
			return "32603-0a.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getName());
		if (qs == null)
			return null;
		if (event.equalsIgnoreCase("32603-3.htm"))
		{
			if (qs.getCond() != 1)
			{
				qs.startQuest();
			}
		}
		return event;
	}
	
	public static void main(String[] args)
	{
		new Q00694_BreakThroughTheHallOfSuffering(694, Q00694_BreakThroughTheHallOfSuffering.class.getSimpleName(), "Break Through The Hall Of Suffering");
	}
}