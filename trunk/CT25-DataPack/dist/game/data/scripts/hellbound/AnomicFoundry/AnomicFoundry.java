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
package hellbound.AnomicFoundry;

import java.util.Map;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.instancemanager.WalkingManager;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import javolution.util.FastMap;

public class AnomicFoundry extends Quest
{
	// Npcs
	private static int LABORER = 22396;
	private static int FOREMAN = 22397;
	private static int LESSER_EVIL = 22398;
	private static int GREATER_EVIL = 22399;

	// npcId, x, y, z, heading, max count
	private static int[][] SPAWNS =
	{
		{
			LESSER_EVIL,
			27883,
			248613,
			-3209,
			-13248,
			5
		},
		{
			LESSER_EVIL,
			26142,
			246442,
			-3216,
			7064,
			5
		},
		{
			LESSER_EVIL,
			27335,
			246217,
			-3668,
			-7992,
			5
		},
		{
			LESSER_EVIL,
			28486,
			245913,
			-3698,
			0,
			10
		},
		{
			GREATER_EVIL,
			28684,
			244118,
			-3700,
			-22560,
			10
		},
	};

	private int respawnTime = 60000;
	private final int respawnMin = 20000;
	private final int respawnMax = 300000;
	
	private final int[] _spawned =
	{
		0,
		0,
		0,
		0,
		0
	};
	private final Map<Integer, Integer> _atkIndex = new FastMap<>();
	
	public AnomicFoundry(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addAggroRangeEnterId(LABORER);
		addAttackId(LABORER);
		addKillId(LABORER, LESSER_EVIL, GREATER_EVIL);
		addSpawnId(LABORER, LESSER_EVIL, GREATER_EVIL);

		startQuestTimer("make_spawn_1", respawnTime, null, null);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("make_spawn_1"))
		{
			if (HellboundManager.getInstance().getLevel() >= 10)
			{
				final int idx = getRandom(3);
				if (_spawned[idx] < SPAWNS[idx][5])
				{
					addSpawn(SPAWNS[idx][0], SPAWNS[idx][1], SPAWNS[idx][2], SPAWNS[idx][3], SPAWNS[idx][4], false, 0, false);
					respawnTime += 10000;
				}
				startQuestTimer("make_spawn_1", respawnTime, null, null);
			}
		}

		else if (event.equalsIgnoreCase("make_spawn_2"))
		{
			if (_spawned[4] < SPAWNS[4][5])
				addSpawn(SPAWNS[4][0], SPAWNS[4][1], SPAWNS[4][2], SPAWNS[4][3], SPAWNS[4][4], false, 0, false);
		}

		else if (event.equalsIgnoreCase("return_laborer"))
		{
			if (npc != null && !npc.isDead())
				((L2Attackable) npc).returnHome();
		}

		else if (event.equalsIgnoreCase("reset_respawn_time"))
			respawnTime = 60000;

