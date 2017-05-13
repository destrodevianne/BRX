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
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastSet;

/**
 * @author InsOmnia
 */
public class OlAriosh extends L2AttackableAIScript
{
	// Npcs
	private static final int ARIOSH = 18555;
	private static final int GUARD = 18556;
	
	// Arrays
	private static L2Npc guard = null;
	private final FastSet<Integer> _lockedSpawns = new FastSet<>();
	private final TIntObjectHashMap<Integer> _spawnedGuards = new TIntObjectHashMap<>();
	
	public OlAriosh(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(ARIOSH);
		addKillId(ARIOSH, GUARD);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final int objId = npc.getObjectId();
		final int x = player.getX();
		final int y = player.getY();
		if (event.equalsIgnoreCase("time_to_spawn"))
			if (!_spawnedGuards.contains(objId))
			{
				npc.broadcastNpcSay("What are you doing ? Rather, help me!");
				guard = addSpawn(GUARD, x + 100, y + 100, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
				_lockedSpawns.remove(objId);
				_spawnedGuards.put(guard.getObjectId(), objId);
			}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public final String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int objId = npc.getObjectId();
		if (npcId == ARIOSH)
			if (!_spawnedGuards.contains(objId))
				if (!_lockedSpawns.contains(objId))
				{
					this.startQuestTimer("time_to_spawn", 60000, npc, player);
					_lockedSpawns.add(objId);
				}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (npcId == GUARD)
			_spawnedGuards.remove(npc.getObjectId());
		else if (npcId == ARIOSH)
		{
			_spawnedGuards.remove(guard.getObjectId());
			guard.decayMe();
			cancelQuestTimer("time_to_spawn", npc, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(final String[] args)
	{
		new OlAriosh(-1, OlAriosh.class.getSimpleName(), "ai/individual/kamaloka");
	}
}