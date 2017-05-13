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
package ai.zones.MithrilMinesZone;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;

/**
 * @author Browser
 */
public final class MithrilMinesZone extends L2AttackableAIScript
{
	// NPCs
	private static final int GRAVE_ROBBER_SUMMONER = 22678; // Grave Robber Summoner (Lunatic)
	private static final int GRAVE_ROBBER_MAGICIAN = 22679; // Grave Robber Magician (Lunatic)
	private static final int[] SUMMONER_MINIONS =
	{
		22683, // Servitor of Darkness
		22684, // Servitor of Darkness
	};
	private static final int[] MAGICIAN_MINIONS =
	{
		22685, // Servitor of Darkness
		22686, // Servitor of Darkness
	};
	
	private MithrilMinesZone()
	{
		super(-1, MithrilMinesZone.class.getSimpleName(), "ai/zones");
		addSpawnId(GRAVE_ROBBER_SUMMONER, GRAVE_ROBBER_MAGICIAN);
		
		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(GRAVE_ROBBER_SUMMONER))
			onSpawn(spawn.getLastSpawn());
		
		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(GRAVE_ROBBER_MAGICIAN))
			onSpawn(spawn.getLastSpawn());
	}
	
	@Override
	public String onSpawn(final L2Npc npc)
	{
		final int[] minions = npc.getId() == GRAVE_ROBBER_SUMMONER ? SUMMONER_MINIONS : MAGICIAN_MINIONS;
		addMinion((L2MonsterInstance) npc, minions[getRandom(minions.length)]);
		return super.onSpawn(npc);
	}
	
	public static void main(final String[] args)
	{
		new MithrilMinesZone();
	}
}