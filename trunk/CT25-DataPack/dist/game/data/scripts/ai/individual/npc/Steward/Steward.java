/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual.npc.Steward;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

/**
 * Steward AI.
 * @author Browser
 */
public class Steward extends Quest
{
	// NPC
	private static final int STEWARD = 32029;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		if (npc.getNpcId() == STEWARD)
		{
			if (event.equalsIgnoreCase("check_condition"))
			{
				if (player.getLevel() >= 82)
				{
					player.teleToLocation(103045,-124361,-2768);
					htmltext = "";
				}
				else
					return "32029-1.htm";
			}
		}	
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == STEWARD)
			return "32029.htm";
		else
			npc.showChatWindow(player);
		
		return null;
	}
	
	private Steward(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(STEWARD);
		addFirstTalkId(STEWARD);
		addTalkId(STEWARD);
	}
	
	public static void main(String[] args)
	{
		new Steward(-1, Steward.class.getSimpleName(), "ai/individual/npc");
	}
}