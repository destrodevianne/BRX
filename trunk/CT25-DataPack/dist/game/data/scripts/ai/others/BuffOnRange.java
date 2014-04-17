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
package ai.others;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.GeoData;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author Kazumi
 */
public class BuffOnRange extends L2AttackableAIScript
{
	public BuffOnRange(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mob = { 13006, // Christmas Tree
				13007, // Special Christmas Tree
				18379 // Trap (Nornil's Garden)
		};
		this.registerMobs(mob, QuestEventType.ON_SPAWN);
	}

	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == 13007)
		{
			startQuestTimer("regen", 5000, npc, null, true);
			startQuestTimer("despawn", 3600000, npc, null);
		}
		else if (npc.getId() == 13006)
			startQuestTimer("despawn", 3600000, npc, null);
		else if (npc.getId() == 18379)
			startQuestTimer("buff", 10000, npc, null, true);
		return null;
	}

        @Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc != null)
		{
			if (npc.getId() == 13007)
			{
				if (event.equalsIgnoreCase("despawn"))
				{
					this.cancelQuestTimers("regen");
					npc.deleteMe();
				}
				else if (event.equalsIgnoreCase("regen"))
				{
					for (L2PcInstance obj : npc.getKnownList().getKnownPlayersInRadius(200))
					{
						if (!(GeoData.getInstance().canSeeTarget(obj.getX(), obj.getY(), obj.getZ(), npc.getX(), npc.getY(), npc.getZ())))
							continue;
						if (!obj.isDead())
							handleCast(npc, obj, 2139, 1);
					}
				}
			}
			else if (npc.getId() == 13006)
			{
				if (event.equalsIgnoreCase("despawn"))
				{
					this.cancelQuestTimers("regen");
					npc.deleteMe();
				}
			}
			else if (npc.getId() == 18379)
			{
				if (event.equalsIgnoreCase("buff"))
				{
					for (L2PcInstance obj : npc.getKnownList().getKnownPlayersInRadius(600))
					{
						if (!obj.isDead())
							handleCast(npc, obj, 4322, 1);
						handleCast(npc, obj, 4327, 1);
						handleCast(npc, obj, 4329, 1);
						handleCast(npc, obj, 4324, 1);
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	private boolean handleCast(L2Npc npc, L2Character player, int skillId, int skillLevel)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if (skill == null)
			return false;
		if (player.getFirstEffect(skill) != null)
		{
			player.stopSkillEffects(2139);
		}
		skill.getEffects(npc, player);
		npc.broadcastPacket(new MagicSkillUse(npc, player, skillId, skillLevel, 0, 0));
		return true;
	}

	public static void main(String[] args)
	{
		new BuffOnRange(-1, BuffOnRange.class.getSimpleName(), "ai/others");
	}
}
