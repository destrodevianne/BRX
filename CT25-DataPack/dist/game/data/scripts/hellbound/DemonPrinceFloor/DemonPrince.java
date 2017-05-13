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
package hellbound.DemonPrinceFloor;

import java.util.Map;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import javolution.util.FastMap;

public class DemonPrince extends L2AttackableAIScript
{
	// Npcs
	private static final int DEMON_PRINCE = 25540;
	private static final int FIEND = 25541;

	// Skills
	private static final SkillHolder UD = new SkillHolder(5044, 2);
	private static final SkillHolder[] AOE =
	{
		new SkillHolder(5376, 4),
		new SkillHolder(5376, 5),
		new SkillHolder(5376, 6)
	};

	// Others
	private static final Map<Integer, Boolean> _attackState = new FastMap<>();
	
	public DemonPrince(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		addAttackId(DEMON_PRINCE);
		addKillId(DEMON_PRINCE);
		addSpawnId(FIEND);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && npc != null && npc.getId() == FIEND && !npc.isDead())
			npc.doCast(AOE[getRandom(3)].getSkill());
		
		return null;
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		if (npc.getId() == DEMON_PRINCE && !npc.isDead())
		{
			if (!_attackState.containsKey(npc.getObjectId()) && npc.getCurrentHp() < npc.getMaxHp() * 0.5)
			{
				npc.doCast(UD.getSkill());
				spawnMinions(npc);
				_attackState.put(npc.getObjectId(), false);
			}
			
			else if (npc.getCurrentHp() < npc.getMaxHp() * 0.1 && _attackState.containsKey(npc.getObjectId()) && _attackState.get(npc.getObjectId()) == false)
			{
				npc.doCast(UD.getSkill());
				spawnMinions(npc);
				_attackState.put(npc.getObjectId(), true);
			}
			
			if (getRandom(1000) < 10)
				spawnMinions(npc);
		}
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getId() == DEMON_PRINCE)
			_attackState.remove(npc.getObjectId());

		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (npc.getId() == FIEND)
			startQuestTimer("cast", 15000, npc, null);
		
		return super.onSpawn(npc);
	}
	
	private void spawnMinions(final L2Npc master)
	{
		if (master != null && !master.isDead())
		{
			final int instanceId = master.getInstanceId();
			final int x = master.getX();
			final int y = master.getY();
			final int z = master.getZ();
			addSpawn(FIEND, x + 200, y, z, 0, false, 0, false, instanceId);
			addSpawn(FIEND, x - 200, y, z, 0, false, 0, false, instanceId);
			addSpawn(FIEND, x - 100, y - 140, z, 0, false, 0, false, instanceId);
			addSpawn(FIEND, x - 100, y + 140, z, 0, false, 0, false, instanceId);
			addSpawn(FIEND, x + 100, y - 140, z, 0, false, 0, false, instanceId);
			addSpawn(FIEND, x + 100, y + 140, z, 0, false, 0, false, instanceId);
		}
	}
	
	public static void main(final String[] args)
	{
		new DemonPrince(-1, DemonPrince.class.getSimpleName(), "hellbound");
	}
}