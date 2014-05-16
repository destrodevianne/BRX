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
package ct25.xtreme.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.instancemanager.tasks.UpdateSoDStateTask;
import ct25.xtreme.gameserver.instancemanager.tasks.UpdateSoIStateTask;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.util.Rnd;

/**
 ** @author Browser 
 */
public class GraciaSeedsManager 
{
	private static final Logger _log = Logger.getLogger(GraciaSeedsManager.class.getName());
	
	// Engine for Seeds
	public static String ENERGY_SEEDS = "EnergySeeds";
	
	// Types for save data
	private static final byte SODTYPE = 1;
	private static final byte SOITYPE = 2;
	private static final byte SOATYPE = 3;
	
	// Seed of Destruction
	private static int 						_SoDTiatKilled = 0;
	private static int						_SoDState = 1;
	private static Calendar 				_SoDLastStateChangeDate;
	private static Calendar 				_SoDResetDate;
	private static ScheduledFuture<?> 		_resetTask = null;
	
	// Seed of Infinity
	private static int						_SoITwinKilled = 0;
	private static int						_SoICohemenesKilled = 0;
	private static int						_SoIEkimusKilled = 0;
	private static int						_SoIState = 1;
	private static Calendar					_SoINextData;
	private static ScheduledFuture<?> 		_scheduleSOINextStage = null;
	private static long 					_timeStepMin = 48 * 60 * 60 * 1000; //48 hours
	private static long 					_timeStepMax = 72 * 60 * 60 * 1000; //72 hours
	
	// Items Seed Of Infinity
	private static int[] _itemsSoI = {13691,13692};
	
	protected GraciaSeedsManager()
	{
		_log.info(getClass().getSimpleName() + ": Initializing");
		_SoDLastStateChangeDate = Calendar.getInstance();
		_SoDResetDate = Calendar.getInstance();
		_SoINextData = Calendar.getInstance();
		
		loadData();
		
		// Destruction
		handleSodStages();
		scheduleSodReset();
		
		// Infinity
		handleSoIStages(true);
		
		_log.info(getClass().getSimpleName() + ": Seed Of Destruction stage = " + getSoDState());
		_log.info(getClass().getSimpleName() + ": Seed Of Infinity stage = " + getSoIState());
		if (getSoIState() >= 3)
		_log.info(getClass().getSimpleName() + ": Seed Of Infinity next State data: " + _SoINextData.getTime());
	}

	public void saveData(byte seedType)
	{
		switch (seedType)
		{
			case SODTYPE:
				// Seed of Destruction
				GlobalVariablesManager.getInstance().set("SoDState", _SoDState);
				GlobalVariablesManager.getInstance().set("SoDTiatKilled", _SoDTiatKilled);
				GlobalVariablesManager.getInstance().set("SoDLSCDate", _SoDLastStateChangeDate.getTimeInMillis());
				GlobalVariablesManager.getInstance().set("SoDResetDate", _SoDResetDate.getTimeInMillis());
				break;
			case SOITYPE:
				// Seed of Infinity
				GlobalVariablesManager.getInstance().set("SoITwinKilled", _SoITwinKilled);
				GlobalVariablesManager.getInstance().set("SoICohemenesKilled", _SoICohemenesKilled);
				GlobalVariablesManager.getInstance().set("SoIEkimusKilled", _SoIEkimusKilled);
				GlobalVariablesManager.getInstance().set("SoIState", _SoIState);
				GlobalVariablesManager.getInstance().set("SoINextData", _SoINextData.getTimeInMillis());
				break;
			case SOATYPE:
				// Seed of Annihilation (handle by dp script)
				break;
			default:
				_log.warning(getClass().getSimpleName() + ": Unknown SeedType in SaveData: " + seedType);
				break;
		}
	}
	
	public void loadData()
	{
		// Seed of Destruction variables
		if (GlobalVariablesManager.getInstance().hasVariable("SoDState"))
		{
			_SoDState = GlobalVariablesManager.getInstance().getInt("SoDState");
			_SoDTiatKilled = GlobalVariablesManager.getInstance().getInt("SoDTiatKilled");
			_SoDLastStateChangeDate.setTimeInMillis(GlobalVariablesManager.getInstance().getLong("SoDLSCDate"));
			_SoDResetDate.setTimeInMillis(GlobalVariablesManager.getInstance().getLong("SoDResetDate"));
		}
		else
		{
			// save Initial values
			saveData(SODTYPE);
		}
		
		// Seed of Infinity variables
		if (GlobalVariablesManager.getInstance().hasVariable("SoIState"))
		{
			_SoIState = GlobalVariablesManager.getInstance().getInt("SoIState");
			_SoITwinKilled = GlobalVariablesManager.getInstance().getInt("SoITwinKilled");
			_SoICohemenesKilled = GlobalVariablesManager.getInstance().getInt("SoICohemenesKilled");
			_SoIEkimusKilled = GlobalVariablesManager.getInstance().getInt("SoIEkimusKilled");
			_SoINextData.setTimeInMillis(GlobalVariablesManager.getInstance().getLong("SoINextData"));
		}
		else
		{
			// save Initial values
			saveData(SOITYPE);
		}
	}

