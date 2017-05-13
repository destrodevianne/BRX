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
package ai.zones.StakatoNest;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Browser
 */
public class SpikedStakatoBaby extends L2AttackableAIScript
{
	// Npcs
	private static final int SPIKED_STAKATO_NURSE = 22630;
	private static final int SPIKED_STAKATO_CAPTAIN = 22629;

	public SpikedStakatoBaby(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addKillId(SPIKED_STAKATO_NURSE);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final L2Npc baby = getBaby(npc);
		if (baby != null && !baby.isDead())
			for (int i = 0; i < 3; i++)
			{
				// Set despawn delay 4 minutes for spawned minions. To avoid multiple instances over time in the same place
				final L2Npc captain = addSpawn(SPIKED_STAKATO_CAPTAIN, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 240000, true);
				captain.setRunning();
				((L2Attackable) captain).addDamageHate(killer, 1, 99999);
				captain.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		return super.onKill(npc, killer, isPet);
	}
	
	public L2Npc getBaby(final L2Npc couple)
	{
		// For now, minions are set as minionInstance. If they change to only monster, use the above code
		if (((L2MonsterInstance) couple).getMinionList().getSpawnedMinions().size() > 0)
			return ((L2MonsterInstance) couple).getMinionList().getSpawnedMinions().get(0);

		return null;
	}
	
	public static void main(final String[] args)
	{
		new SpikedStakatoBaby(-1, SpikedStakatoBaby.class.getSimpleName(), "ai/zones");
	}
}
