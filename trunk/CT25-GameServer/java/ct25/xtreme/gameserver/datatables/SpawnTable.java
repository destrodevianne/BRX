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
package ct25.xtreme.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import javolution.util.FastSet;

import ct25.xtreme.Config;
import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.instancemanager.DayNightSpawnManager;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.interfaces.IL2Procedure;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;

/**
 * This class ...
 *
 * @author Nightmare/ Browser / Zoey76
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2013/04/26 15:29:18 $
 */
public final class SpawnTable
{
	private static final Logger _log = Logger.getLogger(SpawnTable.class.getName());
	// SQL
	private static final String SELECT_SPAWNS = "SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist";
	private static final String SELECT_CUSTOM_SPAWNS = "SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM custom_spawnlist";
	
	private static final Map<Integer, Set<L2Spawn>> _spawnTable = new FastMap<Integer, Set<L2Spawn>>().shared();
	
	protected SpawnTable()
	{
		load();
	}
	
	/**
	 * Wrapper to load all spawns.
	 */
	public void load()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			fillSpawnTable(false);
			final int spawnCount = _spawnTable.size();
			_log.info(getClass().getSimpleName() + ": Loaded " + spawnCount + " npc spawns.");
			if (Config.CUSTOM_SPAWNLIST_TABLE)
			{
				fillSpawnTable(true);
				_log.info(getClass().getSimpleName() + ": Loaded " + (_spawnTable.size() - spawnCount) + " custom npc spawns.");
			}
		}
	}
	
	/**
	 * Retrieves spawn data from database.
	 * @param isCustom if {@code true} the spawns are loaded as custom from custom spawn table
	 * @return the spawn count
	 */
	private int fillSpawnTable(boolean isCustom)
	{
		int npcSpawnCount = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(isCustom ? SELECT_CUSTOM_SPAWNS : SELECT_SPAWNS))
		{
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			int npcId;
			while (rs.next())
			{
				npcId = rs.getInt("npc_templateid");
				template1 = NpcTable.getInstance().getTemplate(npcId);
				if (template1 == null)
				{
					_log.warning(getClass().getSimpleName() + ": Data missing in NPC table for ID: " + npcId + ".");
					continue;
				}
				
				if (template1.isType("L2SiegeGuard") || template1.isType("L2RaidBoss") || (!Config.ALLOW_CLASS_MASTERS && template1.isType("L2ClassMaster")))
				{
					// Don't spawn
					continue;
				}
				
				spawnDat = new L2Spawn(template1);
				spawnDat.setAmount(rs.getInt("count"));
				spawnDat.setLocx(rs.getInt("locx"));
				spawnDat.setLocy(rs.getInt("locy"));
				spawnDat.setLocz(rs.getInt("locz"));
				spawnDat.setHeading(rs.getInt("heading"));
				spawnDat.setRespawnDelay(rs.getInt("respawn_delay"));
				int loc_id = rs.getInt("loc_id");
				spawnDat.setLocation(loc_id);
				
				switch (rs.getInt("periodOfDay"))
				{
					case 0: // default
						npcSpawnCount += spawnDat.init();
						break;
					case 1: // Day
						DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
						npcSpawnCount++;
						break;
					case 2: // Night
						DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
						npcSpawnCount++;
						break;
				}
				
				addSpawn(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Spawn could not be initialized: " + e.getMessage(), e);
		}
		return npcSpawnCount;
	}
	
	public Map<Integer, Set<L2Spawn>> getSpawnTable()
	{
		return _spawnTable;
	}
	
	/**
	 * Get the spawns for the NPC Id.
	 * @param npcId the NPC Id
	 * @return the spawn set for the given npcId
	 */
	public Set<L2Spawn> getSpawns(int npcId)
	{
		return _spawnTable.containsKey(npcId) ? _spawnTable.get(npcId) : Collections.<L2Spawn> emptySet();
	}
	
	/**
	 * Get the first NPC spawn.
	 * @param npcId the NPC Id to search
	 * @return the first not null spawn, if any
	 */
	public L2Spawn getFirstSpawn(int npcId)
	{
		if (_spawnTable.containsKey(npcId))
		{
			for (L2Spawn spawn : _spawnTable.get(npcId))
			{
				if (spawn != null)
				{
					return spawn;
				}
			}
		}
		return null;
	}
	
	/**
	 * Add a new spawn to the spawn table.
	 * @param spawn the spawn to add
	 * @param storeInDb if {@code true} it'll be saved in the database
	 */
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		addSpawn(spawn);
		
		if (storeInDb)
		{
			final String spawnTable = spawn.isCustom() && Config.CUSTOM_SPAWNLIST_TABLE ? "custom_spawnlist" : "spawnlist";
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO " + spawnTable + "(count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?)"))
			{
				statement.setInt(1, spawn.getAmount());
				statement.setInt(2, spawn.getNpcid());
				statement.setInt(3, spawn.getLocx());
				statement.setInt(4, spawn.getLocy());
				statement.setInt(5, spawn.getLocz());
				statement.setInt(6, spawn.getHeading());
				statement.setInt(7, spawn.getRespawnDelay() / 1000);
				statement.setInt(8, spawn.getLocation());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not store spawn in the DB:" + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Delete an spawn from the spawn table.
	 * @param spawn the spawn to delete
	 * @param updateDb if {@code true} database will be updated
	 */
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (!removeSpawn(spawn))
		{
			return;
		}
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM " + (spawn.isCustom() ? "custom_spawnlist" : "spawnlist") + " WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?"))
			{
				statement.setInt(1, spawn.getLocx());
				statement.setInt(2, spawn.getLocy());
				statement.setInt(3, spawn.getLocz());
				statement.setInt(4, spawn.getNpcid());
				statement.setInt(5, spawn.getHeading());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, getClass().getSimpleName() + ": Spawn " + spawn + " could not be removed from DB: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Add a spawn to the spawn set if present, otherwise add a spawn set and add the spawn to the newly created spawn set.
	 * @param spawn the NPC spawn to add
	 */
	private void addSpawn(L2Spawn spawn)
	{
		if (!_spawnTable.containsKey(spawn.getNpcid()))
		{
			_spawnTable.put(spawn.getNpcid(), new FastSet<L2Spawn>().shared());
		}
		_spawnTable.get(spawn.getNpcid()).add(spawn);
	}
	
	/**
	 * Remove a spawn from the spawn set, if the spawn set is empty, remove it as well.
	 * @param spawn the NPC spawn to remove
	 * @return {@code true} if the spawn was successfully removed, {@code false} otherwise
	 */
	private boolean removeSpawn(L2Spawn spawn)
	{
		if (_spawnTable.containsKey(spawn.getNpcid()))
		{
			final Set<L2Spawn> set = _spawnTable.get(spawn.getNpcid());
			boolean removed = set.remove(spawn);
			if (set.isEmpty())
			{
				_spawnTable.remove(spawn.getNpcid());
			}
			return removed;
		}
		return false;
	}
	
	/**
	 * Execute a procedure over all spawns.<br>
	 * <font size="4" color="red">Do not use it!</font>
	 * @param procedure the procedure to execute
	 * @return {@code true} if all procedures were executed, {@code false} otherwise
	 */
	public boolean forEachSpawn(IL2Procedure<L2Spawn> procedure)
	{
		for (Set<L2Spawn> set : _spawnTable.values())
		{
			for (L2Spawn spawn : set)
			{
				if (!procedure.execute(spawn))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnTable _instance = new SpawnTable();
	}
}