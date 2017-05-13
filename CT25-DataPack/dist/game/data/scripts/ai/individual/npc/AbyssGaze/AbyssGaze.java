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
	
	// Locations
	private static final Location[] _locs =
	{
		new Location(-187567, 205570, -9538),
		new Location(-179659, 211061, -12784),
		new Location(-179284, 205990, -15520)
	};
	
	public AbyssGaze(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(ABYSSGAZE);
		addFirstTalkId(ABYSSGAZE);
		addTalkId(ABYSSGAZE);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		switch (event)
		{
			case "check_stage":
			{
				if (GraciaSeedsManager.getInstance().getSoIState() == 2)
					htmltext = "32540-2.htm";
				else if (GraciaSeedsManager.getInstance().getSoIState() == 3)
					htmltext = "32540-1.htm";
				else if (GraciaSeedsManager.getInstance().getSoIState() == 5)
					htmltext = "32540-4.htm";
				else
					htmltext = "32540-3.htm";
				break;
			}

			case "leave":
			{
				player.teleToLocation(-212832, 209822, 4288);
				htmltext = "";
				break;
			}

			case "enter_seed":
			{
				if (GraciaSeedsManager.getInstance().getSoIState() == 3)
				{
					player.teleToLocation(_locs[getRandom(0, 2)], true);
					return null;
				}
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (npc.getId() == ABYSSGAZE)
			return "32540-0.htm";
		return null;
	}

	public static void main(final String[] args)
	{
		new AbyssGaze(-1, AbyssGaze.class.getSimpleName(), "ai/individual/npc");
	}
}