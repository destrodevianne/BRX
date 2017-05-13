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
package ai.zones.ForgeOfTheGods;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;

/**
 * Tar Beetle AI
 * @author nonom, malyelfik
 */
public class TarBeetle extends L2AttackableAIScript
{
	// NPC
	private static final int TAR_BEETLE = 18804;

	// Skills
	private static final int SKILL_ID = 6142;
	private static SkillHolder[] SKILLS =
	{
		new SkillHolder(SKILL_ID, 1),
		new SkillHolder(SKILL_ID, 2),
		new SkillHolder(SKILL_ID, 3)
	};

	private static final TarBeetleSpawn spawn = new TarBeetleSpawn();

	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (spawn.getBeetle(npc).getScriptValue() > 0 && canCastSkill(npc))
		{
			int level = 0;
			final L2Effect effect = player.getFirstEffect(SKILL_ID);
			if (effect != null)
				level = effect.getSkill().getAbnormalLvl();
			if (level < 3)
			{

				npc.setTarget(player);
				npc.doCast(SKILLS[level].getSkill());
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onSpellFinished(final L2Npc npc, final L2PcInstance player, final L2Skill skill)
	{
		if (skill != null && skill.getId() == SKILL_ID)
		{
			final int val = spawn.getBeetle(npc).getScriptValue() - 1;
			if (val <= 0 || SKILLS[0].getSkill().getMpConsume() > npc.getCurrentMp())
				spawn.removeBeetle(npc);
			else
				spawn.getBeetle(npc).isScriptValue(val);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	private boolean canCastSkill(final L2Npc npc)
	{
		for (final SkillHolder holder : SKILLS)
			if (npc.isSkillDisabled(holder.getSkill()))
				return false;
		return true;
	}

	public TarBeetle(final int Id, final String name, final String descr)
	{
		super(Id, name, descr);
		addAggroRangeEnterId(TAR_BEETLE);
		addSpellFinishedId(TAR_BEETLE);

		spawn.startTasks();
	}

	public static void main(final String[] args)
	{
		new TarBeetle(-1, TarBeetle.class.getSimpleName(), "ai/zones/ForgeOfTheGods");
	}
}