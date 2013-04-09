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

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.util.ArrayUtil;

/**
 * Forge of the Gods Tar Beetles Script
 * Tar Beetles should be immortal, untargeteable and immobile.
 * When Tar Beetles see someone near them (300 range) they cast a debuff on target.
 * The debuff lvl gets increased if you already have it, up to max lvl 3.
 * The higher the debuff lvl, the higher the stats reduction is.
 * @author Copyleft
 */
public class ForgeBeetles extends L2AttackableAIScript
{
	static final int[] beetle = {18804};
	static final int debuff = 6142;

	public ForgeBeetles(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(beetle, QuestEventType.ON_AGGRO_RANGE_ENTER);
	}

	@Override
	public final String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (ArrayUtil.arrayContains(beetle,npc.getNpcId()) && npc.getTarget() == null && !npc.isCastingNow())
		{
			byte lvl = 1;

			if (player.getFirstEffect(debuff) != null)
			{
				if (player.getFirstEffect(debuff).getLevel() < 3) //max lvl of the debuff is 3
				{
					lvl++;
				}
			}

			L2Skill skill = SkillTable.getInstance().getInfo(debuff,lvl);
			npc.setTarget(player);
			npc.doCast(skill);

		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}


	public static void main(String[] args)
	{
		new ForgeBeetles(-1, "ForgeBeetles", "ai");
	}
}
