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
package ai.group_template;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.util.ArrayUtil;

public class SkillLvlOnAttack extends L2AttackableAIScript
{
	static final int[] malariaMobs =
	{
		21314,
		21316,
		21317,
		21319,
		21321,
		21322
	}; // Monsters which cast Hot Spring Malaria (4554)
	static final int[] fluMobs =
	{
		21317,
		21322
	}; // Monsters which cast Hot Springs Flu (4553)
	static final int[] choleraMobs =
	{
		21316,
		21319
	}; // Monsters which cast Hot Springs Cholera (4552)
	static final int[] rheumatismMobs =
	{
		21314,
		21321
	}; // Monsters which cast Hot Springs Rheumatism (4551)
	
	public SkillLvlOnAttack(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		registerMobs(malariaMobs, QuestEventType.ON_ATTACK);
		registerMobs(fluMobs, QuestEventType.ON_ATTACK);
		registerMobs(choleraMobs, QuestEventType.ON_ATTACK);
		registerMobs(rheumatismMobs, QuestEventType.ON_ATTACK);
	}
	
	// Default chance is 10% ... custom chance can be set here for every skill
	private int getSkillChance(final int skillId)
	{
		final int chance = 10;
		return chance;
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		int skillId = 0;
		int skillLvl = 0;
		int maxSkillLvl = 0;
		
		if (ArrayUtil.arrayContains(malariaMobs, npc.getId()))
		{
			skillId = 4554;
			maxSkillLvl = 10;
		}
		
		if (ArrayUtil.arrayContains(fluMobs, npc.getId()))
		{
			if (skillId != 0)
				if (getRandom(100) <= 50)
					skillId = 4553;
			maxSkillLvl = 10;
		}
		
		if (ArrayUtil.arrayContains(choleraMobs, npc.getId()))
		{
			if (skillId != 0)
				if (getRandom(100) <= 50)
					skillId = 4552;
			maxSkillLvl = 10;
		}
		
		if (ArrayUtil.arrayContains(rheumatismMobs, npc.getId()))
		{
			if (skillId != 0)
				if (getRandom(100) <= 50)
					skillId = 4551;
			maxSkillLvl = 10;
		}
		
		if (getRandom(100) < getSkillChance(skillId))
		{
			if (attacker.getFirstEffect(skillId) == null)
				skillLvl = 1;
			else
			{
				skillLvl = attacker.getFirstEffect(skillId).getLevel();
				if (skillLvl == maxSkillLvl)
					skillLvl = maxSkillLvl;
				else
					skillLvl++;
			}
			
			npc.setTarget(attacker);
			final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
			skill.getEffects(npc, attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(final String[] args)
	{
		new SkillLvlOnAttack(-1, SkillLvlOnAttack.class.getSimpleName(), "ai/group_template");
	}
}