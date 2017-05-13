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
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * Originally written by RosT in python
 * @author Evilus
 */
public class WhiteAllosce extends L2AttackableAIScript
{
	// Npcs
	private static final int ALLOSCE = 18577;
	private static final int GUARD = 18578;
	
	// Constant
	private boolean _isLock = false;
	
	public WhiteAllosce(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(ALLOSCE);
		addKillId(ALLOSCE);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final int x = player.getX();
		final int y = player.getY();
		
		if (event.equalsIgnoreCase("time_to_spawn"))
		{
			addSpawn(GUARD, x + 100, y + 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			_isLock = false;
		}
		
		return null;
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet, final L2Skill skill)
	{
		final int npcId = npc.getId();
		
		if (npcId == ALLOSCE)
			if (_isLock == false)
			{
				startQuestTimer("time_to_spawn", 40000, npc, player);
				_isLock = true;
			}
			else
				return null;
			
		return null;
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final int npcId = npc.getId();
		
		if (npcId == ALLOSCE)
			cancelQuestTimer("time_to_spawn", npc, player);
		
		return null;
	}
	
	public static void main(final String[] args)
	{
		new WhiteAllosce(-1, WhiteAllosce.class.getSimpleName(), "ai/individual/kamaloka");
	}
}
