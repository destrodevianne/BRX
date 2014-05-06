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
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.interfaces.IL2Seed;
import ct25.xtreme.util.Rnd;

public class GraciaSeedsManager 
{
	private static final Logger _log = Logger.getLogger(GraciaSeedsManager.class.getName());
	
	//Types for save data
	private static final byte SODTYPE = 1;
	private static final byte SOITYPE = 2;
	private static final byte SOATYPE = 3;
	
	// Seed of Destruction
	private static int 						_SoDTiatKilled = 0;
	private static int						_SoDState = 1;
	private static Calendar 				_SoDLastStateChangeDate;
	private static Calendar 				_SoDResetDate;
	private static ScheduledFuture<?> 		_resetTask = null;
	private static ScheduledFuture<?> 		_scheduleStartAITask = null;
	private static IL2Seed 					_seedEnergy;
	
	// Seed of Infinity
	private static int						_SoIKilled = 0;
	private static int						_SoIState = 1;
	private static Calendar					_SoINextData;
	private static ScheduledFuture<?> 		_scheduleSOINextStage = null;
	private static long 					_timeStepMin = 48 * 60 * 60 * 1000; //48 hours
	private static long 					_timeStepMax = 72 * 60 * 60 * 1000; //72 hours
	
	//Items
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
		_log.info(getClass().getSimpleName() + ": Seed Of Infinity nextState data: " + _SoINextData.getTime());
	}

	public void saveData(byte seedType)
	{
		switch (seedType)
		{
			case SODTYPE:
				// Destruction
				GlobalVariablesManager.getInstance().set("SoDState", _SoDState);
				GlobalVariablesManager.getInstance().set("SoDTiatKilled", _SoDTiatKilled);
				GlobalVariablesManager.getInstance().set("SoDLSCDate", _SoDLastStateChangeDate.getTimeInMillis());
				GlobalVariablesManager.getInstance().set("SoDResetDate", _SoDResetDate.getTimeInMillis());
				break;
			case SOITYPE:
				// Infinity
				GlobalVariablesManager.getInstance().set("SoIKilled", _SoIKilled);
				GlobalVariablesManager.getInstance().set("SoIState", _SoIState);
				GlobalVariablesManager.getInstance().set("SoINextData", _SoINextData.getTimeInMillis());
				break;
			case SOATYPE:
				// Seed of Annihilation
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
		
		// Seed 0f Infinity variables
		if (GlobalVariablesManager.getInstance().hasVariable("SoIState"))
		{
			_SoIState = GlobalVariablesManager.getInstance().getInt("SoIState");
			_SoIKilled = GlobalVariablesManager.getInstance().getInt("SoIKilled");
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
				break;
			case 3:
				// handled by DP script
				break;
			default:
				_log.warning(getClass().getSimpleName() + ": Unknown Seed of Destruction state(" + _SoDState + ")! ");
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
		
		if (_SoDState == 3 && _seedEnergy != null)
			_seedEnergy.startAI(GraciaSeedTypes.DESTRUCTION);
		else if (_seedEnergy != null)
			_seedEnergy.stopAI(GraciaSeedTypes.DESTRUCTION);
		
		if (_SoDState == 2) //need schedule energy seed
		{
			long timeLeft = GraciaSeedsManager.getInstance().getSoDTimeForNextStateChange();
			scheduleSoDEnergy(timeLeft);
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
		_log.warning(getClass().getSimpleName() + ": scheduleSodReset("+resetDate+"): the given date has already passed! Restart date = "+_SoDResetDate.getTime()+" in mills= "+_SoDResetDate.getTimeInMillis());
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
		
		//set the exact hour, mins, secs, milisecs
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		while (calendar.getTimeInMillis() < System.currentTimeMillis())
			calendar.add(Calendar.DAY_OF_MONTH, 7);
		//set the new date when SoD resets
		_SoDResetDate = calendar;
		_log.info(getClass().getSimpleName() + ": Seed of Destruction rescheduled to start at = " + _SoDResetDate.getTime());

		setSoDState(1, true);
		handleSodStages();
		scheduleSodReset();
	}

	public synchronized void registerSeed(IL2Seed seed)
	{
		_seedEnergy = seed;
	}
	
	private void scheduleSoDEnergy(long timeLeft)
	{
		if (_scheduleStartAITask != null)
		{
			_scheduleStartAITask.cancel(false);
			_scheduleStartAITask = null;
		}
		_log.info(getClass().getSimpleName() + ": SoD Energy Seeds rescheduled to start after " +Config.SOD_STAGE_2_LENGTH+ " min");
		_scheduleStartAITask = ThreadPoolManager.getInstance().scheduleEffect(new Runnable()
		{
			public void run() { SoDEnergy(); }
		}, timeLeft);
	}
	
	private void SoDEnergy()
	{
		setSoDState(3, true);
	}
	
	public long getSoDResetDate()
	{
		return _SoDResetDate.getTimeInMillis();
	}
	
	// Seed Of Infinity  --------------------------------
	private void handleSoIStages(boolean onLoad)
	{
		long timeStep = Rnd.get(_timeStepMin, _timeStepMax);
		switch (getSoIState()) 
		{
		case 1:
			if (getSoIKilled() >= 20)
			{
				_SoIKilled = 0;
				setSoIStage(2, true);
			}
			break;
		case 2:
			if (getSoIKilled() >= 5)
			{
				_SoIKilled = 0;
				setSoIStage(3, true);
				handleSoIStages(false);
			}
			break;
		case 3:
			if (onLoad)
			{
				if (_SoINextData.getTimeInMillis() <= System.currentTimeMillis())
					ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(4), 1000);
				else
					ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(4), _SoINextData.getTimeInMillis() - System.currentTimeMillis());
					
			}
			else
			{
				_SoIKilled = 0;
				_SoINextData.setTimeInMillis(System.currentTimeMillis() + timeStep);
				if (_scheduleSOINextStage != null)
					_scheduleSOINextStage.cancel(false);
				ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(4), timeStep);
				saveData(SOITYPE);
			}
			break;
		case 4:
			if (onLoad)
			{
				if (_SoINextData.getTimeInMillis() <= System.currentTimeMillis())
					ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(5), 1000);
				else
					ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(5), _SoINextData.getTimeInMillis() - System.currentTimeMillis());
					
			}
			else
			{
				_SoIKilled = 0;
				_SoINextData.setTimeInMillis(System.currentTimeMillis() + timeStep);
				if (_scheduleSOINextStage != null)
					_scheduleSOINextStage.cancel(false);
				ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(5), timeStep);
				saveData(SOITYPE);
			}
			break;
		case 5:
			if (onLoad)
			{
				if (_SoINextData.getTimeInMillis() <= System.currentTimeMillis())
					ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(1), 1000);
				else
					ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(1), _SoINextData.getTimeInMillis() - System.currentTimeMillis());
					
			}
			else
			{
				_SoIKilled = 0;
				_SoINextData.setTimeInMillis(System.currentTimeMillis() + timeStep);
				if (_scheduleSOINextStage != null)
					_scheduleSOINextStage.cancel(false);
				ThreadPoolManager.getInstance().scheduleEffect(new SOINextStateTask(1), timeStep);
				saveData(SOITYPE);
			}
			break;
		default:
			break;
		}
	}
	
	private synchronized void setSoIStage(int value, boolean save)
	{
		_SoIState = value;
		
		if (_SoDState == 3 && _seedEnergy != null)
		{
			_seedEnergy.startAI(GraciaSeedTypes.INFINITY_SUFFERING);
			_seedEnergy.startAI(GraciaSeedTypes.INFINITY_EROSION);
			_seedEnergy.startAI(GraciaSeedTypes.INFINITY_INFINITY);
		}
		else if (_SoDState == 4 && _seedEnergy != null)
		{
			_seedEnergy.stopAI(GraciaSeedTypes.INFINITY_SUFFERING);
			_seedEnergy.stopAI(GraciaSeedTypes.INFINITY_EROSION);
			_seedEnergy.startAI(GraciaSeedTypes.INFINITY_INFINITY);
		}
		else if (_SoDState == 5 && _seedEnergy != null)
		{
			_seedEnergy.stopAI(GraciaSeedTypes.INFINITY_SUFFERING);
			_seedEnergy.startAI(GraciaSeedTypes.INFINITY_EROSION);
			_seedEnergy.startAI(GraciaSeedTypes.INFINITY_INFINITY);
		}
		else if (_seedEnergy != null)
		{
			_seedEnergy.stopAI(GraciaSeedTypes.INFINITY_SUFFERING);
			_seedEnergy.stopAI(GraciaSeedTypes.INFINITY_EROSION);
			_seedEnergy.stopAI(GraciaSeedTypes.INFINITY_INFINITY);
		}
			
		
		if (save)
		{
			saveData(SOITYPE);
		}
	}
	
	public int getSoIState()
	{ return _SoIState;}
	
	public int getSoIKilled()
	{ return _SoIKilled;}
	
	public void addSoIKill(int value)
	{
		_SoIKilled += value;
		handleSoIStages(false);
		saveData(SOITYPE);
	}
	
	class SOINextStateTask implements Runnable
	{
		private int _nextState;
		public SOINextStateTask(int value)
		{ _nextState = value;}
		
		@Override
		public void run()
		{
			setSoIStage(_nextState, true);
			handleSoIStages(false);
			if (_nextState == 1)
				clearItems();
		}
	}
	
	private void clearItems()
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
	
	public static enum GraciaSeedTypes
	{
		INFINITY_SUFFERING,
		INFINITY_EROSION,
		INFINITY_INFINITY,
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
