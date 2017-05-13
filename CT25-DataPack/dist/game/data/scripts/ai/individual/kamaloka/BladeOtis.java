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
package ai.individual.kamaloka;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastMap;

/**
 * @author InsOmnia
 */
public class BladeOtis extends L2AttackableAIScript
{
	// Npcs
	private static final int BLADEO = 18562;
	private static final int GUARD = 18563;
	
	// Constants
	private final TIntObjectHashMap<Integer> _guardSpawns = new TIntObjectHashMap<>();
	private final FastMap<L2Npc, L2Npc> _guardMaster = new FastMap<>();
	
	public BladeOtis(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(BLADEO);
		addKillId(BLADEO, GUARD);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final int objId = npc.getObjectId();
		final int x = player.getX();
		final int y = player.getY();
		if (_guardSpawns.get(objId) != null && _guardSpawns.get(objId) >= 6)
			return null;
		if (event.equalsIgnoreCase("time_to_spawn"))
		{
			final L2Npc guard = addSpawn(GUARD, x + getRandom(-20, 50), y + getRandom(-20, 50), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			if (_guardSpawns.get(objId) != null)
				_guardSpawns.put(objId, _guardSpawns.get(objId) + 1);
			else
				_guardSpawns.put(objId, 1);
			_guardMaster.put(guard, npc);
			guard.setTarget(player);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int objId = npc.getObjectId();
		final int maxHp = npc.getMaxHp();
		final double nowHp = npc.getStatus().getCurrentHp();
		if (npcId == BLADEO)
			if (nowHp < maxHp * 0.5)
				if (_guardSpawns.get(objId) == null || _guardSpawns.get(objId) == 0)
					this.startQuestTimer("time_to_spawn", 1, npc, player);
				else if (_guardSpawns.get(objId) < 6)
					this.startQuestTimer("time_to_spawn", 10000, npc, player);
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int objId = npc.getObjectId();
		if (npcId == GUARD)
		{
			if (_guardMaster.get(npc) != null)
			{
				final L2Npc master = _guardMaster.get(npc);
				if (_guardSpawns.get(master.getObjectId()) != null && _guardSpawns.get(master.getObjectId()) > 0)
					_guardSpawns.put(master.getObjectId(), _guardSpawns.get(master.getObjectId()) - 1);
				_guardMaster.remove(npc);
			}
		}
		else if (npcId == BLADEO)
		{
			if (_guardSpawns.contains(objId))
				_guardSpawns.remove(objId);
			for (final L2Npc i : _guardMaster.keySet())
				if (_guardMaster.get(i) != null && npc == _guardMaster.get(i))
				{
					i.decayMe();
					_guardMaster.remove(i);
				}
			cancelQuestTimer("time_to_spawn", npc, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(final String[] args)
	{
		new BladeOtis(-1, BladeOtis.class.getSimpleName(), "ai/individual/kamaloka");
	}
}