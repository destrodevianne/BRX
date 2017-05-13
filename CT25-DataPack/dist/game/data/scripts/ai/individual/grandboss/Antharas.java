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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.Config;
import ct25.xtreme.gameserver.GeoData;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2GrandBossInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.zone.type.L2BossZone;
import ct25.xtreme.gameserver.network.serverpackets.Earthquake;
import ct25.xtreme.gameserver.network.serverpackets.L2GameServerPacket;
import ct25.xtreme.gameserver.network.serverpackets.PlaySound;
import ct25.xtreme.gameserver.network.serverpackets.SpecialCamera;
import ct25.xtreme.gameserver.templates.StatsSet;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;

/**
 * This class ... control for sequence of fight against Antharas.
 * @version $Revision: $ $Date: $
 * @author L2J_JP SANDMAN
 */
public class Antharas extends L2AttackableAIScript
{
	// Config
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	private static final int FWA_INACTIVITYTIME = 900000;
	private static final boolean FWA_OLDANTHARAS = false;
	private static final boolean FWA_MOVEATRANDOM = true;
	private static final boolean FWA_DOSERVEREARTHQUAKE = true;
	private static final int FWA_LIMITOFWEAK = 45;
	private static final int FWA_LIMITOFNORMAL = 63;
	private static final int FWA_MAXMOBS = 10; // this includes Antharas itself
	private static final int FWA_INTERVALOFMOBSWEAK = 180000;
	private static final int FWA_INTERVALOFMOBSNORMAL = 150000;
	private static final int FWA_INTERVALOFMOBSSTRONG = 120000;
	private static final int FWA_PERCENTOFBEHEMOTH = 60;
	private static final int FWA_SELFDESTRUCTTIME = 15000;

	// Location of teleport cube.
	private final int _teleportCubeId = 31859;
	private final int _teleportCubeLocation[][] =
	{
		{
			177615,
			114941,
			-7709,
			0
		}
	};
	protected List<L2Spawn> _teleportCubeSpawn = new FastList<>();
	protected List<L2Npc> _teleportCube = new FastList<>();

	// Spawn data of monsters.
	protected TIntObjectHashMap<L2Spawn> _monsterSpawn = new TIntObjectHashMap<>();

	// Instance of monsters.
	protected List<L2Npc> _monsters = new FastList<>();
	protected L2GrandBossInstance _antharas = null;

	// Monster Ids
	private static final int ANTHARASOLDID = 29019;
	private static final int ANTHARASWEAKID = 29066;
	private static final int ANTHARASNORMALID = 29067;
	private static final int ANTHARASSTRONGID = 29068;

	// Tasks.
	protected ScheduledFuture<?> _cubeSpawnTask = null;
	protected ScheduledFuture<?> _monsterSpawnTask = null;
	protected ScheduledFuture<?> _activityCheckTask = null;
	protected ScheduledFuture<?> _socialTask = null;
	protected ScheduledFuture<?> _mobiliseTask = null;
	protected ScheduledFuture<?> _mobsSpawnTask = null;
	protected ScheduledFuture<?> _selfDestructionTask = null;
	protected ScheduledFuture<?> _moveAtRandomTask = null;
	protected ScheduledFuture<?> _movieTask = null;

	// Antharas Status Tracking :
	private static final byte DORMANT = 0; // Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final byte WAITING = 1; // Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	// before he unleashes his attack. Entry is unlocked
	private static final byte FIGHTING = 2; // Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 3; // Antharas has been killed. Entry is locked

	protected static long _LastAction = 0;

	protected static L2BossZone _Zone;

	public static void main(final String[] args)
	{
		// now call the constructor (starts up the ai)
		new Antharas(-1, "antharas", "ai");
	}

	// Boss: Antharas
	public Antharas(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		final int[] mob =
		{
			ANTHARASOLDID,
			ANTHARASWEAKID,
			ANTHARASNORMALID,
			ANTHARASSTRONGID,
			29069,
			29070,
			29071,
			29072,
			29073,
			29074,
			29075,
			29076
		};
		this.registerMobs(mob);
		init();
	}

