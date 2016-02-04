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

package ct25.xtreme.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import ct25.xtreme.Config;
import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.Server;
import ct25.xtreme.gameserver.cache.CrestCache;
import ct25.xtreme.gameserver.cache.HtmCache;
import ct25.xtreme.gameserver.datatables.AccessLevels;
import ct25.xtreme.gameserver.datatables.AdminCommandAccessRights;
import ct25.xtreme.gameserver.datatables.ArmorSetsTable;
import ct25.xtreme.gameserver.datatables.AugmentationData;
import ct25.xtreme.gameserver.datatables.CharNameTable;
import ct25.xtreme.gameserver.datatables.CharTemplateTable;
import ct25.xtreme.gameserver.datatables.ClanTable;
import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.datatables.EnchantGroupsTable;
import ct25.xtreme.gameserver.datatables.EnchantHPBonusData;
import ct25.xtreme.gameserver.datatables.EventDroplist;
import ct25.xtreme.gameserver.datatables.FishTable;
import ct25.xtreme.gameserver.datatables.GMSkillTable;
import ct25.xtreme.gameserver.datatables.HelperBuffTable;
import ct25.xtreme.gameserver.datatables.HennaTable;
import ct25.xtreme.gameserver.datatables.HennaTreeTable;
import ct25.xtreme.gameserver.datatables.HerbDropTable;
import ct25.xtreme.gameserver.datatables.HeroSkillTable;
import ct25.xtreme.gameserver.datatables.ItemTable;
import ct25.xtreme.gameserver.datatables.LevelUpData;
import ct25.xtreme.gameserver.datatables.MapRegionTable;
import ct25.xtreme.gameserver.datatables.MerchantPriceConfigTable;
import ct25.xtreme.gameserver.datatables.ModsBufferSchemeTable;
import ct25.xtreme.gameserver.datatables.ModsBufferSkillTable;
import ct25.xtreme.gameserver.datatables.MultiSell;
import ct25.xtreme.gameserver.datatables.NobleSkillTable;
import ct25.xtreme.gameserver.datatables.NpcBufferTable;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.datatables.NpcWalkerRoutesTable;
import ct25.xtreme.gameserver.datatables.OfflineTradersTable;
import ct25.xtreme.gameserver.datatables.PetDataTable;
import ct25.xtreme.gameserver.datatables.SiegeScheduleData;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.datatables.SkillTreesData;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.datatables.StaticObjects;
import ct25.xtreme.gameserver.datatables.SummonItemsData;
import ct25.xtreme.gameserver.datatables.SummonSkillsTable;
import ct25.xtreme.gameserver.datatables.TeleportLocationTable;
import ct25.xtreme.gameserver.datatables.UIData;
import ct25.xtreme.gameserver.geoeditorcon.GeoEditorListener;
import ct25.xtreme.gameserver.handler.AdminCommandHandler;
import ct25.xtreme.gameserver.handler.ChatHandler;
import ct25.xtreme.gameserver.handler.ItemHandler;
import ct25.xtreme.gameserver.handler.SkillHandler;
import ct25.xtreme.gameserver.handler.UserCommandHandler;
import ct25.xtreme.gameserver.handler.VoicedCommandHandler;
import ct25.xtreme.gameserver.idfactory.IdFactory;
import ct25.xtreme.gameserver.instancemanager.AirShipManager;
import ct25.xtreme.gameserver.instancemanager.AntiFeedManager;
import ct25.xtreme.gameserver.instancemanager.AuctionManager;
import ct25.xtreme.gameserver.instancemanager.BoatManager;
import ct25.xtreme.gameserver.instancemanager.BotManager;
import ct25.xtreme.gameserver.instancemanager.CastleManager;
import ct25.xtreme.gameserver.instancemanager.CastleManorManager;
import ct25.xtreme.gameserver.instancemanager.ClanHallManager;
import ct25.xtreme.gameserver.instancemanager.CoupleManager;
import ct25.xtreme.gameserver.instancemanager.CursedWeaponsManager;
import ct25.xtreme.gameserver.instancemanager.DayNightSpawnManager;
import ct25.xtreme.gameserver.instancemanager.DimensionalRiftManager;
import ct25.xtreme.gameserver.instancemanager.FortManager;
import ct25.xtreme.gameserver.instancemanager.FortSiegeManager;
import ct25.xtreme.gameserver.instancemanager.FourSepulchersManager;
import ct25.xtreme.gameserver.instancemanager.GlobalVariablesManager;
import ct25.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.ItemAuctionManager;
import ct25.xtreme.gameserver.instancemanager.ItemsOnGroundManager;
import ct25.xtreme.gameserver.instancemanager.MailManager;
import ct25.xtreme.gameserver.instancemanager.MercTicketManager;
import ct25.xtreme.gameserver.instancemanager.PetitionManager;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.instancemanager.RaidBossPointsManager;
import ct25.xtreme.gameserver.instancemanager.RaidBossSpawnManager;
import ct25.xtreme.gameserver.instancemanager.SiegeManager;
import ct25.xtreme.gameserver.instancemanager.TerritoryWarManager;
import ct25.xtreme.gameserver.instancemanager.TransformationManager;
import ct25.xtreme.gameserver.instancemanager.UndergroundColiseumManager;
import ct25.xtreme.gameserver.instancemanager.WalkingManager;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.AutoChatHandler;
import ct25.xtreme.gameserver.model.AutoSpawnHandler;
import ct25.xtreme.gameserver.model.L2Manor;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.PartyMatchRoomList;
import ct25.xtreme.gameserver.model.PartyMatchWaitingList;
import ct25.xtreme.gameserver.model.entity.Hero;
import ct25.xtreme.gameserver.model.entity.TvTManager;
import ct25.xtreme.gameserver.model.entity.event.DMManager;
import ct25.xtreme.gameserver.model.entity.event.Hitman;
import ct25.xtreme.gameserver.model.entity.event.LMManager;
import ct25.xtreme.gameserver.model.olympiad.Olympiad;
import ct25.xtreme.gameserver.network.L2GameClient;
import ct25.xtreme.gameserver.network.L2GamePacketHandler;
import ct25.xtreme.gameserver.network.communityserver.CommunityServerThread;
import ct25.xtreme.gameserver.pathfinding.PathFinding;
import ct25.xtreme.gameserver.script.faenor.FaenorScriptEngine;
import ct25.xtreme.gameserver.scripting.CompiledScriptCache;
import ct25.xtreme.gameserver.scripting.L2ScriptEngineManager;
import ct25.xtreme.gameserver.taskmanager.AutoAnnounceTaskManager;
import ct25.xtreme.gameserver.taskmanager.KnownListUpdateTaskManager;
import ct25.xtreme.gameserver.taskmanager.TaskManager;
import ct25.xtreme.status.Status;
import ct25.xtreme.util.DeadLockDetector;
import ct25.xtreme.util.IPv4Filter;