		return null;
	}
	
	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (getRandom(10000) < 2000)
			requestHelp(npc, player, 500, FOREMAN);
		requestHelp(npc, player, 500, LESSER_EVIL);
		requestHelp(npc, player, 500, GREATER_EVIL);
		
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		int atkIndex = _atkIndex.containsKey(npc.getObjectId()) ? _atkIndex.get(npc.getObjectId()) : 0;
		if (atkIndex == 0)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1800109)); // Enemy invasion! Hurry up!
			cancelQuestTimer("return_laborer", npc, null);
			startQuestTimer("return_laborer", 60000, npc, null);

			if (respawnTime > respawnMin)
				respawnTime -= 5000;
			else if (respawnTime <= respawnMin && getQuestTimer("reset_respawn_time", null, null) == null)
				startQuestTimer("reset_respawn_time", 600000, null, null);
		}

		if (getRandom(10000) < 2000)
		{
			atkIndex++;
			_atkIndex.put(npc.getObjectId(), atkIndex);
			requestHelp(npc, attacker, 1000 * atkIndex, FOREMAN);
			requestHelp(npc, attacker, 1000 * atkIndex, LESSER_EVIL);
			requestHelp(npc, attacker, 1000 * atkIndex, GREATER_EVIL);

			if (getRandom(10) < 1)
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(npc.getX() + getRandom(-800, 800), npc.getY() + getRandom(-800, 800), npc.getZ(), npc.getHeading()));
		}
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (getSpawnGroup(npc) >= 0)
		{
			_spawned[getSpawnGroup(npc)]--;
			SpawnTable.getInstance().deleteSpawn(npc.getSpawn(), false);
		}

		else if (npc.getId() == LABORER)
		{
			if (getRandom(10000) < 8000)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1800110)); // Process... shouldn't... be delayed... because of me...
				if (respawnTime < respawnMax)
					respawnTime += 10000;
				else if (respawnTime >= respawnMax && getQuestTimer("reset_respawn_time", null, null) == null)
					startQuestTimer("reset_respawn_time", 600000, null, null);
			}
			_atkIndex.remove(npc.getObjectId());
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			SpawnTable.getInstance().addNewSpawn(npc.getSpawn(), false);
			if (getSpawnGroup(npc) >= 0)
				_spawned[getSpawnGroup(npc)]++;

			if (npc.getId() == LABORER)
				npc.setIsNoRndWalk(true);
		}

		if (getSpawnGroup(npc) >= 0 && getSpawnGroup(npc) <= 2)
		{
			if (!npc.isTeleporting())
				WalkingManager.getInstance().startMoving(npc, getRoute(npc));
			
			else
			{
				_spawned[getSpawnGroup(npc)]--;
				SpawnTable.getInstance().deleteSpawn(npc.getSpawn(), false);
				npc.scheduleDespawn(100);
				if (_spawned[3] < SPAWNS[3][5])
					addSpawn(SPAWNS[3][0], SPAWNS[3][1], SPAWNS[3][2], SPAWNS[3][3], SPAWNS[3][4], false, 0, false);
			}
		}

		else if (getSpawnGroup(npc) == 3)
		{
			if (!npc.isTeleporting())
				WalkingManager.getInstance().startMoving(npc, getRoute(npc));

			else
			{
				startQuestTimer("make_spawn_2", respawnTime * 2, null, null);
				_spawned[3]--;
				SpawnTable.getInstance().deleteSpawn(npc.getSpawn(), false);
				npc.scheduleDespawn(100);
			}
		}

		else if (getSpawnGroup(npc) == 4 && !npc.isTeleporting())
			WalkingManager.getInstance().startMoving(npc, getRoute(npc));

		return super.onSpawn(npc);
	}

	private static int getSpawnGroup(final L2Npc npc)
	{
		int ret = -1;
		final int[] coords = new int[2];
		coords[0] = npc.getSpawn().getLocx();
		coords[1] = npc.getSpawn().getLocy();
		final int npcId = npc.getId();
		
		for (int i = 0; i < 5; i++)
			if (SPAWNS[i][0] == npcId && SPAWNS[i][1] == coords[0] && SPAWNS[i][2] == coords[1])
			{
				ret = i;
				break;
			}

		return ret;
	}

	private static int getRoute(final L2Npc npc)
	{
		final int ret = getSpawnGroup(npc);
		
		return ret >= 0 ? ret + 6 : -1;
	}

	private static void requestHelp(final L2Npc requester, final L2PcInstance agressor, final int range, final int helperId)
	{
		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(helperId))
		{
			final L2MonsterInstance monster = (L2MonsterInstance) spawn.getLastSpawn();
			if (monster != null && agressor != null && !monster.isDead() && monster.isInsideRadius(requester, range, true, false) && !agressor.isDead())
				monster.addDamageHate(agressor, 0, 1000);
		}
	}
	
	public static void main(final String[] args)
	{
		new AnomicFoundry(-1, AnomicFoundry.class.getSimpleName(), "hellbound");
	}
}