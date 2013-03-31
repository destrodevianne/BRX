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

import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.util.Rnd;

/**
 * 
 * @author Browser
 */
public class CannibalisticStakatoFollower extends L2AttackableAIScript
{
	private static final int CANNIBALISTIC_LEADER = 22625;
	//private static final int CANNIBALISTIC_FOLLOWER = 22624;
	
	public CannibalisticStakatoFollower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(CANNIBALISTIC_LEADER);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getMaxHp() * 0.3 > npc.getCurrentHp())
		{
			if (Rnd.get(100) <= 25)
			{
				final L2Npc minion = getLeaderMinion(npc);
				if (minion != null && !minion.isDead())
				{
					npc.broadcastPacket(new MagicSkillUse(npc, minion, 4485, 1, 3000, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new eatTask(npc, minion), 3000);
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	public L2Npc getLeaderMinion(L2Npc leader)
	{
		// For now, minions are set as minionInstance. If they change to only monster, use the above code
	        if (((L2MonsterInstance)leader).getMinionList().getSpawnedMinions().size() > 0)
	            return ((L2MonsterInstance)leader).getMinionList().getSpawnedMinions().get(0);		
		
		return null;
		
		/**
		final int spawnId = leader.getSpawn().getId() + 1;
		final L2Spawn spawnCouple = SpawnTable.getInstance().getSpawnTable().get(spawnId);
		if (spawnCouple != null && spawnCouple.getLastSpawn() != null)
		{
			if (spawnCouple.getLastSpawn().getNpcId() == CANNIBALISTIC_FOLLOWER)
				return spawnCouple.getLastSpawn();
		}

		return null;
		*/
	}

	private class eatTask implements Runnable
	{
		private L2Npc _npc;
		private L2Npc _minion;
		
		private eatTask (L2Npc npc, L2Npc minion)
		{
			_npc = npc;
			_minion = minion;
		}
		
		public void run()
		{
			if (_minion == null)
				return;
			
			final double hpToSacrifice = _minion.getCurrentHp();
			_npc.setCurrentHp(_npc.getCurrentHp() + hpToSacrifice);
			_npc.broadcastPacket(new MagicSkillUse(_npc, _minion, 4484, 1, 1000, 0));
			_minion.doDie(_minion);
		}
	}

	public static void main(String[] args)
	{
		new CannibalisticStakatoFollower(-1, "CannibalisticStakatoFollower", "ai");
		_log.info("Stakato Nest: Loaded CannibalisticStakatoFollower.");
	}
}