/**
 * This class ...
 * 
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB() + "MB");
		
		if (Config.SERVER_VERSION != null)
		{
			_log.info("Freya Server Version:    " + Config.SERVER_VERSION);
		}
		if (Config.DATAPACK_VERSION != null)
		{
			_log.info("Freya Datapack Version:  " + Config.DATAPACK_VERSION);
		}
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("World");
		// start game time control early
		GameTimeController.getInstance();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionTable.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Skills");
		EnchantGroupsTable.getInstance();
		SkillTable.getInstance();
		SkillTreesData.getInstance();
		SummonSkillsTable.getInstance();
		HeroSkillTable.getInstance();
		NobleSkillTable.getInstance();
		GMSkillTable.getInstance();
		
		printSection("Items");
		ItemTable.getInstance();
		SummonItemsData.getInstance();
		EnchantHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		TradeController.getInstance();
		MultiSell.getInstance();
		RecipeController.getInstance();
		ArmorSetsTable.getInstance();
		FishTable.getInstance();
		
		printSection("Characters");
		CharTemplateTable.getInstance();
		CharNameTable.getInstance();
		LevelUpData.getInstance();
		AccessLevels.getInstance();
		AdminCommandAccessRights.getInstance();
		GmListTable.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		if(Config.ENABLE_BOTREPORT)
			BotManager.getInstance();
		
		printSection("Clans");
		ClanTable.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
			PathFinding.getInstance();
		
		printSection("NPCs");
		HerbDropTable.getInstance();
		NpcTable.getInstance();
		NpcWalkerRoutesTable.getInstance();
		WalkingManager.getInstance();
		ZoneManager.getInstance();
		DoorTable.getInstance();
		StaticObjects.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		SpawnTable.getInstance();
		HellboundManager.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("Siege");
		SiegeScheduleData.getInstance();
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		L2Manor.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestCache.getInstance();
		TeleportLocationTable.getInstance();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		HennaTable.getInstance();
		HennaTreeTable.getInstance();
		HelperBuffTable.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		
		printSection("Scheme Buffer");
		ModsBufferSkillTable.getInstance();
		ModsBufferSchemeTable.getInstance();
		
		printSection("Scripts");
		QuestManager.getInstance();
		TransformationManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		try
		{
			_log.info("Loading Server Scripts");
			File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
			if(!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
		}
		catch (IOException ioe)
		{
			_log.severe("Failed loading scripts.cfg, no script going to be loaded");
		}
		try
		{
			CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
			if (compiledScriptCache == null)
			{
				_log.info("Compiled Scripts Cache is disabled.");
			}
			else
			{
				compiledScriptCache.purge();
				
				if (compiledScriptCache.isModified())
				{
					compiledScriptCache.save();
					_log.info("Compiled Scripts Cache was saved.");
				}
				else
				{
					_log.info("Compiled Scripts Cache is up-to-date.");
				}
			}
			
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "Failed to store Compiled Scripts Cache.", e);
		}
		QuestManager.getInstance().report();
		TransformationManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
			ItemsOnGroundManager.getInstance();
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
			ItemsAutoDestroy.getInstance();
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		AutoChatHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoChatHandler: Loaded " + AutoChatHandler.getInstance().size() + " handlers in total.");
		_log.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		
		AdminCommandHandler.getInstance();
		ChatHandler.getInstance();
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		
		if (Config.L2JMOD_ALLOW_WEDDING)
			CoupleManager.getInstance();
		
		TaskManager.getInstance();

		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		MerchantPriceConfigTable.getInstance().updateReferences();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		
		if (Config.ALLOW_MAIL)
			MailManager.getInstance();
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
			GeoEditorListener.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		TvTManager.getInstance();
		DMManager.getInstance();
		LMManager.getInstance();
		Hitman.start();
		UndergroundColiseumManager.getInstance();
		KnownListUpdateTaskManager.getInstance();
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
			OfflineTradersTable.restoreOfflineTraders();
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
			_deadDetectThread = null;
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the
		// allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info("GameServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		CommunityServerThread.initialize();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<L2GameClient>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, "WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server Loaded in " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds");
		
		AutoAnnounceTaskManager.getInstance();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is = new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
		// Initialize config
		Config.load();
		printSection("Database");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
			s = "-" + s;
		_log.info(s);
	}
}
