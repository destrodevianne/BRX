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
package hellbound.RankuFloor;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.util.MinionList;
import gnu.trove.set.hash.TIntHashSet;

public class Ranku extends L2AttackableAIScript
{
	// Npcs
	private static final int RANKU = 25542;
	private static final int MINION = 32305;
	private static final int MINION_2 = 25543;

	// Strings
	private static final int MINIONS_FSTRING_ID = 1800136; // Don't kill me please.. Something's strangling me...

	// Constants
	private static TIntHashSet myTrackingSet = new TIntHashSet();
	
	public Ranku(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		addAttackId(RANKU);
		addKillId(RANKU, MINION);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("checkup") && npc.getId() == RANKU && !npc.isDead())
		{
			for (final L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
				if (minion != null && !minion.isDead() && myTrackingSet.contains(minion.getObjectId()))
				{
					final L2PcInstance[] players = minion.getKnownList().getKnownPlayers().values().toArray(new L2PcInstance[minion.getKnownList().getKnownPlayers().size()]);
					final L2PcInstance killer = players[getRandom(players.length)];
					minion.reduceCurrentHp(minion.getMaxHp() / 100, killer, null);
				}
			startQuestTimer("checkup", 1000, npc, null);
		}
		return null;
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		if (npc.getId() == RANKU)
			for (final L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
				if (minion != null && !minion.isDead() && !myTrackingSet.contains(minion.getObjectId()))
				{
					minion.broadcastPacket(new NpcSay(minion.getObjectId(), Say2.ALL, minion.getId(), MINIONS_FSTRING_ID));
					startQuestTimer("checkup", 1000, npc, null);
					synchronized (myTrackingSet)
					{
						myTrackingSet.add(minion.getObjectId());
					}
				}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getId() == MINION)
		{
			if (myTrackingSet.contains(npc.getObjectId()))
				synchronized (myTrackingSet)
				{
					myTrackingSet.remove(npc.getObjectId());
				}

			final L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();

			if (master != null && !master.isDead())
			{
				final L2MonsterInstance minion2 = MinionList.spawnMinion(master, MINION_2);
				minion2.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
			}
		}

		else if (npc.getId() == RANKU)
			for (final L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
				if (myTrackingSet.contains(minion.getObjectId()))
					synchronized (myTrackingSet)
					{
						myTrackingSet.remove(minion.getObjectId());
					}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(final String[] args)
	{
		new Ranku(-1, Ranku.class.getSimpleName(), "hellbound");
	}
}