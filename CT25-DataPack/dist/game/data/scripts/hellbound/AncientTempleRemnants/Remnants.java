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
package hellbound.AncientTempleRemnants;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author DS
 */
public class Remnants extends L2AttackableAIScript
{
	// Npcs
	private static final int DEREK = 18465;
	private static final int[] NPCS =
	{
		18463,
		18464,
		18465
	};

	// Item
	private static final int HOLY_WATER = 2358;

	// String Msgs
	private static final String MSG = "The holy water affects Remnants Ghost. You have freed his soul.";
	private static final String MSG_DEREK = "The holy water affects Derek. You have freed his soul.";

	@Override
	public final String onSpawn(final L2Npc npc)
	{
		npc.setIsMortal(false);
		return super.onSpawn(npc);
	}

	@Override
	public final String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (skill.getId() == HOLY_WATER)
			if (!npc.isDead())
				if (targets.length > 0 && targets[0] == npc)
					if (npc.getCurrentHp() < npc.getMaxHp() * 0.02) // Lower, than 2%
					{
						npc.doDie(caster);
						
						if (npc.getId() == DEREK)
							caster.sendMessage(MSG_DEREK);
						else
							caster.sendMessage(MSG);
					}
				
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	// Do not override onKill for Derek here. Let's make global Hellbound manipulations in Engine where it is possible.

	public Remnants(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int npcId : NPCS)
		{
			addSpawnId(npcId);
			addSkillSeeId(npcId);
		}
	}

	public static void main(final String[] args)
	{
		new Remnants(-1, Remnants.class.getSimpleName(), "hellbound");
	}
}