	// Initialize
	private void init()
	{
		// Setting spawn data of monsters.
		try
		{
			_Zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
			L2NpcTemplate template1;
			L2Spawn tempSpawn;

			// Old Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASOLDID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29019, tempSpawn);

			// Weak Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASWEAKID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29066, tempSpawn);

			// Normal Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASNORMALID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29067, tempSpawn);

			// Strong Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASSTRONGID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29068, tempSpawn);
		}
		catch (final Exception e)
		{
			_log.warning(e.getMessage());
		}

		// Setting spawn data of teleport cube.
		try
		{
			final L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for (final int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch (final Exception e)
		{
			_log.warning(e.getMessage());
		}
		int status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
		if (FWA_OLDANTHARAS || status == WAITING)
		{
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARASOLDID);
			final Long respawnTime = info.getLong("respawn_time");
			if (status == DEAD && respawnTime <= System.currentTimeMillis())
			{
				// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
				// also, the status needs to be changed to DORMANT
				GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
				status = DORMANT;
			}
			else if (status == FIGHTING)
			{
				final int loc_x = info.getInt("loc_x");
				final int loc_y = info.getInt("loc_y");
				final int loc_z = info.getInt("loc_z");
				final int heading = info.getInt("heading");
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(ANTHARASOLDID, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (status == DEAD)
				ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(ANTHARASOLDID), respawnTime - System.currentTimeMillis());
			else
				setAntharasSpawnTask();
		}
		else
		{
			final int statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			final int statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			final int statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);
			int antharasId = 0;
			if (statusWeak == FIGHTING || statusWeak == DEAD)
			{
				antharasId = ANTHARASWEAKID;
				status = statusWeak;
			}
			else if (statusNormal == FIGHTING || statusNormal == DEAD)
			{
				antharasId = ANTHARASNORMALID;
				status = statusNormal;
			}
			else if (statusStrong == FIGHTING || statusStrong == DEAD)
			{
				antharasId = ANTHARASSTRONGID;
				status = statusStrong;
			}
			if (antharasId != 0 && status == FIGHTING)
			{
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				final int loc_x = info.getInt("loc_x");
				final int loc_y = info.getInt("loc_y");
				final int loc_z = info.getInt("loc_z");
				final int heading = info.getInt("heading");
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(antharasId, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (antharasId != 0 && status == DEAD)
			{
				final StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				final Long respawnTime = info.getLong("respawn_time");
				if (respawnTime <= System.currentTimeMillis())
				{
					// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
					// also, the status needs to be changed to DORMANT
					GrandBossManager.getInstance().setBossStatus(antharasId, DORMANT);
					status = DORMANT;
				}
				else
					ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(antharasId), respawnTime - System.currentTimeMillis());
			}
		}
	}

	// Do spawn teleport cube.
	public void spawnCube()
	{
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}

		for (final L2Spawn spawnDat : _teleportCubeSpawn)
			_teleportCube.add(spawnDat.doSpawn());
	}

	// Setting Antharas spawn task.
	public void setAntharasSpawnTask()
	{
		if (_monsterSpawnTask == null)
			synchronized (this)
			{
				if (_monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, WAITING);
					_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1), Config.Antharas_Wait_Time);
				}
			}
	}

	protected void startMinionSpawns(final int antharasId)
	{
		int intervalOfMobs;

		// Interval of minions is decided by the type of Antharas
		// that invaded the lair.
		switch (antharasId)
		{
			case ANTHARASWEAKID:
				intervalOfMobs = FWA_INTERVALOFMOBSWEAK;
				break;
			case ANTHARASNORMALID:
				intervalOfMobs = FWA_INTERVALOFMOBSNORMAL;
				break;
			default:
				intervalOfMobs = FWA_INTERVALOFMOBSSTRONG;
				break;
		}

		// Spawn mobs.
		_mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MobsSpawn(), intervalOfMobs, intervalOfMobs);
	}

	// Do spawn Antharas.
	private class AntharasSpawn implements Runnable
	{
		private int _taskId = 0;
		private final Collection<L2Character> _players = _Zone.getCharactersInside().values();

		public AntharasSpawn(final int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;

			switch (_taskId)
			{
				case 1: // Spawn.
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					_monsterSpawnTask.cancel(false);
					_monsterSpawnTask = null;
					if (FWA_OLDANTHARAS)
						npcId = 29019; // old
					else if (_players.size() <= FWA_LIMITOFWEAK)
						npcId = 29066; // weak
					else if (_players.size() > FWA_LIMITOFNORMAL)
						npcId = 29068; // strong
					else
						npcId = 29067; // normal
						
					// Do spawn.
					antharasSpawn = _monsterSpawn.get(npcId);
					_antharas = (L2GrandBossInstance) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(_antharas);

					_monsters.add(_antharas);
					_antharas.setIsImmobilized(true);

					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
					GrandBossManager.getInstance().setBossStatus(npcId, FIGHTING);
					_LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);

					// Setting 1st time of minions spawn task.
					if (!FWA_OLDANTHARAS)
						startMinionSpawns(npcId);

					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2), 16);
					break;
				case 2:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, -19, 0, 20000, 0, 0, 1, 0));

					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3), 3000);
					break;
				
				case 3:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, 0, 6000, 20000, 0, 0, 1, 0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4), 10000);
					break;
				case 4:
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 3700, 0, -3, 0, 10000, 0, 0, 1, 0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5), 200);
					break;
				
				case 5:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 22000, 30000, 0, 0, 1, 0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6), 10800);
					break;
				
				case 6:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 300, 7000, 0, 0, 1, 0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7), 1900);
					break;
				
				case 7:
					_antharas.abortCast();

					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_antharas), 16);

					// Move at random.
					if (FWA_MOVEATRANDOM)
					{
						final L2CharPosition pos = new L2CharPosition(getRandom(175000, 178500), getRandom(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_antharas, pos), 500);
					}

					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					break;
			}
		}
	}

	protected void broadcastPacket(final L2GameServerPacket mov)
	{
		if (_Zone != null)
			for (final L2Character characters : _Zone.getCharactersInside().values())
				if (characters instanceof L2PcInstance)
					characters.sendPacket(mov);
	}

	// Do spawn Behemoth or Bomber.
	private class MobsSpawn implements Runnable
	{
		public MobsSpawn()
		{
		}

		@Override
		public void run()
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			final boolean isBehemoth = getRandom(100) < FWA_PERCENTOFBEHEMOTH;
			try
			{
				final int mobNumber = isBehemoth ? 2 : 3;
				// Set spawn.
				for (int i = 0; i < mobNumber; i++)
				{
					if (_monsters.size() >= FWA_MAXMOBS)
						break;
					int npcId;
					if (isBehemoth)
						npcId = 29069;
					else
						npcId = getRandom(29070, 29076);
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new L2Spawn(template1);
					// allocates it at random in the lair of Antharas.
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = (_antharas.getX() - x) * (_antharas.getX() - x) + (_antharas.getY() - y) * (_antharas.getY() - y);
					while (tried++ < 25 && notFound)
					{
						final int rx = getRandom(175000, 179900);
						final int ry = getRandom(112400, 116000);
						final int rdt = (_antharas.getX() - rx) * (_antharas.getX() - rx) + (_antharas.getY() - ry) * (_antharas.getY() - ry);
						if (GeoData.getInstance().canSeeTarget(_antharas.getX(), _antharas.getY(), -7704, rx, ry, -7704))
							if (rdt < dt)
							{
								x = rx;
								y = ry;
								dt = rdt;
								if (rdt <= 900000)
									notFound = false;
							}
					}
					tempSpawn.setLocx(x);
					tempSpawn.setLocy(y);
					tempSpawn.setLocz(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
					SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
					// Do spawn.
					_monsters.add(tempSpawn.doSpawn());
				}
			}
			catch (final Exception e)
			{
				_log.warning(e.getMessage());
			}
		}
	}

	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		switch (npc.getId())
		{
			case 29070:
			case 29071:
			case 29072:
			case 29073:
			case 29074:
			case 29075:
			case 29076:
				if (_selfDestructionTask == null && !npc.isDead())
					_selfDestructionTask = ThreadPoolManager.getInstance().scheduleGeneral(new SelfDestructionOfBomber(npc), FWA_SELFDESTRUCTTIME);
				break;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	// Do self destruction.
	private class SelfDestructionOfBomber implements Runnable
	{
		private final L2Npc _bomber;

		public SelfDestructionOfBomber(final L2Npc bomber)
		{
			_bomber = bomber;
		}

		@Override
		public void run()
		{
			L2Skill skill = null;
			switch (_bomber.getId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}

			_bomber.doCast(skill);
			if (_selfDestructionTask != null)
			{
				_selfDestructionTask.cancel(false);
				_selfDestructionTask = null;
			}
		}
	}

	@Override
	public String onSpellFinished(final L2Npc npc, final L2PcInstance player, final L2Skill skill)
	{
		if (npc.isInvul())
			return null;
		else if (skill != null && (skill.getId() == 5097 || skill.getId() == 5094))
			switch (npc.getId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				case 29076:
					npc.doDie(npc);
					break;
			}
		return super.onSpellFinished(npc, player, skill);
	}

	// At end of activity time.
	protected class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			final Long temp = System.currentTimeMillis() - _LastAction;
			if (temp > FWA_INACTIVITYTIME)
			{
				GrandBossManager.getInstance().setBossStatus(_antharas.getId(), DORMANT);
				setUnspawn();
			}
		}
	}

	// Clean Antharas's lair.
	public void setUnspawn()
	{
		// Eliminate players.
		_Zone.oustAllPlayers();

		// Not executed tasks is canceled.
		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		if (_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if (_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}

		// Delete monsters.
		for (final L2Npc mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		// Delete teleport cube.
		for (final L2Npc cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();
	}

	// Do spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		private final int _type;

		public CubeSpawn(final int type)
		{
			_type = type;
		}

		@Override
		public void run()
		{
			if (_type == 0)
			{
				spawnCube();
				_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(1), 1800000);
			}
			else
				setUnspawn();
		}
	}

	// UnLock Antharas.
	private static class UnlockAntharas implements Runnable
	{
		private final int _bossId;

		public UnlockAntharas(final int bossId)
		{
			_bossId = bossId;
		}

		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(_bossId, DORMANT);
			if (FWA_DOSERVEREARTHQUAKE)
				for (final L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					p.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
		}
	}

	// Action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private final L2GrandBossInstance _boss;

		public SetMobilised(final L2GrandBossInstance boss)
		{
			_boss = boss;
		}

		@Override
		public void run()
		{
			_boss.setIsImmobilized(false);

			// When it is possible to act, a social action is canceled.
			if (_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}

	// Move at random on after Antharas appears.
	private static class MoveAtRandom implements Runnable
	{
		private final L2Npc _npc;
		private final L2CharPosition _pos;

		public MoveAtRandom(final L2Npc npc, final L2CharPosition pos)
		{
			_npc = npc;
			_pos = pos;
		}

		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.getId() == 29019 || npc.getId() == 29066 || npc.getId() == 29067 || npc.getId() == 29068)
		{
			_LastAction = System.currentTimeMillis();
			if (GrandBossManager.getInstance().getBossStatus(_antharas.getId()) != FIGHTING)
				_Zone.oustAllPlayers();
			else if (!FWA_OLDANTHARAS && _mobsSpawnTask == null)
				startMinionSpawns(npc.getId());
		}
		else if (npc.getId() > 29069 && npc.getId() < 29077 && npc.getCurrentHp() <= damage)
		{
			L2Skill skill = null;
			switch (npc.getId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}

			npc.doCast(skill);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getId() == 29019 || npc.getId() == 29066 || npc.getId() == 29067 || npc.getId() == 29068)
		{
			npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(0), 10000);
			GrandBossManager.getInstance().setBossStatus(npc.getId(), DEAD);
			final long respawnTime = (long) Config.Interval_Of_Antharas_Spawn + getRandom(Config.Random_Of_Antharas_Spawn);
			ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(npc.getId()), respawnTime);
			// also save the respawn time so that the info is maintained past reboots
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(npc.getId());
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(npc.getId(), info);
		}
		else if (npc.getId() == 29069)
		{
			final int countHPHerb = getRandom(6, 18);
			final int countMPHerb = getRandom(6, 18);
			for (int i = 0; i < countHPHerb; i++)
				((L2MonsterInstance) npc).dropItem(killer, 8602, 1);
			for (int i = 0; i < countMPHerb; i++)
				((L2MonsterInstance) npc).dropItem(killer, 8605, 1);
		}
		if (_monsters.contains(npc))
			_monsters.remove(npc);
		return super.onKill(npc, killer, isPet);
	}
}