	private void handleSodStages()
	{
		switch(_SoDState)
		{
			case 1:
				// do nothing, players should kill Tiat a few times
				break;
			case 2:
				// Conquest Complete state, if too much time is passed than change to defense state
				long timePast = System.currentTimeMillis() - _SoDLastStateChangeDate.getTimeInMillis();
				if (timePast >= Config.SOD_STAGE_2_LENGTH)
				{
					// change to Defense state
					setSoDState(3, true);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleEffect(new UpdateSoDStateTask(), Config.SOD_STAGE_2_LENGTH - timePast);
				}
				break;
			case 3:
				// handled by DP script
				break;
			default:
				_log.warning(getClass().getSimpleName() + ": Unknown Seed of Destruction state(" + _SoDState + ")! ");
		}
	}
	
	public void updateSodState()
	{
		final Quest quest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
		if (quest == null)
		{
			_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
		}
		else
		{
			quest.notifyEvent("StopSoDAi", null, null);
		}
	}

	public void increaseSoDTiatKilled()
	{
		if (_SoDState == 1)
		{
			_SoDTiatKilled++;
			if (_SoDTiatKilled >= Config.SOD_TIAT_KILL_COUNT)
			{
				setSoDState(2, false);
			}
			saveData(SODTYPE);
			// Start Energy Seeds AI
			Quest esQuest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
			if (esQuest == null)
			{
				_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
			}
			else
			{
				esQuest.notifyEvent("StartSoDAi", null, null);
			}
		}
	}

	public int getSoDTiatKilled()
	{
		return _SoDTiatKilled;
	}
	
	public synchronized void setSoDState(int value, boolean doSave)
	{
		_log.info(getClass().getSimpleName() + ": New Seed of Destruction state -> " + value + ".");
		_SoDLastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		_SoDState = value;
		// reset number of Tiat kills
		if (_SoDState == 1)
		{
			_SoDTiatKilled = 0;
		}
	
		if (doSave)
		{
			saveData(SODTYPE);
		}
	}
	
	public long getSoDTimeForNextStateChange()
	{
		switch(_SoDState)
		{
			case 1:
				return -1;
			case 2:
				return (_SoDLastStateChangeDate.getTimeInMillis() + Config.SOD_STAGE_2_LENGTH - System.currentTimeMillis());
			case 3:
				// not implemented yet
				return -1;
			default:
				// this should not happen!
				return -1;
		}
	}
	
	public Calendar getSoDLastStateChangeDate()
	{
		return _SoDLastStateChangeDate;
	}
	
	public int getSoDState()
	{
		return _SoDState;
	}
	
