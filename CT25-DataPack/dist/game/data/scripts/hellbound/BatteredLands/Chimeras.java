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
package hellbound.BatteredLands;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

public class Chimeras extends L2AttackableAIScript
{
	// Npcs
	private static final int[] NPCS =
	{
		22349,
		22350,
		22351,
		22352
	};
	private static final int CELTUS = 22353;

	// Locs
	private static final int[][] LOCATIONS =
	{
		{
			3678,
			233418,
			-3319
		},
		{
			2038,
			237125,
			-3363
		},
		{
			7222,
			240617,
			-2033
		},
		{
			9969,
			235570,
			-1993
		}
	};
	
	// Items
	private static final int BOTTLE = 9672;
	private static final int DIM_LIFE_FORCE = 9680;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (HellboundManager.getInstance().getLevel() == 7 && !npc.isTeleporting()) // Have random spawn points only in 7 lvl
		{
			final int[] spawn = LOCATIONS[getRandom(LOCATIONS.length)];
			if (!npc.isInsideRadius(spawn[0], spawn[1], spawn[2], 200, false, false))
			{
				npc.getSpawn().setLocx(spawn[0]);
				npc.getSpawn().setLocy(spawn[1]);
				npc.getSpawn().setLocz(spawn[2]);
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(npc, spawn), 100);
			}
		}
		
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (skill.getId() == BOTTLE && !npc.isDead())
			if (targets.length > 0 && targets[0] == npc)
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.1)
				{
					if (HellboundManager.getInstance().getLevel() == 7)
						HellboundManager.getInstance().updateTrust(3, true);
					
					npc.setIsDead(true);
					
					if (npc.getId() == CELTUS)
						((L2Attackable) npc).dropItem(caster, CONTAINED_LIFE_FORCE, 1);
					else if (getRandom(100) < 80)
						((L2Attackable) npc).dropItem(caster, DIM_LIFE_FORCE, 1);
					else if (getRandom(100) < 80)
						((L2Attackable) npc).dropItem(caster, LIFE_FORCE, 1);
					npc.onDecay();
				}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	private static class Teleport implements Runnable
	{
		private final L2Npc _npc;
		private final int[] _coords;
		
		public Teleport(final L2Npc npc, final int[] coords)
		{
			_npc = npc;
			_coords = coords;
		}
		
		@Override
		public void run()
		{
			_npc.teleToLocation(_coords[0], _coords[1], _coords[2]);
		}
	}
	
	public Chimeras(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addSkillSeeId(NPCS);
		addSpawnId(CELTUS);
		addSkillSeeId(CELTUS);
	}
	
	public static void main(final String[] args)
	{
		new Chimeras(-1, Chimeras.class.getSimpleName(), "hellbound");
	}
}
