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
package hellbound.Desert;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;

public class Sandstorm extends L2AttackableAIScript
{
	// Npc
	private static final int Sandstorm = 32350;

	// Skill
	private static SkillHolder GUST = new SkillHolder(5435, 1);

	public Sandstorm(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		super.addAttackId(Sandstorm);
	}
	
	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final int npcId = npc.getId();
		
		if (npcId == Sandstorm)
		{
			npc.setTarget(player);
			npc.doCast(GUST.getSkill());
		}
		
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public static void main(final String[] args)
	{
		new Sandstorm(-1, Sandstorm.class.getSimpleName(), "hellbound");
	}
}