	private void scheduleSodReset()
	{   
		long timeLeft = 0;
		long resetDate = _SoDResetDate.getTimeInMillis();
		if (resetDate <= System.currentTimeMillis())//if so, SoD will open after 1 sec
		_log.warning(getClass().getSimpleName() + ": Seed Of Destruction restart date = " + _SoDResetDate.getTime());
		else timeLeft = resetDate - System.currentTimeMillis();
		if (_resetTask != null)
		{
			_resetTask.cancel(false);
			_resetTask = null;
		}
		_resetTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				sodReset();
			}
		}, timeLeft);
	}
	
	private void sodReset()
	{
		Calendar calendar;
		calendar = Calendar.getInstance();
		
		int nowDay = calendar.get(Calendar.DAY_OF_WEEK); 
		switch (nowDay)
		{
			case Calendar.SUNDAY:
			case Calendar.MONDAY:
			case Calendar.TUESDAY:
			case Calendar.SATURDAY:
				calendar.add(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY - nowDay);
				break;
			case Calendar.WEDNESDAY:
			case Calendar.THURSDAY:
			case Calendar.FRIDAY:
				calendar.add(Calendar.DAY_OF_WEEK, Calendar.SATURDAY - nowDay);
				break;
		}
		
		// Set the exact hour, mins, secs, milisecs
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		while (calendar.getTimeInMillis() < System.currentTimeMillis())
			calendar.add(Calendar.DAY_OF_MONTH, 7);
		// Set the new date when Seed Of Destruction resets
		_SoDResetDate = calendar;
		_log.info(getClass().getSimpleName() + ": Seed of Destruction rescheduled to start at = " + _SoDResetDate.getTime());

		setSoDState(1, true);
		handleSodStages();
		scheduleSodReset();
	}
	
	public long getSoDResetDate()
	{
		return _SoDResetDate.getTimeInMillis();
	}
	
	
	// Seed Of Infinity  --------------------------------
	public void handleSoIStages(boolean onLoad)
	{
		long timeStep = Rnd.get(_timeStepMin, _timeStepMax);
		switch (getSoIState()) 
		{
		case 1:
			if (getCohemenesKillCounts() >= Config.SOI_COHEMENES_KILL_COUNT && getTwinKillCounts() >= Config.SOI_TWIN_KILL_COUNT)
			{
				_SoICohemenesKilled = 0;
				_SoITwinKilled = 0;
				setSoIStage(2, true);
			}
			break;
		case 2:
			if (getEkimusKillCounts() >= Config.SOI_EKIMUS_KILL_COUNT)
			{
				_SoIEkimusKilled = 0;
				setSoIStage(3, true);
				handleSoIStages(false);
			}
			break;
		case 3:
			if (onLoad)
			{
				if (_SoINextData.getTimeInMillis() <= System.currentTimeMillis())
					ThreadPoolManager.getInstance().scheduleEffect(new UpdateSoIStateTask(4), 1000);
				else
					ThreadPoolManager.getInstance().scheduleEffect(new UpdateSoIStateTask(4), _SoINextData.getTimeInMillis() - System.currentTimeMillis());
					
			}
			else
			{
				_SoITwinKilled = 0;
				_SoINextData.setTimeInMillis(System.currentTimeMillis() + timeStep);
				if (_scheduleSOINextStage != null)
					_scheduleSOINextStage.cancel(false);
				ThreadPoolManager.getInstance().scheduleEffect(new UpdateSoIStateTask(4), timeStep);
				saveData(SOITYPE);
			}
			break;
		case 4:
			if (getCohemenesKillCounts() >= Config.SOI_COHEMENES_KILL_COUNT && getTwinKillCounts() >= Config.SOI_TWIN_KILL_COUNT)
			{
				_SoITwinKilled = 0;
				_SoICohemenesKilled = 0;
				setSoIStage(5, true);
			}
			break;
		case 5:
			if (getEkimusKillCounts() >= Config.SOI_EKIMUS_KILL_COUNT)
			{
				_SoIEkimusKilled = 0;
				setSoIStage(1, true);
			}
			break;
		default:
			break;
		}
	}
	
	public synchronized void setSoIStage(int value, boolean save)
	{
		_log.info(getClass().getSimpleName() + ": New Seed of Infinity state -> " + value + ".");
		_SoINextData.setTimeInMillis(System.currentTimeMillis());
		_SoIState = value;
		// If stage 1, clear all values
		if (_SoIState == 1)
		{
			_SoITwinKilled = 0;
			_SoICohemenesKilled = 0;
			_SoIEkimusKilled = 0;
		}
		if (save)
		{
			saveData(SOITYPE);
		}
	}
	
	public int getSoIState()
	{ 
		return _SoIState;
	}
	
	public int getTwinKillCounts()
	{ 
		return _SoITwinKilled;
	}
	
	public int getCohemenesKillCounts()
	{ 
		return _SoICohemenesKilled;
	}
	
	public int getEkimusKillCounts()
	{ 
		return _SoIEkimusKilled;
	}
	
	public void updateSoIState()
	{
		final Quest quest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
		if (quest == null)
		{
			_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
		}
		else
		{
			quest.notifyEvent("StopSoIAi", null, null);
		}
	}
			
	public void addTwinKill()
	{
		_SoITwinKilled++;
		handleSoIStages(false);
		{
			saveData(SOITYPE);
		}
	}
	
	public void addCohemenesKill()
	{
		_SoICohemenesKilled++;
		handleSoIStages(false);
		{
			saveData(SOITYPE);
		}
	}
	
	public void addEkimusKill()
	{
		_SoIEkimusKilled++;
		handleSoIStages(false);
		{
			saveData(SOITYPE);
			// Start Energy Seeds AI
			Quest esQuest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
			if (esQuest == null)
			{
				_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
			}
			else
			{
				esQuest.notifyEvent("StartSoIAi", null, null);
			}
		}
	}
	
	public long getSoITimeForNextStateChange()
	{
		switch(_SoIState)
		{
			case 1:
				return -1;
			case 2:
				return -1;
			case 3:
				return (_SoINextData.getTimeInMillis() - System.currentTimeMillis());
			case 4:
				return -1;
			case 5:
				return -1;
			default:
				// this should not happen!
				return -1;
		}
	}
	
	public void clearItems()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player != null)
			{
				for (int itemId : _itemsSoI)
					player.getInventory().destroyItem("GraciaSeedsManager", itemId, 1, player, null);
			}
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM items WHERE item_id = "+_itemsSoI[0]+" OR item_id = "+_itemsSoI[1]);

			statement.executeQuery();
			statement.close();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	public enum GraciaSeeds
	{
		INFINITY,
		DESTRUCTION,
		ANNIHILATION_BISTAKON,
		ANNIHILATION_REPTILIKON,
		ANNIHILATION_COKRAKON
	}
	
	public static final GraciaSeedsManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final GraciaSeedsManager _instance = new GraciaSeedsManager();
	}
}
