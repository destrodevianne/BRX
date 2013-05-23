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

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.util.Rnd;

/**
 * 
 * @author Browser
 */
public class FemaleSpikedStakato extends L2AttackableAIScript
{
	private static final int MALE_SPIKED_STAKATO = 22621;
	//private static final int FEMALE_SPIKED_STAKATO = 22620;
	private static final int SPIKED_STAKATO_GUARD = 22619;
	
	public FemaleSpikedStakato(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(MALE_SPIKED_STAKATO);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2Npc couple = getCouple(npc);
		if (couple != null && !couple.isDead())
		{
			for (int i = 0; i < 3; i++)
			{
				// Set despawn delay 4 minutes for spawned minions. To avoid multiple instances over time in the same place
				final L2Npc guard = addSpawn(SPIKED_STAKATO_GUARD, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 240000, true);
				guard.setRunning();
				((L2Attackable) guard).addDamageHate(killer, 1, 99999);
				guard.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	public L2Npc getCouple(L2Npc couple)
	{
		// For now, minions are set as minionInstance. If they change to only monster, use the above code
        if (((L2MonsterInstance)couple).getMinionList().getSpawnedMinions().size() > 0)
            return ((L2MonsterInstance)couple).getMinionList().getSpawnedMinions().get(0);
		
		return null;
		
		/**
		final int spawnId = couple.getSpawn().getId() + 1;
		final L2Spawn spawnCouple = SpawnTable.getInstance().getSpawnTable().get(spawnId);
		if (spawnCouple != null && spawnCouple.getLastSpawn() != null)
		{
			if (spawnCouple.getLastSpawn().getNpcId() == FEMALE_SPIKED_STAKATO)
				return spawnCouple.getLastSpawn();
		}
		return null;
		*/
	}

	public static void main(String[] args)
	{
		new FemaleSpikedStakato(-1, FemaleSpikedStakato.class.getSimpleName(), "ai/zones");
	}
}
