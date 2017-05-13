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
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.util.ArrayUtil;
import javolution.util.FastMap;

/**
 * @author InsOmnia
 */
public class RespawnOnAttack extends L2AttackableAIScript
{
	private static final FastMap<Integer, Integer[]> MOBSPAWNS = new FastMap<>();
	private static final int[] MOBS =
	{
		20832,
		20836,
		20833,
		20835,
		20840,
		20841,
		20842,
		20843,
		20844,
		20845,
		20846,
		20847,
		21612
	};
	static
	{
		MOBSPAWNS.put(21607, new Integer[]
		{
			20833,
			10
		});
		MOBSPAWNS.put(21604, new Integer[]
		{
			20832,
			10
		});
		MOBSPAWNS.put(21605, new Integer[]
		{
			20843,
			10
		});
		MOBSPAWNS.put(21610, new Integer[]
		{
			20835,
			10
		});
		MOBSPAWNS.put(21616, new Integer[]
		{
			20840,
			10
		});
		MOBSPAWNS.put(21619, new Integer[]
		{
			20841,
			10
		});
		MOBSPAWNS.put(21622, new Integer[]
		{
			20842,
			10
		});
		MOBSPAWNS.put(21628, new Integer[]
		{
			20844,
			10
		});
		MOBSPAWNS.put(21631, new Integer[]
		{
			20845,
			10
		});
		MOBSPAWNS.put(21634, new Integer[]
		{
			20846,
			10
		});
		MOBSPAWNS.put(21637, new Integer[]
		{
			20847,
			10
		});
		MOBSPAWNS.put(21613, new Integer[]
		{
			20839,
			10
		});
	}
	
	public RespawnOnAttack(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int id : MOBSPAWNS.keySet())
			super.addAttackId(id);
		for (final int id : MOBS)
			super.addAttackId(id);
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		final double currentHp = npc.getCurrentHp();
		final int maxHp = npc.getMaxHp();
		final int npcId = npc.getId();
		if (MOBSPAWNS.containsKey(npcId))
		{
			final Integer[] tmp = MOBSPAWNS.get(npcId);
			if (currentHp < maxHp * 0.78 && currentHp > maxHp * 0.72 && getRandom(100) < tmp[1])
			{
				npc.decayMe();
				final L2Npc newNpc = addSpawn(tmp[0], npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
				newNpc.setRunning();
				((L2Attackable) newNpc).addDamageHate(attacker, 0, 500);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
		}
		else if (ArrayUtil.arrayContains(MOBS, npcId))
			if (currentHp < maxHp * 0.80 && currentHp > maxHp * 0.70 && getRandom(100) < 25)
			{
				npc.decayMe();
				final L2Npc newNpc = addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
				newNpc.setRunning();
				((L2Attackable) newNpc).addDamageHate(attacker, 0, 500);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(final String[] args)
	{
		new RespawnOnAttack(-1, RespawnOnAttack.class.getSimpleName(), "ai/group_template");
	}
}