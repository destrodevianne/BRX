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

	// Misc
	private static final int MIN_LV = 82;

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		switch (event)
		{
			case "check_condition":
			{
				if (player.getLevel() >= MIN_LV)
				{
					player.teleToLocation(103045, -124361, -2768);
					htmltext = "";
				}
				else
					return "32029-1.htm";
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		switch (npc.getId())
		{
			case STEWARD:
			{
				return "32029-0.htm";
			}
		}
		return null;
	}

	private Steward(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(STEWARD);
		addFirstTalkId(STEWARD);
		addTalkId(STEWARD);
	}

	public static void main(final String[] args)
	{
		new Steward(-1, Steward.class.getSimpleName(), "ai/individual/npc");
	}
}