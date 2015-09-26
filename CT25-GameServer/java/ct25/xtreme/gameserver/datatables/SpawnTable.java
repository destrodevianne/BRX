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
	
	private static final Map<Integer, Set<L2Spawn>> _spawnTable = new FastMap<Integer, Set<L2Spawn>>().shared();
	private int _npcSpawnCount;
	private int _customSpawnCount;
	
	/**
	 * Wrapper to load all spawns.
	 */
	public void reloadAll()
	{
		fillSpawnTable();
	}
	
	private SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
			fillSpawnTable();
	}
	
	/**
	 * Retrieves spawn data from database.
	 * @param isCustom if {@code true} the spawns are loaded as custom from custom spawn table
	 * @return the spawn count
	 */
	private void fillSpawnTable()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist");
			ResultSet rset = statement.executeQuery();
			
			L2Spawn spawnDat;
			L2NpcTemplate template1;
			
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
					{
						// Don't spawn
					}
					else if (template1.type.equalsIgnoreCase("L2RaidBoss"))
					{
						// Don't spawn raidboss
					}
					else if(!Config.HITMAN_ENABLE_EVENT && template1.type.equals("L2Hitman"))
					{
						// You got the idea...
					}
					else if (!Config.ALLOW_CLASS_MASTERS && template1.type.equals("L2ClassMaster"))
					{
						// Dont' spawn class masters
					}
					else
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						int loc_id = rset.getInt("loc_id");
						spawnDat.setLocation(loc_id);
				
				switch (rset.getInt("periodOfDay"))
				{
					case 0: // default
						_npcSpawnCount += spawnDat.init();
						break;
					case 1: // Day
						DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
						_npcSpawnCount++;
						break;
					case 2: // Night
						DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
						_npcSpawnCount++;
						break;
					}
				
					addSpawn(spawnDat);
					}
				}
				else
				{
					_log.warning("SpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "SpawnTable: Spawn could not be initialized: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		_log.info("SpawnTable: Loaded " + _spawnTable.size() + " Npc Spawn Locations.");

	if (Config.CUSTOM_SPAWNLIST_TABLE)
	{
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM custom_spawnlist");
			ResultSet rset = statement.executeQuery();
		
			L2Spawn spawnDat;
			L2NpcTemplate template1;
		
			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
					{
						// Don't spawn
					}
					else if (template1.type.equalsIgnoreCase("L2RaidBoss"))
					{
						// Don't spawn raidboss
					}
					else if (!Config.ALLOW_CLASS_MASTERS && template1.type.equals("L2ClassMaster"))
					{
						// Dont' spawn class masters
					}
					else
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						spawnDat.setCustom(true);
						int loc_id = rset.getInt("loc_id");
						spawnDat.setLocation(loc_id);
					
						switch (rset.getInt("periodOfDay"))
						{
						case 0: // default
							_customSpawnCount += spawnDat.init();
							break;
						case 1: // Day
							DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
							_customSpawnCount++;
							break;
						case 2: // Night
							DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
							_customSpawnCount++;
							break;
						}
					
						addSpawn(spawnDat);
					}
				}
				else
				{
					_log.warning("CustomSpawnTable: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.log(Level.WARNING, "CustomSpawnTable: Spawn could not be initialized: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_log.info("CustomSpawnTable: Loaded " + _customSpawnCount + " Npc Spawn Locations.");
	
	}

	if (Config.DEBUG)
		_log.fine("SpawnTable: Spawning completed, total number of NPCs in the world: " + (_npcSpawnCount + _customSpawnCount));

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
				statement.setInt(2, spawn.getId());
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
				statement.setInt(4, spawn.getId());
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
		if (!_spawnTable.containsKey(spawn.getId()))
		{
			_spawnTable.put(spawn.getId(), new FastSet<L2Spawn>().shared());
		}
		_spawnTable.get(spawn.getId()).add(spawn);
	}
	
	/**
	 * Remove a spawn from the spawn set, if the spawn set is empty, remove it as well.
	 * @param spawn the NPC spawn to remove
	 * @return {@code true} if the spawn was successfully removed, {@code false} otherwise
	 */
	private boolean removeSpawn(L2Spawn spawn)
	{
		if (_spawnTable.containsKey(spawn.getId()))
		{
			final Set<L2Spawn> set = _spawnTable.get(spawn.getId());
			boolean removed = set.remove(spawn);
			if (set.isEmpty())
			{
				_spawnTable.remove(spawn.getId());
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

	/**
	 * Finds a spawn for the given NPC ID.
	 * @param npcId the NPC Id
	 * @return a spawn for the given NPC ID or {@code null}
	 */
	public L2Spawn findAny(int npcId)
	{
		return getSpawns(npcId).stream().findFirst().orElse(null);
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