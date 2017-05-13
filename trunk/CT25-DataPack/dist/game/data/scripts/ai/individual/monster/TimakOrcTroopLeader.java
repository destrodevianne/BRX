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
package ai.individual.monster;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author InsOmnia
 */
public class TimakOrcTroopLeader extends L2AttackableAIScript
{
	// Npc
	private static final int TimakOrcTroopLeader = 20767;
	
	public TimakOrcTroopLeader(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(TimakOrcTroopLeader);
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		if (npc.getId() == TimakOrcTroopLeader)
			if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				if (getRandom(100) > 90)
					npc.broadcastNpcSay("Destroy the enemy, my brothers!");
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(final String[] args)
	{
		new TimakOrcTroopLeader(-1, TimakOrcTroopLeader.class.getSimpleName(), "ai/individual/monster");
	}
}