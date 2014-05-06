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
package ai.individual.npc.AbyssGaze;

import ct25.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

/**
 * @author Browser
 */
public class AbyssGaze extends Quest
{
	// NPC
	private static final int ABYSSGAZE = 32540;
		
	//Locations
	private static final Location[] _locs = {new Location(-187567, 205570, -9538)/*HOS*/, 
		new Location(-179659, 211061, -12784)/*HOE*/, 
		new Location(-179284,205990,-15520)/*HOI*/};
	
	//Misc
	private static final int MIN_LEVEL = 75;
		
	public AbyssGaze(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ABYSSGAZE);
		addFirstTalkId(ABYSSGAZE);
		addTalkId(ABYSSGAZE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		  {
			if (player.getLevel() >= MIN_LEVEL)
			{
				if (event.equalsIgnoreCase("request_permission"))
				{
					if ((GraciaSeedsManager.getInstance().getSoIState() == 2) || (GraciaSeedsManager.getInstance().getSoIState() == 5))
					{
						htmltext = "32540-2.htm";
					}
					else if ((GraciaSeedsManager.getInstance().getSoIState() == 3) || (GraciaSeedsManager.getInstance().getSoIState() == 4))
					{
						htmltext = "32540-1.htm";
					}
					else
					{
						htmltext = "32540-3.htm";
					}
				}
				
				else if (event.equalsIgnoreCase("leave"))
				{
					player.teleToLocation(-212832, 209822, 4288);
					htmltext = "";
				}
				
				else if (event.equalsIgnoreCase("enter_seed"))
				{
					if (GraciaSeedsManager.getInstance().getSoIState() == 3)
						player.teleToLocation(_locs[getRandom(0, 2)], true);//HOS,HOE,HOI
					else if (GraciaSeedsManager.getInstance().getSoIState() == 4)
						player.teleToLocation(_locs[2], true);//HOI
					
					return null;
				}
				
				else if (event.equalsIgnoreCase("request_ekimus"))
				{
					if (GraciaSeedsManager.getInstance().getSoIState() == 2)
					{
						htmltext = "32540-4.htm";
					}
					else if (GraciaSeedsManager.getInstance().getSoIState() == 5)
					{
						htmltext = "32540-5.htm";
					}
					return null;
				}
			}
			else
			{
				htmltext = "32540-3.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		if (npc.getId() == ABYSSGAZE)
			return "32540.htm";
		else
			npc.showChatWindow(player);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new AbyssGaze(-1, AbyssGaze.class.getSimpleName(), "ai/individual/npc");
	}
}