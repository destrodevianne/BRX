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
package ai.individual.kamaloka;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * Originally written by RosT in py
 * @author Evilus
 */
public class SeerFlouros extends L2AttackableAIScript
{
	// Npcs
	private static final int SEER = 18559;
	private static final int GUARD = 18560;
	
	// Constant
	private int isGuardSpawn = 0;
	
	public SeerFlouros(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(SEER);
		addKillId(GUARD, SEER);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final int x = player.getX();
		final int y = player.getY();
		
		if (event.equalsIgnoreCase("time_to_spawn"))
		{
			if (isGuardSpawn == 0)
			{
				addSpawn(GUARD, x, y, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				isGuardSpawn = 1;
			}
			else if (isGuardSpawn == 1)
			{
				addSpawn(GUARD, x, y, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				isGuardSpawn = 2;
			}
			else if (isGuardSpawn == 2)
				return null;
		}
		else if (event.equalsIgnoreCase("time_to_spawn1"))
			if (isGuardSpawn == 1)
			{
				addSpawn(GUARD, x, y, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				isGuardSpawn = 2;
			}
			else if (isGuardSpawn == 2)
				return null;
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public final String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (npcId == SEER)
			if (isGuardSpawn == 0)
				startQuestTimer("time_to_spawn", 30000, npc, player);
			else if (isGuardSpawn == 1)
				startQuestTimer("time_to_spawn1", 60000, npc, player);
		return super.onAttack(npc, player, damage, isPet);
		
	}
	
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (npcId == GUARD)
		{
			if (isGuardSpawn == 0)
				startQuestTimer("time_to_spawn", 30000, npc, player);
			else if (isGuardSpawn == 1)
			{
				startQuestTimer("time_to_spawn1", 60000, npc, player);
				isGuardSpawn -= 1;
			}
			else if (isGuardSpawn == 2)
			{
				isGuardSpawn -= 1;
				startQuestTimer("time_to_spawn1", 60000, npc, player);
			}
		}
		else if (npcId == SEER)
		{
			cancelQuestTimer("time_to_spawn", npc, player);
			cancelQuestTimer("time_to_spawn1", npc, player);
		}
		return super.onKill(npc, player, isPet);
	}
	
	public static void main(final String[] args)
	{
		new SeerFlouros(-1, SeerFlouros.class.getSimpleName(), "ai/individual/kamaloka");
	}
}
