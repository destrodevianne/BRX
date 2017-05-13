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
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2NpcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.util.Util;

public class FairyTrees extends L2AttackableAIScript
{
	// Npcs
	private static final int[] mobs =
	{
		27185,
		27186,
		27187,
		27188
	};

	public FairyTrees(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		this.registerMobs(mobs, QuestEventType.ON_KILL);
		super.addSpawnId(27189);
	}

	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (Util.contains(mobs, npcId))
			for (int i = 0; i < 20; i++)
			{
				final L2Attackable newNpc = (L2Attackable) addSpawn(27189, npc.getX(), npc.getY(), npc.getZ(), 0, false, 30000);
				final L2Character originalKiller = isPet ? killer.getPet() : killer;
				newNpc.setRunning();
				newNpc.addDamageHate(originalKiller, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalKiller);
				if (getRandom(1, 2) == 1)
				{
					final L2Skill skill = SkillTable.getInstance().getInfo(4243, 1);
					if (skill != null && originalKiller != null)
						skill.getEffects(newNpc, originalKiller);
				}
			}

		return super.onKill(npc, killer, isPet);
	}

	public static void main(final String[] args)
	{
		new FairyTrees(-1, FairyTrees.class.getSimpleName(), "ai/group_template");
	}
}
