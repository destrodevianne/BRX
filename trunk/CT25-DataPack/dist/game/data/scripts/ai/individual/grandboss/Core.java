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
package ai.individual.grandboss;

import java.util.List;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.Config;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2GrandBossInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.network.serverpackets.PlaySound;
import ct25.xtreme.gameserver.templates.StatsSet;
import javolution.util.FastList;

/**
 * Core AI
 * @author DrLecter Revised By Emperorc
 */
public class Core extends L2AttackableAIScript
{
	// Npcs
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	private static final int SUSCEPTOR = 29011;

	// CORE Status Tracking :
	private static final byte ALIVE = 0; // Core is spawned.
	private static final byte DEAD = 1; // Core has been killed.

	private static boolean _FirstAttacked;

	List<L2Attackable> Minions = new FastList<>();

	public Core(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		final int[] mobs =
		{
			CORE,
			DEATH_KNIGHT,
			DOOM_WRAITH,
			SUSCEPTOR
		};
		registerMobs(mobs);

		_FirstAttacked = false;
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
		final int status = GrandBossManager.getInstance().getBossStatus(CORE);
		if (status == DEAD)
		{
			// load the unlock date and time for Core from DB
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			// if Core is locked until a certain time, mark it so and start the unlock timer
			// the unlock time has not yet expired.
			if (temp > 0)
				startQuestTimer("core_unlock", temp, null, null);
			else
			{
				// the time has already expired while the server was offline. Immediately spawn Core.
				final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
				GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			final String test = loadGlobalQuestVar("Core_Attacked");
			if (test.equalsIgnoreCase("true"))
				_FirstAttacked = true;
			final int loc_x = info.getInt("loc_x");
			final int loc_y = info.getInt("loc_y");
			final int loc_z = info.getInt("loc_z");
			final int heading = info.getInt("heading");
			final int hp = info.getInt("currentHP");
			final int mp = info.getInt("currentMP");
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, loc_x, loc_y, loc_z, heading, false, 0);
			core.setCurrentHpMp(hp, mp);
			spawnBoss(core);
		}
	}

	@Override
	public void saveGlobalData()
	{
		final String val = "" + _FirstAttacked;
		saveGlobalQuestVar("Core_Attacked", val);
	}

	public void spawnBoss(final L2GrandBossInstance npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		// Spawn minions
		L2Attackable mob;
		for (int i = 0; i < 5; i++)
		{
			final int x = 16800 + i * 360;
			mob = (L2Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
			mob = (L2Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
			final int x2 = 16800 + i * 600;
			mob = (L2Attackable) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
		}
		for (int i = 0; i < 4; i++)
		{
			final int x = 16800 + i * 450;
			mob = (L2Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + getRandom(40), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
		}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("core_unlock"))
		{
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0);
			GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			final L2Attackable mob = (L2Attackable) addSpawn(npc.getId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
			mob.setIsRaidMinion(true);
			Minions.add(mob);
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (int i = 0; i < Minions.size(); i++)
			{
				final L2Attackable mob = Minions.get(i);
				if (mob != null)
					mob.decayMe();
			}
			Minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.getId() == CORE)
			if (_FirstAttacked)
			{
				if (getRandom(100) == 0)
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), 1000003)); // Removing intruders.
			}
			else
			{
				_FirstAttacked = true;
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), 1000001)); // A non-permitted target has been discovered.
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), 1000002)); // Intruder removal system initiated.
			}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (npcId == CORE)
		{
			final int objId = npc.getObjectId();
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, 1000004)); // A fatal error has occurred.
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, 1000005)); // System is being shut down...
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, 1000006)); // ......
			_FirstAttacked = false;
			addSpawn(31842, 16502, 110165, -6394, 0, false, 900000);
			addSpawn(31842, 18948, 110166, -6397, 0, false, 900000);
			GrandBossManager.getInstance().setBossStatus(CORE, DEAD);
			// time is 60hour +/- 23hour
			final long respawnTime = (long) Config.Interval_Of_Core_Spawn + getRandom(Config.Random_Of_Core_Spawn);
			startQuestTimer("core_unlock", respawnTime, null, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(CORE, info);
			this.startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if (GrandBossManager.getInstance().getBossStatus(CORE) == ALIVE && Minions != null && Minions.contains(npc))
		{
			Minions.remove(npc);
			startQuestTimer("spawn_minion", 60000, npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}

	public static void main(final String[] args)
	{
		// now call the constructor (starts up the ai)
		new Core(-1, "core", "ai");
	}
}