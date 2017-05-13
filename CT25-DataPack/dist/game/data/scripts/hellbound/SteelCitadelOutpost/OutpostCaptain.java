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
package hellbound.SteelCitadelOutpost;

import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public class OutpostCaptain extends Quest
{
	// Npcs
	private static final int CAPTAIN = 18466;
	private static final int[] DEFENDERS =
	{
		22357,
		22358
	};
	private static final int DOORKEEPER = 32351;
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("level_up"))
		{
			npc.deleteMe();
			HellboundManager.getInstance().setLevel(9);
		}
		return null;
	}
	
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (HellboundManager.getInstance().getLevel() == 8)
			addSpawn(DOORKEEPER, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0, false);
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		npc.setIsNoRndWalk(true);

		if (npc.getId() == CAPTAIN)
		{
			final L2DoorInstance door = DoorTable.getInstance().getDoor(20250001);
			if (door != null)
				door.closeMe();
		}

		else if (npc.getId() == DOORKEEPER)
			startQuestTimer("level_up", 3000, npc, null);
		
		return super.onSpawn(npc);
	}
	
	public OutpostCaptain(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addKillId(CAPTAIN);
		addSpawnId(CAPTAIN, DOORKEEPER);

		for (final int i : DEFENDERS)
			addSpawnId(i);
	}
	
	public static void main(final String[] args)
	{
		new OutpostCaptain(-1, OutpostCaptain.class.getSimpleName(), "hellbound");
	}
}