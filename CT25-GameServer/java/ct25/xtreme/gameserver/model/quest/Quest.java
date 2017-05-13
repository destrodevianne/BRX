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
package ct25.xtreme.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.cache.HtmCache;
import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.datatables.ItemTable;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.idfactory.IdFactory;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.L2DropData;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.L2Trap;
import ct25.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2TrapInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.holders.ItemHolder;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.model.interfaces.IIdentifiable;
import ct25.xtreme.gameserver.model.interfaces.IPositionable;
import ct25.xtreme.gameserver.model.interfaces.IProcedure;
import ct25.xtreme.gameserver.model.itemcontainer.Inventory;
import ct25.xtreme.gameserver.model.itemcontainer.PcInventory;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct25.xtreme.gameserver.network.serverpackets.ExShowScreenMessage;
import ct25.xtreme.gameserver.network.serverpackets.InventoryUpdate;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.NpcQuestHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.PlaySound;
import ct25.xtreme.gameserver.network.serverpackets.StatusUpdate;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.scripting.ManagedScript;
import ct25.xtreme.gameserver.scripting.ScriptManager;
import ct25.xtreme.gameserver.skills.Stats;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct25.xtreme.gameserver.util.MinionList;
import ct25.xtreme.util.Rnd;
import ct25.xtreme.util.Util;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author Luis Arias
 *
 */
public class Quest extends ManagedScript implements IIdentifiable
{
	public static final Logger _log = Logger.getLogger(Quest.class.getName());
	
	/** HashMap containing events from String value of the event */
	private static Map<String, Quest> _allEventsS = new FastMap<String, Quest>();
	/** HashMap containing lists of timers from the name of the timer */
	private Map<String, FastList<QuestTimer>> _allEventTimers = new FastMap<String, FastList<QuestTimer>>();
	private final Set<Integer> _questInvolvedNpcs = new HashSet<>();
	private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
	private final int _questId;
	private final String _name;
	private final String _descr;
	private final byte _initialState = State.CREATED;
	protected boolean _onEnterWorld = false;
	private boolean _isCustom = false;
	// NOTE: questItemIds will be overridden by child classes.  Ideally, it should be
	// protected instead of public.  However, quest scripts written in Jython will
	// have trouble with protected, as Jython only knows private and public...
	// In fact, protected will typically be considered private thus breaking the scripts.
	// Leave this as public as a workaround.
	public int[] questItemIds = null;
	
	private static final String DEFAULT_NO_QUEST_MSG = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	private static final String DEFAULT_ALREADY_COMPLETED_MSG = "<html><body>This quest has already been completed.</body></html>";
	
	private static final int RESET_HOUR = 6;
	private static final int RESET_MINUTES = 30;
	
	/**
	 * @return the reset hour for a daily quest, could be overridden on a script.
	 */
	public int getResetHour()
	{
		return RESET_HOUR;
	}
	
	/**
	 * @return the reset minutes for a daily quest, could be overridden on a script.
	 */
	public int getResetMinutes()
	{
		return RESET_MINUTES;
	}
	
	/**
	 * This enum contains known sound effects used by quests.<br>
	 * The idea is to have only a single object of each quest sound instead of constructing a new one every time a script calls the playSound method.<br>
	 * This is pretty much just a memory and CPU cycle optimization; avoids constructing/deconstructing objects all the time if they're all the same.<br>
	 * For datapack scripts written in Java and extending the Quest class, this does not need an extra import.
	 * @author jurchiks
	 */
	public static enum QuestSound
	{
		ITEMSOUND_QUEST_ACCEPT(new PlaySound("ItemSound.quest_accept")), ITEMSOUND_QUEST_MIDDLE(new PlaySound("ItemSound.quest_middle")), ITEMSOUND_QUEST_FINISH(new PlaySound("ItemSound.quest_finish")), ITEMSOUND_QUEST_ITEMGET(new PlaySound("ItemSound.quest_itemget")),
		// Newbie Guide tutorial (incl. some quests), Mutated Kaneus quests, Quest 192
		ITEMSOUND_QUEST_TUTORIAL(new PlaySound("ItemSound.quest_tutorial")),
		// Quests 107, 363, 364
		ITEMSOUND_QUEST_GIVEUP(new PlaySound("ItemSound.quest_giveup")),
		// Quests 212, 217, 224, 226, 416
		ITEMSOUND_QUEST_BEFORE_BATTLE(new PlaySound("ItemSound.quest_before_battle")),
		// Quests 211, 258, 266, 330
		ITEMSOUND_QUEST_JACKPOT(new PlaySound("ItemSound.quest_jackpot")),
		// Quests 508, 509 and 510
		ITEMSOUND_QUEST_FANFARE_1(new PlaySound("ItemSound.quest_fanfare_1")),
		// Played only after class transfer via Test Server Helpers (Id 31756 and 31757)
		ITEMSOUND_QUEST_FANFARE_2(new PlaySound("ItemSound.quest_fanfare_2")),
		// Quest 336
		ITEMSOUND_QUEST_FANFARE_MIDDLE(new PlaySound("ItemSound.quest_fanfare_middle")),
		// Quest 114
		ITEMSOUND_ARMOR_WOOD(new PlaySound("ItemSound.armor_wood_3")),
		// Quest 21
		ITEMSOUND_ARMOR_CLOTH(new PlaySound("ItemSound.item_drop_equip_armor_cloth")), AMDSOUND_ED_CHIMES(new PlaySound("AmdSound.ed_chimes_05")), HORROR_01(new PlaySound("horror_01")), // played when spawned monster sees player
		// Quest 22
		AMBSOUND_HORROR_01(new PlaySound("AmbSound.dd_horror_01")), AMBSOUND_HORROR_03(new PlaySound("AmbSound.d_horror_03")), AMBSOUND_HORROR_15(new PlaySound("AmbSound.d_horror_15")),
		// Quest 23
		ITEMSOUND_ARMOR_LEATHER(new PlaySound("ItemSound.itemdrop_armor_leather")), ITEMSOUND_WEAPON_SPEAR(new PlaySound("ItemSound.itemdrop_weapon_spear")), AMBSOUND_MT_CREAK(new PlaySound("AmbSound.mt_creak01")), AMBSOUND_EG_DRON(new PlaySound("AmbSound.eg_dron_02")), SKILLSOUND_HORROR_02(new PlaySound("SkillSound5.horror_02")), CHRSOUND_MHFIGHTER_CRY(new PlaySound("ChrSound.MHFighter_cry")),
		// Quest 24
		AMDSOUND_WIND_LOOT(new PlaySound("AmdSound.d_wind_loot_02")), INTERFACESOUND_CHARSTAT_OPEN(new PlaySound("InterfaceSound.charstat_open_01")),
		// Quest 25
		AMDSOUND_HORROR_02(new PlaySound("AmdSound.dd_horror_02")), CHRSOUND_FDELF_CRY(new PlaySound("ChrSound.FDElf_Cry")),
		// Quest 115
		AMBSOUND_WINGFLAP(new PlaySound("AmbSound.t_wingflap_04")), AMBSOUND_THUNDER(new PlaySound("AmbSound.thunder_02")),
		// Quest 120
		AMBSOUND_DRONE(new PlaySound("AmbSound.ed_drone_02")), AMBSOUND_CRYSTAL_LOOP(new PlaySound("AmbSound.cd_crystal_loop")), AMBSOUND_PERCUSSION_01(new PlaySound("AmbSound.dt_percussion_01")), AMBSOUND_PERCUSSION_02(new PlaySound("AmbSound.ac_percussion_02")),
		// Quest 648 and treasure chests
		ITEMSOUND_BROKEN_KEY(new PlaySound("ItemSound2.broken_key")),
		// Quest 184
		ITEMSOUND_SIREN(new PlaySound("ItemSound3.sys_siren")),
		// Quest 648
		ITEMSOUND_ENCHANT_SUCCESS(new PlaySound("ItemSound3.sys_enchant_success")), ITEMSOUND_ENCHANT_FAILED(new PlaySound("ItemSound3.sys_enchant_failed")),
		// Best farm mobs
		ITEMSOUND_SOW_SUCCESS(new PlaySound("ItemSound3.sys_sow_success")),
		// Quest 25
		SKILLSOUND_HORROR_1(new PlaySound("SkillSound5.horror_01")),
		// Quests 21 and 23
		SKILLSOUND_HORROR_2(new PlaySound("SkillSound5.horror_02")),
		// Quest 22
		SKILLSOUND_ANTARAS_FEAR(new PlaySound("SkillSound3.antaras_fear")),
		// Quest 505
		SKILLSOUND_JEWEL_CELEBRATE(new PlaySound("SkillSound2.jewel.celebrate")),
		// Quest 373
		SKILLSOUND_LIQUID_MIX(new PlaySound("SkillSound5.liquid_mix_01")), SKILLSOUND_LIQUID_SUCCESS(new PlaySound("SkillSound5.liquid_success_01")), SKILLSOUND_LIQUID_FAIL(new PlaySound("SkillSound5.liquid_fail_01")),
		// Quest 111
		ETCSOUND_ELROKI_SONG_FULL(new PlaySound("EtcSound.elcroki_song_full")), ETCSOUND_ELROKI_SONG_1ST(new PlaySound("EtcSound.elcroki_song_1st")), ETCSOUND_ELROKI_SONG_2ND(new PlaySound("EtcSound.elcroki_song_2nd")), ETCSOUND_ELROKI_SONG_3RD(new PlaySound("EtcSound.elcroki_song_3rd")),
		// Long duration AI sounds
		BS01_A(new PlaySound("BS01_A")), BS02_A(new PlaySound("BS02_A")), BS03_A(new PlaySound("BS03_A")), BS04_A(new PlaySound("BS04_A")), BS06_A(new PlaySound("BS06_A")), BS07_A(new PlaySound("BS07_A")), BS08_A(new PlaySound("BS08_A")), BS01_D(new PlaySound("BS01_D")), BS02_D(new PlaySound("BS02_D")), BS05_D(new PlaySound("BS05_D")), BS07_D(new PlaySound("BS07_D"));
		
		private final PlaySound _playSound;
		
		private static Map<String, PlaySound> soundPackets = new HashMap<>();
		
		private QuestSound(PlaySound playSound)
		{
			_playSound = playSound;
		}
		
		/**
		 * Get a {@link PlaySound} packet by its name.
		 * @param soundName : the name of the sound to look for
		 * @return the {@link PlaySound} packet with the specified sound or {@code null} if one was not found
		 */
		public static PlaySound getSound(String soundName)
		{
			if (soundPackets.containsKey(soundName))
			{
				return soundPackets.get(soundName);
			}
			
			for (QuestSound qs : QuestSound.values())
			{
				if (qs._playSound.getSoundName().equals(soundName))
				{
					soundPackets.put(soundName, qs._playSound); // cache in map to avoid looping repeatedly
					return qs._playSound;
				}
			}
			
			_log.info("Missing QuestSound enum for sound: " + soundName);
			soundPackets.put(soundName, new PlaySound(soundName));
			return soundPackets.get(soundName);
		}
		
		/**
		 * @return the name of the sound of this QuestSound object
		 */
		public String getSoundName()
		{
			return _playSound.getSoundName();
		}
		
		/**
		 * @return the {@link PlaySound} packet of this QuestSound object
		 */
		public PlaySound getPacket()
		{
			return _playSound;
		}
	}
	
	/**
	 * Return collection view of the values contains in the allEventS
	 * @return Collection<Quest>
	 */
	public static Collection<Quest> findAllEvents()
	{
		return _allEventsS.values();
	}
	
	/**
	 * (Constructor)Add values to class variables and put the quest in HashMaps.
	 * @param questId : int pointing out the ID of the quest
	 * @param name : String corresponding to the name of the quest
	 * @param descr : String for the description of the quest
	 */
	public Quest(int questId, String name, String descr)
	{
		_questId = questId;
		_name = name;
		_descr = descr;
		
		if (questId != 0)
		{
			QuestManager.getInstance().addQuest(Quest.this);
		}
		else
		{
			_allEventsS.put(name, this);
		}
		init_LoadGlobalData();
	}
	
	/**
	 * The function init_LoadGlobalData is, by default, called by the constructor of all quests.
	 * Children of this class can implement this function in order to define what variables
	 * to load and what structures to save them in.  By default, nothing is loaded.
	 */
	protected void init_LoadGlobalData()
	{
	
	}
	
	/**
	 * The function saveGlobalData is, by default, called at shutdown, for all quests, by the QuestManager.
	 * Children of this class can implement this function in order to convert their structures
	 * into <var, value> tuples and make calls to save them to the database, if needed.
	 * By default, nothing is saved.
	 */
	public void saveGlobalData()
	{
	
	}
	
	public static enum TrapAction
	{
		TRAP_TRIGGERED, TRAP_DETECTED, TRAP_DISARMED
	}
	
	public static enum QuestEventType
	{
		ON_FIRST_TALK(false), // control the first dialog shown by NPCs when they are clicked (some quests must override the default npc action)
		QUEST_START(true), // onTalk action from start npcs
		ON_TALK(true), // onTalk action from npcs participating in a quest
		ON_ATTACK(true), // onAttack action triggered when a mob gets attacked by someone
		ON_KILL(true), // onKill action triggered when a mob gets killed.
		ON_SPAWN(true), // onSpawn action triggered when an NPC is spawned or respawned.
		ON_SKILL_SEE(true), // NPC or Mob saw a person casting a skill (regardless what the target is).
		ON_FACTION_CALL(true), // NPC or Mob saw a person casting a skill (regardless what the target is).
		ON_AGGRO_RANGE_ENTER(true), // a person came within the Npc/Mob's range
		ON_SEE_CREATURE(true), // onSeeCreature action, triggered when NPC's known list include the character
		ON_EVENT_RECEIVED(true), // onEventReceived action, triggered when NPC receiving an event, sent by other NPC
		ON_MOVE_FINISHED(true), // onMoveFinished action, triggered when NPC stops after moving
		ON_NODE_ARRIVED(true), // onNodeArrived action, triggered when NPC, controlled by Walking Manager, arrives to next node
		ON_SPELL_FINISHED(true), // on spell finished action when npc finish casting skill
		ON_SKILL_LEARN(false), // control the AcquireSkill dialog from quest script
		ON_ENTER_ZONE(true), // on zone enter
		ON_EXIT_ZONE(true), // on zone exit
		ON_TRAP_ACTION(true); // on zone exit
		
		// control whether this event type is allowed for the same npc template in multiple quests
		// or if the npc must be registered in at most one quest for the specified event
		private boolean _allowMultipleRegistration;
		
		QuestEventType(boolean allowMultipleRegistration)
		{
			_allowMultipleRegistration = allowMultipleRegistration;
		}
		
		public boolean isMultipleRegistrationAllowed()
		{
			return _allowMultipleRegistration;
		}
		
	}
	
	/**
	 * Return ID of the quest
	 * @return int
	 */
	@Override
	public int getId()
	{
		return _questId;
	}
	
	/**
	 * Add a new QuestState to the database and return it.
	 * @param player
	 * @return QuestState : QuestState created
	 */
	public QuestState newQuestState(L2PcInstance player)
	{
		QuestState qs = new QuestState(this, player, getInitialState());
		return qs;
	}
	
	/**
	 * Get the specified player's {@link QuestState} object for this quest.<br>
	 * If the player does not have it and initIfNode is {@code true},<br>
	 * create a new QuestState object and return it, otherwise return {@code null}.
	 * @param player the player whose QuestState to get
	 * @param initIfNone if true and the player does not have a QuestState for this quest,<br>
	 *            create a new QuestState
	 * @return the QuestState object for this quest or null if it doesn't exist
	 */
	public QuestState getQuestState(L2PcInstance player, boolean initIfNone)
	{
		final QuestState qs = player.getQuestState(_name);
		if ((qs != null) || !initIfNone)
		{
			return qs;
		}
		return newQuestState(player);
	}
	
	/**
	 * Return initial state of the quest
	 * @return State
	 */
	public byte getInitialState()
	{
		return _initialState;
	}
	
	/**
	 * Return name of the quest
	 * @return String
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Return description of the quest
	 * @return String
	 */
	public String getDescr()
	{
		return _descr;
	}
	
	/**
	 * Add a timer to the quest, if it doesn't exist already
	 * @param name: name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time: time in ms for when to fire the timer
	 * @param npc:  npc associated with this timer (can be null)
	 * @param player: player associated with this timer (can be null)
	 */
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player)
	{
		startQuestTimer(name, time, npc, player, false);
	}
	
	/**
	 * Add a timer to the quest, if it doesn't exist already.  If the timer is repeatable,
	 * it will auto-fire automatically, at a fixed rate, until explicitly canceled.
	 * @param name: name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time: time in ms for when to fire the timer
	 * @param npc:  npc associated with this timer (can be null)
	 * @param player: player associated with this timer (can be null)
	 * @param repeatable: indicates if the timer is repeatable or one-time.
	 */
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		// Add quest timer if timer doesn't already exist
		FastList<QuestTimer> timers = getQuestTimers(name);
		// no timer exists with the same name, at all
		if (timers == null)
		{
			timers = new FastList<QuestTimer>();
			timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			_allEventTimers.put(name, timers);
		}
		// a timer with this name exists, but may not be for the same set of npc and player
		else
		{
			// if there exists a timer with this name, allow the timer only if the [npc, player] set is unique
			// nulls act as wildcards
			if (getQuestTimer(name, npc, player) == null)
			{
				try
				{
					_rwLock.writeLock().lock();
					timers.add(new QuestTimer(this, name, time, npc, player, repeating));
				}
				finally
				{
					_rwLock.writeLock().unlock();
				}
			}
		}
	}
	
	public QuestTimer getQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		FastList<QuestTimer> qt = getQuestTimers(name);
		
		if (qt == null || qt.isEmpty())
			return null;
		try
		{
			_rwLock.readLock().lock();
			for (QuestTimer timer : qt)
			{
				if (timer != null)
				{
					if (timer.isMatch(this, name, npc, player))
						return timer;
				}
			}
			
		}
		finally
		{
			_rwLock.readLock().unlock();
		}
		return null;
	}
	
	private FastList<QuestTimer> getQuestTimers(String name)
	{
		return _allEventTimers.get(name);
	}
	
	public void cancelQuestTimers(String name)
	{
		FastList<QuestTimer> timers = getQuestTimers(name);
		if (timers == null)
			return;
		try
		{
			_rwLock.writeLock().lock();
			for (QuestTimer timer : timers)
			{
				if (timer != null)
				{
					timer.cancel();
				}
			}
		}
		finally
		{
			_rwLock.writeLock().unlock();
		}
	}
	
	public void cancelQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		QuestTimer timer = getQuestTimer(name, npc, player);
		if (timer != null)
			timer.cancel();
	}
	
	public void removeQuestTimer(QuestTimer timer)
	{
		if (timer == null)
			return;
		FastList<QuestTimer> timers = getQuestTimers(timer.getName());
		if (timers == null)
			return;
		try
		{
			_rwLock.writeLock().lock();
			timers.remove(timer);
		}
		finally
		{
			_rwLock.writeLock().unlock();
		}
	}
	
	// these are methods to call from java
	
	public final boolean notifyItemTalk(L2ItemInstance item, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onItemTalk(item, player);
			if (res != null)
			{
				if (res.equalsIgnoreCase("true"))
				{
					return true;
				}
				else if (res.equalsIgnoreCase("false"))
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	/**
	 * @param item
	 * @param player
	 * @return
	 */
	public String onItemTalk(L2ItemInstance item, L2PcInstance player)
	{
		return null;
	}
	
	/**
	 * @param item
	 * @param player
	 * @param event
	 * @return
	 */
	public final boolean notifyItemEvent(L2ItemInstance item, L2PcInstance player, String event)
	{
		String res = null;
		try
		{
			res = onItemEvent(item, player, event);
			if (res != null)
			{
				if (res.equalsIgnoreCase("true"))
				{
					return true;
				}
				else if (res.equalsIgnoreCase("false"))
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isPet, skill);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public final boolean notifyDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		return showResult(qs.getPlayer(), res);
	}
	
	public final boolean notifySpellFinished(L2Npc instance, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(instance, player, skill);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	/**
	 * Notify quest script when something happens with a trap
	 * @param trap: the trap instance which triggers the notification
	 * @param trigger: the character which makes effect on the trap
	 * @param action: 0: trap casting its skill. 1: trigger detects the trap. 2: trigger removes the trap
	 */
	public final boolean notifyTrapAction(L2Trap trap, L2Character trigger, Quest.TrapAction action)
	{
		String res = null;
		try
		{
			res = onTrapAction(trap, trigger, action);
		}
		catch (Exception e)
		{
			if (trigger.getActingPlayer() != null)
				return showError(trigger.getActingPlayer(), e);
			_log.log(Level.WARNING, "Exception on onTrapAction() in notifyTrapAction(): " + e.getMessage(), e);
			return true;
		}
		if (trigger.getActingPlayer() != null)
			return showResult(trigger.getActingPlayer(), res);
		return false;
	}
	
	public final boolean notifySpawn(L2Npc npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onSpawn() in notifySpawn(): " + e.getMessage(), e);
			return true;
		}
		return false;
	}
	
	public final boolean notifyEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyEnterWorld(L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onEnterWorld(player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		String res = null;
		try
		{
			res = onKill(npc, killer, isPet);
		}
		catch (Exception e)
		{
			return showError(killer, e);
		}
		return showResult(killer, res);
	}
	
	public final boolean notifyTalk(L2Npc npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs.getPlayer());
		}
		catch (Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs.getPlayer(), res);
	}
	
	// override the default NPC dialogs when a quest defines this for the given NPC
	public final boolean notifyFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		// if the quest returns text to display, display it.
		if (res != null && res.length() > 0)
			return showResult(player, res);
		// else tell the player that
		else
			player.sendPacket(ActionFailed.STATIC_PACKET);
		// note: if the default html for this npc needs to be shown, onFirstTalk should
		// call npc.showChatWindow(player) and then return null.
		return true;
	}
	
	public final boolean notifyAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAcquireSkillList(npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkillInfo(npc, player, skill);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkill(npc, player, skill);
			if (res == "true")
				return true;
			else if (res == "false")
				return false;
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public class TmpOnSkillSee implements Runnable
	{
		private L2Npc _npc;
		private L2PcInstance _caster;
		private L2Skill _skill;
		private L2Object[] _targets;
		private boolean _isPet;
		
		public TmpOnSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
		{
			_npc = npc;
			_caster = caster;
			_skill = skill;
			_targets = targets;
			_isPet = isPet;
		}
		
		public void run()
		{
			String res = null;
			try
			{
				res = onSkillSee(_npc, _caster, _skill, _targets, _isPet);
			}
			catch (Exception e)
			{
				showError(_caster, e);
			}
			showResult(_caster, res);
			
		}
	}
	
	public final boolean notifySkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(new TmpOnSkillSee(npc, caster, skill, targets, isPet));
		return true;
	}
	
	public final boolean notifyFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isPet);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public class TmpOnAggroEnter implements Runnable
	{
		private L2Npc _npc;
		private L2PcInstance _pc;
		private boolean _isPet;
		
		public TmpOnAggroEnter(L2Npc npc, L2PcInstance pc, boolean isPet)
		{
			_npc = npc;
			_pc = pc;
			_isPet = isPet;
		}
		
		public void run()
		{
			String res = null;
			try
			{
				res = onAggroRangeEnter(_npc, _pc, _isPet);
			}
			catch (Exception e)
			{
				showError(_pc, e);
			}
			showResult(_pc, res);
			
		}
	}
	
	public final boolean notifyAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(new TmpOnAggroEnter(npc, player, isPet));
		return true;
	}
	
	public class TmpOnSeeCreature implements Runnable
	{
		private final L2Npc _npc;
		private final L2Character _creature;
		private final boolean _isPet;
		
		public TmpOnSeeCreature(L2Npc npc, L2Character creature, boolean isPet)
		{
			_npc = npc;
			_creature = creature;
			_isPet = isPet;
		}
		
		public void run()
		{
			L2PcInstance player = null;
			if (_isPet || _creature.isPlayer())
			{
				player = _creature.getActingPlayer();
			}
			String res = null;
			try
			{
				res = onSeeCreature(_npc, _creature, _isPet);
			}
			catch (Exception e)
			{
				if (player != null)
				{
					showError(player, e);
				}
			}
			if (player != null)
			{
				showResult(player, res);
			}
		}
	}
	
	public final boolean notifySeeCreature(L2Npc npc, L2Character creature, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(new TmpOnSeeCreature(npc, creature, isPet));
		return true;
	}
	
	/**
	 * @param eventName - name of event
	 * @param sender - NPC, who sent event
	 * @param receiver - NPC, who received event
	 * @param reference - L2Object to pass, if needed
	 */
	public final void notifyEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		try
		{
			onEventReceived(eventName, sender, receiver, reference);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onEventReceived() in notifyEventReceived(): " + e.getMessage(), e);
		}
	}
	
	public final boolean notifyEnterZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = this.onEnterZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
				return showError(player, e);
		}
		if (player != null)
			return showResult(player, res);
		return true;
	}
	
	public final boolean notifyExitZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = this.onExitZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
				return showError(player, e);
		}
		if (player != null)
			return showResult(player, res);
		return true;
	}
	
	public final void notifyMoveFinished(L2Npc npc)
	{
		try
		{
			onMoveFinished(npc);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onMoveFinished() in notifyMoveFinished(): " + e.getMessage(), e);
		}
	}
	
	public final void notifyNodeArrived(L2Npc npc)
	{
		try
		{
			onNodeArrived(npc);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on onNodeArrived() in notifyNodeArrived(): " + e.getMessage(), e);
		}
	}
	
	// these are methods that java calls to invoke scripts
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		return null;
	}
	
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		return onAttack(npc, attacker, damage, isPet);
	}
	
	public String onDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		if (killer instanceof L2Npc)
			return onAdvEvent("", (L2Npc) killer, qs.getPlayer());
		else
			return onAdvEvent("", null, qs.getPlayer());
	}

	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (player != null)
		{
			final QuestState qs = player.getQuestState(getName());
			if (qs != null)
			{
				return onEvent(event, qs);
			}
		}
		return null;
	}
	
	/**
	 * This function is called in place of {@link #onAdvEvent(String, L2Npc, L2PcInstance)} if the former is not implemented.<br>
	 * If a script contains both {@link #onAdvEvent(String, L2Npc, L2PcInstance)} and this implementation, then this method will never be called unless the script's {@link #onAdvEvent(String, L2Npc, L2PcInstance)} explicitly calls this method.
	 * @param event this parameter contains a string identifier for the event.<br>
	 *            Generally, this string is passed directly via the link.<br>
	 *            For example:<br>
	 *            <code>
	 *            &lt;a action="bypass -h Quest 626_ADarkTwilight 31517-01.htm"&gt;hello&lt;/a&gt;
	 *            </code><br>
	 *            The above link sets the event variable to "31517-01.htm" for the quest 626_ADarkTwilight.<br>
	 *            In the case of timers, this will be the name of the timer.<br>
	 *            This parameter serves as a sort of identifier.
	 * @param qs this parameter contains a reference to the quest state of the player who used the link or started the timer.
	 * @return the text returned by the event (may be {@code null}, a filename or just text)
	 */
	public String onEvent(String event, QuestState qs)
	{
		return null;
	}
	
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		return null;
	}
	
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return null;
	}
	
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public String onItemEvent(L2ItemInstance item, L2PcInstance player, String event)
	{
		return null;
	}
	
	public String onAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public String onAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public String onAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		return null;
	}
	
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public String onTrapAction(L2Trap trap, L2Character trigger, TrapAction action)
	{
		return null;
	}
	
	public String onSpawn(L2Npc npc)
	{
		return null;
	}
	
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		return null;
	}
	
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return null;
	}
	
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isPet)
	{
		return null;
	}
	
	public String onEnterWorld(L2PcInstance player)
	{
		return null;
	}
	
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	public String onEventReceived(String eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		return null;
	}
	
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	public String onMoveFinished(L2Npc npc)
	{
		return null;
	}
	
	public String onNodeArrived(L2Npc npc)
	{
		return null;
	}
	
	/**
	 * Show message error to player who has an access level greater than 0
	 * @param player : L2PcInstance
	 * @param t : Throwable
	 * @return boolean
	 */
	public boolean showError(L2PcInstance player, Throwable t)
	{
		_log.log(Level.WARNING, this.getScriptFile().getAbsolutePath(), t);
		if (t.getMessage() == null)
			t.printStackTrace();
		if (player != null && player.getAccessLevel().isGm())
		{
			String res = "<html><body><title>Script error</title>" + Util.getStackTrace(t) + "</body></html>";
			return showResult(player, res);
		}
		return false;
	}
	
	/**
	 * Show a message to player.<BR><BR>
	 * <U><I>Concept : </I></U><BR>
	 * 3 cases are managed according to the value of the parameter "res" :<BR>
	 * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
	 * <LI><U>"res" starts with "<html>" :</U> the message hold in "res" is shown in a dialog box</LI>
	 * <LI><U>otherwise :</U> the message held in "res" is shown in chat box</LI>
	 * @param qs : QuestState
	 * @param res : String pointing out the message to show at the player
	 * @return boolean
	 */
	public boolean showResult(L2PcInstance player, String res)
	{
		if (res == null || res.isEmpty() || player == null)
			return true;
			
		if (res.endsWith(".htm") || res.endsWith(".html"))
		{
			showHtmlFile(player, res);
		}
		else if (res.startsWith("<html>"))
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.sendMessage(res);
		}
		return false;
	}
	
	/**
	 * Add quests to the L2PCInstance of the player.<BR><BR>
	 * <U><I>Action : </U></I><BR>
	 * Add state of quests, drops and variables for quests in the HashMap _quest of L2PcInstance
	 * @param player : Player who is entering the world
	 */
	public final static void playerEnter(L2PcInstance player)
	{
		
		Connection con = null;
		try
		{
			// Get list of quests owned by the player from database
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId=? and name=?");
			PreparedStatement invalidQuestDataVar = con.prepareStatement("delete FROM character_quests WHERE charId=? and name=? and var=?");
			
			PreparedStatement statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE charId=? AND var=?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				
				// Get ID of the quest and ID of its state
				String questId = rs.getString("name");
				String statename = rs.getString("value");
				
				// Search quest associated with the ID
				Quest q = QuestManager.getInstance().getQuest(questId);
				if (q == null)
				{
					_log.finer("Unknown quest " + questId + " for player " + player.getName());
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestData.setInt(1, player.getObjectId());
						invalidQuestData.setString(2, questId);
						invalidQuestData.executeUpdate();
					}
					continue;
				}
				
				// Create a new QuestState for the player that will be added to the player's list of quests
				new QuestState(q, player, State.getStateId(statename));
			}
			rs.close();
			invalidQuestData.close();
			statement.close();
			
			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE charId=? AND var<>?");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rs = statement.executeQuery();
			while (rs.next())
			{
				String questId = rs.getString("name");
				String var = rs.getString("var");
				String value = rs.getString("value");
				// Get the QuestState saved in the loop before
				QuestState qs = player.getQuestState(questId);
				if (qs == null)
				{
					_log.finer("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestDataVar.setInt(1, player.getObjectId());
						invalidQuestDataVar.setString(2, questId);
						invalidQuestDataVar.setString(3, var);
						invalidQuestDataVar.executeUpdate();
					}
					continue;
				}
				// Add parameter to the quest
				qs.setInternal(var, value);
			}
			rs.close();
			invalidQuestDataVar.close();
			statement.close();
			
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not insert char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		// events
		for (String name : _allEventsS.keySet())
		{
			player.processQuestEvent(name, "enter");
		}
	}
	
	/**
	 * Insert (or Update) in the database variables that need to stay persistant for this quest after a reboot.
	 * This function is for storage of values that do not related to a specific player but are
	 * global for all characters.  For example, if we need to disable a quest-gatekeeper until
	 * a certain time (as is done with some grand-boss gatekeepers), we can save that time in the DB.
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public final void saveGlobalQuestVar(String var, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not insert global quest variable:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Read from the database a previously saved variable for this quest.
	 * Due to performance considerations, this function should best be used only when the quest is first loaded.
	 * Subclasses of this class can define structures into which these loaded values can be saved.
	 * However, on-demand usage of this function throughout the script is not prohibited, only not recommended.
	 * Values read from this function were entered by calls to "saveGlobalQuestVar"
	 * @param var : String designating the name of the variable for the quest
	 * @return String : String representing the loaded value for the passed var, or an empty string if the var was invalid
	 */
	public final String loadGlobalQuestVar(String var)
	{
		String result = "";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
			statement.setString(1, getName());
			statement.setString(2, var);
			ResultSet rs = statement.executeQuery();
			if (rs.first())
				result = rs.getString(1);
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not load global quest variable:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return result;
	}
	
	/**
	 * Permanently delete from the database a global quest variable that was previously saved for this quest.
	 * @param var : String designating the name of the variable for the quest
	 */
	public final void deleteGlobalQuestVar(String var)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete global quest variable:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Permanently delete from the database all global quest variables that was previously saved for this quest.
	 */
	public final void deleteAllGlobalQuestVars()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?");
			statement.setString(1, getName());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete global quest variables:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Insert in the database the quest for the player.
	 * @param qs : QuestState pointing out the state of the quest
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO character_quests (charId,name,var,value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.setString(5, value);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not insert char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Update the value of the variable "var" for the quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * The selection of the right record is made with :
	 * <LI>charId = qs.getPlayer().getObjectID()</LI>
	 * <LI>name = qs.getQuest().getName()</LI>
	 * <LI>var = var</LI>
	 * <BR><BR>
	 * The modification made is :
	 * <LI>value = parameter value</LI>
	 * @param qs : Quest State
	 * @param var : String designating the name of the variable for quest
	 * @param value : String designating the value of the variable for quest
	 */
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE charId=? AND name=? AND var = ?");
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not update char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 * @param qs : object QuestState pointing out the player's quest
	 * @param var : String designating the variable characterizing the quest
	 */
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Delete the player's quest from database.
	 * @param qs : QuestState pointing out the player's quest
	 */
	public static void deleteQuestInDb(QuestState qs)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete char quest:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Create a record in database for quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * Use fucntion createQuestVarInDb() with following parameters :<BR>
	 * <LI>QuestState : parameter sq that puts in fields of database :
	 * 	 <UL type="square">
	 *     <LI>charId : ID of the player</LI>
	 *     <LI>name : name of the quest</LI>
	 *   </UL>
	 * </LI>
	 * <LI>var : string "&lt;state&gt;" as the name of the variable for the quest</LI>
	 * <LI>val : string corresponding at the ID of the state (in fact, initial state)</LI>
	 * @param qs : QuestState
	 */
	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", State.getStateName(qs.getState()));
	}
	
	/**
	 * Update informations regarding quest in database.<BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Get ID state of the quest recorded in object qs</LI>
	 * <LI>Test if quest is completed. If true, add a star (*) before the ID state</LI>
	 * <LI>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</LI>
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(QuestState qs)
	{
		String val = State.getStateName(qs.getState());
		updateQuestVarInDb(qs, "<state>", val);
	}
	
	/**
	 * Return default html page "You are either not on a quest that involves this NPC.."
	 * @param player
	 * @return
	 */
	public static String getNoQuestMsg(L2PcInstance player)
	{
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/noquest.htm");
		if (result != null && result.length() > 0)
			return result;
			
		return DEFAULT_NO_QUEST_MSG;
	}
	
	/**
	 * Return default html page "This quest has already been completed."
	 * @param player
	 * @return
	 */
	public static String getAlreadyCompletedMsg(L2PcInstance player)
	{
		final String result = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/alreadycompleted.htm");
		if (result != null && result.length() > 0)
			return result;
			
		return DEFAULT_ALREADY_COMPLETED_MSG;
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.
	 * @param eventType type of event being registered
	 * @param npcIds NPC Ids to register
	 */
	public void addEventId(QuestEventType eventType, int... npcIds)
	{
		try
		{
			for (int npcId : npcIds)
			{
				final L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
				if (t != null)
				{
					t.addQuestEvent(eventType, this);
					_questInvolvedNpcs.add(npcId);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on addEventId(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.<BR><BR>
	 * @param npcId : id of the NPC to register
	 * @param eventType : type of event being registered
	 * @return L2NpcTemplate : Npc Template corresponding to the npcId, or null if the id is invalid
	 */
	public L2NpcTemplate addEventId(int npcId, QuestEventType eventType)
	{
		try
		{
			L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			if (t != null)
			{
				t.addQuestEvent(eventType, this);
			}
			return t;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on addEventId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	// TODO: Remove after all Jython scripts are replaced with Java versions.
	public void addStartNpc(int npcId)
	{
		addEventId(QuestEventType.QUEST_START, npcId);
	}
	
	public void addFirstTalkId(int npcId)
	{
		addEventId(QuestEventType.ON_FIRST_TALK, npcId);
	}
	
	public void addTalkId(int npcId)
	{
		addEventId(QuestEventType.ON_TALK, npcId);
	}
	
	public void addKillId(int killId)
	{
		addEventId(QuestEventType.ON_KILL, killId);
	}
	
	public void addAttackId(int npcId)
	{
		addEventId(QuestEventType.ON_ATTACK, npcId);
	}
	
	//TODO All Collection Methods Replaced
	public void addStartNpc(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.QUEST_START, npcId);
		}
	}
	
	public void addFirstTalkId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_FIRST_TALK, npcId);
		}
	}
	
	public void addAcquireSkillId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_SKILL_LEARN, npcId);
		}
	}
	
	public void addAttackId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_ATTACK, npcId);
		}
	}
	
	public void addKillId(Collection<Integer> killIds)
	{
		for (int killId : killIds)
		{
			addEventId(QuestEventType.ON_KILL, killId);
		}
	}
	
	public void addTalkId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_TALK, npcId);
		}
	}
	
	public void addSpawnId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_SPAWN, npcId);
		}
	}
	
	public void addSkillSeeId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_SKILL_SEE, npcId);
		}
	}
	
	public void addSpellFinishedId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_SPELL_FINISHED, npcId);
		}
	}
	
	public void addTrapActionId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_TRAP_ACTION, npcId);
		}
	}
	
	public void addFactionCallId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_FACTION_CALL, npcId);
		}
	}
	
	public void addAggroRangeEnterId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_AGGRO_RANGE_ENTER, npcId);
		}
	}
	
	public void addSeeCreatureId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_SEE_CREATURE, npcId);
		}
	}
	
	public void addEventReceivedId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_EVENT_RECEIVED, npcId);
		}
	}
	
	public void addMoveFinishedId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_MOVE_FINISHED, npcId);
		}
	}
	
	public void addNodeArrivedId(Collection<Integer> npcIds)
	{
		for (int npcId : npcIds)
		{
			addEventId(QuestEventType.ON_NODE_ARRIVED, npcId);
		}
	}
	
	//TODO New Methods Ids of the Npcs register
	/**
	 * Add the quest to the NPC's startQuest
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addStartNpc(int... npcIds)
	{
		addEventId(QuestEventType.QUEST_START, npcIds);
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog).
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addFirstTalkId(int... npcIds)
	{
		addEventId(QuestEventType.ON_FIRST_TALK, npcIds);
	}
	
	/**
	 * Add the NPC to the AcquireSkill dialog.
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addAcquireSkillId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_LEARN, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for attack events.
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addAttackId(int... npcIds)
	{
		addEventId(QuestEventType.ON_ATTACK, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for kill events.
	 * @param killIds
	 */
	public void addKillId(int... killIds)
	{
		addEventId(QuestEventType.ON_KILL, killIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addTalkId(int... npcIds)
	{
		addEventId(QuestEventType.ON_TALK, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for spawn events.
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addSpawnId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SPAWN, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for skill see events.
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addSkillSeeId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SKILL_SEE, npcIds);
	}
	
	/**
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addSpellFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SPELL_FINISHED, npcIds);
	}
	
	/**
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addTrapActionId(int... npcIds)
	{
		addEventId(QuestEventType.ON_TRAP_ACTION, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for faction call events.
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addFactionCallId(int... npcIds)
	{
		addEventId(QuestEventType.ON_FACTION_CALL, npcIds);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for character see events.
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addAggroRangeEnterId(int... npcIds)
	{
		addEventId(QuestEventType.ON_AGGRO_RANGE_ENTER, npcIds);
	}
	
	/**
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addSeeCreatureId(int... npcIds)
	{
		addEventId(QuestEventType.ON_SEE_CREATURE, npcIds);
	}
	
	/**
	 * Register onEventReceived trigger for NPC
	 * @param npcIds the IDs of the NPCs to register
	 */
	public void addEventReceivedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_EVENT_RECEIVED, npcIds);
	}
	
	/**
	 * Register onMoveFinished trigger for NPC
	 * @param npcIds
	 */
	public void addMoveFinishedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_MOVE_FINISHED, npcIds);
	}
	
	/**
	 * Register addNodeArrived trigger for NPC
	 * @param npcIds id of NPC to register
	 */
	public void addNodeArrivedId(int... npcIds)
	{
		addEventId(QuestEventType.ON_NODE_ARRIVED, npcIds);
	}
	
	public L2ZoneType addEnterZoneId(int zoneId)
	{
		try
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null)
			{
				zone.addQuestEvent(Quest.QuestEventType.ON_ENTER_ZONE, this);
			}
			return zone;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on addEnterZoneId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	public L2ZoneType addExitZoneId(int zoneId)
	{
		try
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null)
			{
				zone.addQuestEvent(Quest.QuestEventType.ON_EXIT_ZONE, this);
			}
			return zone;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception on addExitZoneId(): " + e.getMessage(), e);
			return null;
		}
	}
	
	// returns a random party member's L2PcInstance for the passed player's party
	// returns the passed player if he has no party.
	public L2PcInstance getRandomPartyMember(L2PcInstance player)
	{
		// NPE prevention.  If the player is null, there is nothing to return
		if (player == null)
			return null;
		if ((player.getParty() == null) || (player.getParty().getPartyMembers().isEmpty()))
			return player;
		L2Party party = player.getParty();
		return party.getPartyMembers().get(getRandom(party.getPartyMembers().size()));
	}
	
	/**
	 * Get a random party member from the specified player's party.<br>
	 * If the player is not in a party, only the player himself is checked.<br>
	 * The lucky member is chosen by standard loot roll rules -<br>
	 * each member rolls a random number, the one with the highest roll wins.
	 * @param player the player whose party to check
	 * @param npc the NPC used for distance and other checks (if {@link #checkPartyMember(L2PcInstance, L2Npc)} is overriden)
	 * @return the random party member or {@code null}
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, L2Npc npc)
	{
		if ((player == null) || !checkDistanceToTarget(player, npc))
		{
			return null;
		}
		final L2Party party = player.getParty();
		L2PcInstance luckyPlayer = null;
		if (party == null)
		{
			if (checkPartyMember(player, npc))
			{
				luckyPlayer = player;
			}
		}
		else
		{
			int highestRoll = 0;
			
			for (L2PcInstance member : party.getPartyMembers())
			{
				final int rnd = getRandom(1000);
				
				if ((rnd > highestRoll) && checkPartyMember(member, npc))
				{
					highestRoll = rnd;
					luckyPlayer = member;
				}
			}
		}
		if ((luckyPlayer != null) && checkDistanceToTarget(luckyPlayer, npc))
		{
			return luckyPlayer;
		}
		return null;
	}
	
	/**
	 * This method is called for every party member in {@link #getRandomPartyMember(L2PcInstance, L2Npc)}.<br>
	 * It is intended to be overriden by the specific quest implementations.
	 * @param player the player to check
	 * @param npc the NPC that was passed to {@link #getRandomPartyMember(L2PcInstance, L2Npc)}
	 * @return {@code true} if this party member passes the check, {@code false} otherwise
	 */
	public boolean checkPartyMember(L2PcInstance player, L2Npc npc)
	{
		return true;
	}
	
	/**
	 * Get a random party member from the player's party who has this quest at the specified quest progress.<br>
	 * If the player is not in a party, only the player himself is checked.
	 * @param player the player whose random party member state to get
	 * @param condition the quest progress step the random member should be at (-1 = check only if quest is started)
	 * @param playerChance how many times more chance does the player get compared to other party members (3 - 3x more chance).<br>
	 *            On retail servers, the killer usually gets 2-3x more chance than other party members
	 * @param target the NPC to use for the distance check (can be null)
	 * @return the {@link QuestState} object of the random party member or {@code null} if none matched the condition
	 */
	public QuestState getRandomPartyMemberState(L2PcInstance player, int condition, int playerChance, L2Npc target)
	{
		if ((player == null) || (playerChance < 1))
		{
			return null;
		}
		
		QuestState qs = player.getQuestState(getName());
		if (!player.isInParty())
		{
			if (!checkPartyMemberConditions(qs, condition, target))
			{
				return null;
			}
			if (!checkDistanceToTarget(player, target))
			{
				return null;
			}
			return qs;
		}
		
		final List<QuestState> candidates = new ArrayList<>();
		if (checkPartyMemberConditions(qs, condition, target) && (playerChance > 0))
		{
			for (int i = 0; i < playerChance; i++)
			{
				candidates.add(qs);
			}
		}
		
		for (L2PcInstance member : player.getParty().getPartyMembers())
		{
			if (member == player)
			{
				continue;
			}
			
			qs = member.getQuestState(getName());
			if (checkPartyMemberConditions(qs, condition, target))
			{
				candidates.add(qs);
			}
		}
		
		if (candidates.isEmpty())
		{
			return null;
		}
		
		qs = candidates.get(getRandom(candidates.size()));
		if (!checkDistanceToTarget(qs.getPlayer(), target))
		{
			return null;
		}
		return qs;
	}
	
	private boolean checkPartyMemberConditions(QuestState qs, int condition, L2Npc npc)
	{
		return ((qs != null) && ((condition == -1) ? qs.isStarted() : qs.isCond(condition)) && checkPartyMember(qs, npc));
	}
	
	private static boolean checkDistanceToTarget(L2PcInstance player, L2Npc target)
	{
		return ((target == null) || ct25.xtreme.gameserver.util.Util.checkIfInRange(1500, player, target, true));
	}
	
	/**
	 * This method is called for every party member in {@link #getRandomPartyMemberState(L2PcInstance, int, int, L2Npc)} if/after all the standard checks are passed.<br>
	 * It is intended to be overriden by the specific quest implementations.<br>
	 * It can be used in cases when there are more checks performed than simply a quest condition check,<br>
	 * for example, if an item is required in the player's inventory.
	 * @param qs the {@link QuestState} object of the party member
	 * @param npc the NPC that was passed as the last parameter to {@link #getRandomPartyMemberState(L2PcInstance, int, int, L2Npc)}
	 * @return {@code true} if this party member passes the check, {@code false} otherwise
	 */
	public boolean checkPartyMember(QuestState qs, L2Npc npc)
	{
		return true;
	}
	
	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 * @param player: the instance of a player whose party is to be searched
	 * @param value: the value of the "cond" variable that must be matched
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified
	 * 			condition, or null if no match.
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String value)
	{
		return getRandomPartyMember(player, "cond", value);
	}
	
	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 * @param player: the instance of a player whose party is to be searched
	 * @param var/value: a tuple specifying a quest condition that must be satisfied for
	 *     a party member to be considered.
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified
	 * 				condition, or null if no match.  If the var is null, any random party
	 * 				member is returned (i.e. no condition is applied).
	 * 				The party member must be within 1500 distance from the target of the reference
	 * 				player, or if no target exists, 1500 distance from the player itself.
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String var, String value)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
			return null;
			
		// for null var condition, return any random party member.
		if (var == null)
			return getRandomPartyMember(player);
			
		// normal cases...if the player is not in a party, check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if ((party == null) || (party.getPartyMembers().isEmpty()))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && (temp.get(var) != null) && (temp.get(var)).equalsIgnoreCase(value))
				return player; // match
				
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		FastList<L2PcInstance> candidates = new FastList<L2PcInstance>();
		
		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if (target == null)
			target = player;
			
		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			if (partyMember == null)
				continue;
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.get(var) != null) && (temp.get(var)).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 1500, true, false))
				candidates.add(partyMember);
		}
		// if there was no match, return null...
		if (candidates.isEmpty())
			return null;
			
		// if a match was found from the party, return one of them at random.
		return candidates.get(getRandom(candidates.size()));
	}
	
	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 * @param player: the instance of a player whose party is to be searched
	 * @param state: the state in which the party member's queststate must be in order to be considered.
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified
	 * 				condition, or null if no match.  If the var is null, any random party
	 * 				member is returned (i.e. no condition is applied).
	 */
	public L2PcInstance getRandomPartyMemberState(L2PcInstance player, byte state)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
			return null;
			
		// normal cases...if the player is not in a partym check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if ((party == null) || (party.getPartyMembers().isEmpty()))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state))
				return player; // match
				
			return null; // no match
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		FastList<L2PcInstance> candidates = new FastList<L2PcInstance>();
		
		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if (target == null)
			target = player;
			
		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			if (partyMember == null)
				continue;
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state) && partyMember.isInsideRadius(target, 1500, true, false))
				candidates.add(partyMember);
		}
		// if there was no match, return null...
		if (candidates.isEmpty())
			return null;
			
		// if a match was found from the party, return one of them at random.
		return candidates.get(getRandom(candidates.size()));
	}
	
	/**
	 * Get a random party member with required cond value.
	 * @param player the instance of a player whose party is to be searched
	 * @param cond the value of the "cond" variable that must be matched
	 * @return a random party member that matches the specified condition, or {@code null} if no match was found
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, int cond)
	{
		return getRandomPartyMember(player, "cond", String.valueOf(cond));
	}
	
	/**
	 * Show an on screen message to the player.
	 * @param player the player to display the message to
	 * @param text the message to display
	 * @param time the duration of the message in milliseconds
	 */
	public static void showOnScreenMsg(L2PcInstance player, String text, int time)
	{
		player.sendPacket(new ExShowScreenMessage(text, time));
	}
	
	/**
	 * Show HTML file to client
	 * @param fileName
	 * @return String : message sent to client
	 */
	public String showHtmlFile(L2PcInstance player, String fileName)
	{
		boolean questwindow = true;
		if (fileName.endsWith(".html"))
			questwindow = false;
		int questId = getId();
		//Create handler to file linked to the quest
		String content = getHtm(player.getHtmlPrefix(), fileName);
		
		if (player.getTarget() != null)
			content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
			
		//Send message to client if message not empty
		if (content != null)
		{
			if (questwindow && questId > 0 && questId < 20000 && questId != 999)
			{
				NpcQuestHtmlMessage npcReply = new NpcQuestHtmlMessage(5, questId);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			else
			{
				NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				player.sendPacket(npcReply);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		return content;
	}
	
	/**
	 * Return HTML file contents
	 * @param player
	 * @param fileName
	 * @return
	 */
	public String getHtm(String prefix, String fileName)
	{
		String content = HtmCache.getInstance().getHtm(prefix, "data/scripts/" + getDescr().toLowerCase() + "/" + getName() + "/" + fileName);
		
		if (content == null)
		{
			content = HtmCache.getInstance().getHtm(prefix, "data/scripts/quests/Q" + getName() + "/" + fileName);
			if (content == null)
				content = HtmCache.getInstance().getHtmForce(prefix, "data/scripts/quests/" + getName() + "/" + fileName);
		}
		
		return content;
	}
	
	// Method - Public
	
	/**
	 * Add a temporary (quest) spawn
	 * Return instance of newly spawned npc
	 */
	public static L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, false);
	}
	
	/**
	 * Add a temporary (quest) spawn
	 * Return instance of newly spawned npc
	 * with summon animation
	 */
	public L2Npc addSpawn(int npcId, L2Character cha, boolean isSummonSpawn)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, isSummonSpawn);
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay, false);
	}
	
	/**
	 * @param npcId
	 * @param loc
	 * @param randomOffSet
	 * @param despawnDelay
	 * @return
	 */
	public L2Npc addSpawn(int npcId, Location loc, boolean randomOffSet, long despawnDelay)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffSet, despawnDelay, false, 0);
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, instanceId, -1);
	}
	
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, instanceId);
	}
	
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId, int onKillDelay)
	{
		//sometimes (for timed addspawn) when the spawn is called the instance not exists anymore
		//if (instanceId != 0 && !InstanceManager.getInstance().instanceExist(instanceId))
		//{
		//	return null;
		//}
		
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
				// reaches here, xyz have become 0!  Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc!  This will NOT work
				// with quest spawns!  For both of the above cases, we need a fail-safe spawn.  For this, we use the
				// default spawn location, which is at the player's loc.
				if ((x == 0) && (y == 0))
				{
					_log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}
				if (randomOffset)
				{
					int offset;
					
					offset = getRandom(2); // Get the direction of the offset
					if (offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= getRandom(50, 100);
					x += offset;
					
					offset = getRandom(2); // Get the direction of the offset
					if (offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= getRandom(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z + 20);
				spawn.stopRespawn();
				result = spawn.spawnOne(isSummonSpawn);
				
				if (despawnDelay > 0)
					result.scheduleDespawn(despawnDelay);
					
				return result;
			}
		}
		catch (Exception e1)
		{
			_log.warning("Could not spawn Npc " + npcId);
		}
		
		return null;
	}
	
	public L2Trap addTrap(int trapId, int x, int y, int z, int heading, L2Skill skill, int instanceId)
	{
		L2NpcTemplate TrapTemplate = NpcTable.getInstance().getTemplate(trapId);
		L2Trap trap = new L2TrapInstance(IdFactory.getInstance().getNextId(), TrapTemplate, instanceId, -1, skill);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(heading);
		//L2World.getInstance().storeObject(trap);
		trap.spawnMe(x, y, z);
		
		return trap;
	}
	
	public L2Npc addMinion(L2MonsterInstance master, int minionId)
	{
		return MinionList.spawnMinion(master, minionId);
	}
	
	public int[] getRegisteredItemIds()
	{
		return questItemIds;
	}
	
	@Override
	public String getScriptName()
	{
		return this.getName();
	}
	
	@Override
	public void setActive(boolean status)
	{
		// TODO implement me
	}
	
	@Override
	public boolean reload()
	{
		unload();
		return super.reload();
	}
	
	@Override
	public boolean unload()
	{
		return unload(true);
	}
	
	public boolean unload(boolean removeFromList)
	{
		this.saveGlobalData();
		// cancel all pending timers before reloading.
		// if timers ought to be restarted, the quest can take care of it
		// with its code (example: save global data indicating what timer must
		// be restarted).
		for (FastList<QuestTimer> timers : _allEventTimers.values())
			for (QuestTimer timer : timers)
				timer.cancel();
		_allEventTimers.clear();
		
		for (Integer npcId : _questInvolvedNpcs)
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId.intValue());
			if (template != null)
			{
				return QuestManager.getInstance().removeQuest(this);
			}
		}
		_questInvolvedNpcs.clear();
		
		if (removeFromList)
			return QuestManager.getInstance().removeQuest(this);
		else
			return true;
	}
	
	public Set<Integer> getQuestInvolvedNpcs()
	{
		return _questInvolvedNpcs;
	}
	
	@Override
	public ScriptManager<?> getScriptManager()
	{
		return QuestManager.getInstance();
	}
	
	public void setOnEnterWorld(boolean val)
	{
		_onEnterWorld = val;
	}
	
	public boolean getOnEnterWorld()
	{
		return _onEnterWorld;
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param holder
	 */
	public static void rewardItems(L2PcInstance player, ItemHolder holder)
	{
		rewardItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Give a reward to player using multipliers.
	 * @param player the player to whom to give the item
	 * @param itemId the Id of the item to give
	 * @param count the amount of items to give
	 */
	public static void rewardItems(L2PcInstance player, int itemId, long count)
	{
		if (count <= 0)
		{
			return;
		}
		
		L2ItemInstance _tmpItem = ItemTable.getInstance().createDummyItem(itemId);
		
		if (_tmpItem == null)
		{
			return;
		}
		
		try
		{
			if (itemId == PcInventory.ADENA_ID)
			{
				count *= Config.RATE_QUEST_REWARD_ADENA;
			}
			else if (Config.RATE_QUEST_REWARD_USE_MULTIPLIERS)
			{
				if (_tmpItem.isEtcItem())
				{
					switch (_tmpItem.getEtcItem().getItemType())
					{
						case POTION:
							count *= Config.RATE_QUEST_REWARD_POTION;
							break;
						case SCRL_ENCHANT_WP:
						case SCRL_ENCHANT_AM:
						case SCROLL:
							count *= Config.RATE_QUEST_REWARD_SCROLL;
							break;
						case RECIPE:
							count *= Config.RATE_QUEST_REWARD_RECIPE;
							break;
						case MATERIAL:
							count *= Config.RATE_QUEST_REWARD_MATERIAL;
							break;
						default:
							count *= Config.RATE_QUEST_REWARD;
					}
				}
			}
			else
			{
				count *= Config.RATE_QUEST_REWARD;
			}
		}
		catch (Exception e)
		{
			count = Long.MAX_VALUE;
		}
		
		// Add items to player's inventory
		L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * Send the system message and the status update packets to the player.
	 * @param player the player that has got the item
	 * @param item the item obtain by the player
	 * @param count the item count
	 */
	private static void sendItemGetMessage(L2PcInstance player, L2ItemInstance item, long count)
	{
		// If item for reward is gold, send message of gold reward to client
		if (item.getId() == PcInventory.ADENA_ID)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addItemNumber(count);
			player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(item);
				player.sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	/**
	 * Take an amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param itemId the Id of the item to take
	 * @param amount the amount to take
	 * @return {@code true} if any items were taken, {@code false} otherwise
	 */
	public static boolean takeItems(L2PcInstance player, int itemId, long amount)
	{
		// Get object item from player's inventory list
		L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return false;
		}
		
		// Tests on count value in order not to have negative value
		if ((amount < 0) || (amount > item.getCount()))
		{
			amount = item.getCount();
		}
		
		// Destroy the quantity of items wanted
		if (item.isEquipped())
		{
			L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		return player.destroyItemByItemId("Quest", itemId, amount, player, true);
	}
	
	/**
	 * Take a set amount of a specified item from player's inventory.
	 * @param player the player whose item to take
	 * @param holder the {@link ItemHolder} object containing the ID and count of the item to take
	 * @return {@code true} if the item was taken, {@code false} otherwise
	 */
	protected static boolean takeItem(L2PcInstance player, ItemHolder holder)
	{
		if (holder == null)
		{
			return false;
		}
		return takeItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * Take a set amount of all specified items from player's inventory.
	 * @param player the player whose items to take
	 * @param itemList the list of {@link ItemHolder} objects containing the IDs and counts of the items to take
	 * @return {@code true} if all items were taken, {@code false} otherwise
	 */
	protected static boolean takeAllItems(L2PcInstance player, ItemHolder... itemList)
	{
		if ((itemList == null) || (itemList.length == 0))
		{
			return false;
		}
		// first check if the player has all items to avoid taking half the items from the list
		if (!hasAllItems(player, true, itemList))
		{
			return false;
		}
		for (ItemHolder item : itemList)
		{
			// this should never be false, but just in case
			if (!takeItem(player, item))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Take an amount of all specified items from player's inventory.
	 * @param player the player whose items to take
	 * @param amount the amount to take of each item
	 * @param itemIds a list or an array of Ids of the items to take
	 * @return {@code true} if all items were taken, {@code false} otherwise
	 */
	public boolean takeItems(L2PcInstance player, int amount, int... itemIds)
	{
		boolean check = true;
		if (itemIds != null)
		{
			for (int item : itemIds)
			{
				check &= takeItems(player, item, amount);
			}
		}
		return check;
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param itemId
	 * @param count
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count)
	{
		giveItems(player, itemId, count, 0);
	}
	
	/**
	 * Give item/reward to the player
	 * @param player
	 * @param holder
	 */
	protected static void giveItems(L2PcInstance player, ItemHolder holder)
	{
		giveItems(player, holder.getId(), holder.getCount());
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param enchantlevel
	 */
	public static void giveItems(L2PcInstance player, int itemId, long count, int enchantlevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// If item for reward is adena (Id=57), modify count with rate for quest reward if rates available
		if ((itemId == PcInventory.ADENA_ID) && (enchantlevel == 0))
		{
			count = (long) (count * Config.RATE_QUEST_REWARD_ADENA);
		}
		
		// Add items to player's inventory
		L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((enchantlevel > 0) && (itemId != PcInventory.ADENA_ID))
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param count
	 * @param attributeId
	 * @param attributeLevel
	 */
	public void giveItems(L2PcInstance player, int itemId, long count, byte attributeId, int attributeLevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		// Add items to player's inventory
		L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
		
		if (item == null)
		{
			return;
		}
		
		// set enchant level for item if that item is not adena
		if ((attributeId >= 0) && (attributeLevel > 0))
		{
			item.setElementAttr(attributeId, attributeLevel);
			if (item.isEquipped())
			{
				item.updateElementAttrBonus(player);
			}
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
		}
		
		sendItemGetMessage(player, item, count);
	}
	
	/**
	 * Drop Quest item using Config.RATE_QUEST_DROP
	 * @param player
	 * @param itemId int Item Identifier of the item to be dropped
	 * @param count (minCount, maxCount) long Quantity of items to be dropped
	 * @param neededCount Quantity of items needed for quest
	 * @param dropChance int Base chance of drop, same as in droplist
	 * @param sound boolean indicating whether to play sound
	 * @return boolean indicating whether player has requested number of items
	 */
	public boolean dropQuestItems(L2PcInstance player, int itemId, int count, long neededCount, int dropChance, boolean sound)
	{
		return dropQuestItems(player, itemId, count, count, neededCount, dropChance, sound);
	}
	
	/**
	 * @param player
	 * @param itemId
	 * @param minCount
	 * @param maxCount
	 * @param neededCount
	 * @param dropChance
	 * @param sound
	 * @return
	 */
	public boolean dropQuestItems(L2PcInstance player, int itemId, int minCount, int maxCount, long neededCount, int dropChance, boolean sound)
	{
		dropChance *= Config.RATE_QUEST_DROP / ((player.getParty() != null) ? player.getParty().getMemberCount() : 1);
		long currentCount = getQuestItemsCount(player, itemId);
		
		if ((neededCount > 0) && (currentCount >= neededCount))
		{
			return true;
		}
		
		if (currentCount >= neededCount)
		{
			return true;
		}
		
		long itemCount = 0;
		int random = getRandom(L2DropData.MAX_CHANCE);
		
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
			{
				itemCount += getRandom(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (itemCount > 0)
		{
			// if over neededCount, just fill the gap
			if ((neededCount > 0) && ((currentCount + itemCount) > neededCount))
			{
				itemCount = neededCount - currentCount;
			}
			
			// Inventory slot check
			if (!player.getInventory().validateCapacityByItemId(itemId))
			{
				return false;
			}
			
			// Give the item to Player
			player.addItem("Quest", itemId, itemCount, player.getTarget(), true);
			
			if (sound)
			{
				playSound(player, ((currentCount + itemCount) < neededCount) ? QuestSound.ITEMSOUND_QUEST_ITEMGET : QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		
		return ((neededCount > 0) && ((currentCount + itemCount) >= neededCount));
	}
	
	/**
	 * Registers all items that have to be destroyed in case player abort the quest or finish it.
	 * @param items
	 */
	public void registerQuestItems(int... items)
	{
		questItemIds = items;
	}
	
	/**
	 * If a quest is set as custom, it will display it's name in the NPC Quest List.<br>
	 * Retail quests are unhardcoded to display the name using a client string.
	 * @param val if {@code true} the quest script will be set as custom quest.
	 */
	public void setIsCustom(boolean val)
	{
		_isCustom = val;
	}
	
	/**
	 * @return {@code true} if the quest script is a custom quest, {@code false} otherwise.
	 */
	public boolean isCustomQuest()
	{
		return _isCustom;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if at least one items exist in player's inventory, {@code false} otherwise
	 */
	public boolean hasAtLeastOneQuestItem(L2PcInstance player, int... itemIds)
	{
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the enchantment level of an item in player's inventory.
	 * @param player the player whose item to check
	 * @param itemId the ID of the item whose enchantment level to get
	 * @return the enchantment level of the item or 0 if the item was not found
	 */
	public static int getEnchantLevel(L2PcInstance player, int itemId)
	{
		final L2ItemInstance enchantedItem = player.getInventory().getItemByItemId(itemId);
		if (enchantedItem == null)
		{
			return 0;
		}
		return enchantedItem.getEnchantLevel();
	}
	
	/**
	 * Give Adena to the player.
	 * @param player the player to whom to give the Adena
	 * @param count the amount of Adena to give
	 * @param applyRates if {@code true} quest rates will be applied to the amount
	 */
	public void giveAdena(L2PcInstance player, long count, boolean applyRates)
	{
		if (applyRates)
		{
			rewardItems(player, Inventory.ADENA_ID, count);
		}
		else
		{
			giveItems(player, Inventory.ADENA_ID, count);
		}
	}
	
	/**
	 * Give the specified player a set amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player the player to give the item(s) to
	 * @param itemId the ID of the item to give
	 * @param amountToGive the amount of items to give
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance the drop chance as a decimal digit from 0 to 1
	 * @param playSound if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if limit > 0 and the limit was reached or if limit <= 0 and items were given; {@code false} in all other cases
	 */
	public static boolean giveItemRandomly(L2PcInstance player, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, null, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	/**
	 * Give the specified player a set amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player the player to give the item(s) to
	 * @param npc the NPC that "dropped" the item (can be null)
	 * @param itemId the ID of the item to give
	 * @param amountToGive the amount of items to give
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance the drop chance as a decimal digit from 0 to 1
	 * @param playSound if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if limit > 0 and the limit was reached or if limit <= 0 and items were given; {@code false} in all other cases
	 */
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, npc, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}
	
	/**
	 * Give the specified player a random amount of items if he is lucky enough.<br>
	 * Not recommended to use this for non-stacking items.
	 * @param player the player to give the item(s) to
	 * @param npc the NPC that "dropped" the item (can be null)
	 * @param itemId the ID of the item to give
	 * @param minAmount the minimum amount of items to give
	 * @param maxAmount the maximum amount of items to give (will give a random amount between min/maxAmount multiplied by quest rates)
	 * @param limit the maximum amount of items the player can have. Won't give more if this limit is reached. 0 - no limit.
	 * @param dropChance the drop chance as a decimal digit from 0 to 1
	 * @param playSound if true, plays ItemSound.quest_itemget when items are given and ItemSound.quest_middle when the limit is reached
	 * @return {@code true} if limit > 0 and the limit was reached or if limit <= 0 and items were given; {@code false} in all other cases
	 */
	public static boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{
		final long currentCount = getQuestItemsCount(player, itemId);
		
		if ((limit > 0) && (currentCount >= limit))
		{
			return true;
		}
		
		minAmount *= Config.RATE_QUEST_DROP;
		maxAmount *= Config.RATE_QUEST_DROP;
		dropChance *= Config.RATE_QUEST_DROP; // TODO separate configs for rate and amount
		if ((npc != null) && Config.L2JMOD_CHAMPION_ENABLE && npc.isChampion())
		{
			dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
			if ((itemId == Inventory.ADENA_ID) || (itemId == Inventory.ANCIENT_ADENA_ID))
			{
				minAmount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
				maxAmount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
			}
			else
			{
				minAmount *= Config.L2JMOD_CHAMPION_REWARDS;
				maxAmount *= Config.L2JMOD_CHAMPION_REWARDS;
			}
		}
		
		long amountToGive = ((minAmount == maxAmount) ? minAmount : Rnd.get(minAmount, maxAmount));
		final double random = Rnd.nextDouble();
		// Inventory slot check (almost useless for non-stacking items)
		if ((dropChance >= random) && (amountToGive > 0) && player.getInventory().validateCapacityByItemId(itemId))
		{
			if ((limit > 0) && ((currentCount + amountToGive) > limit))
			{
				amountToGive = limit - currentCount;
			}
			
			// Give the item to player
			L2ItemInstance item = player.addItem("Quest", itemId, amountToGive, npc, true);
			if (item != null)
			{
				// limit reached (if there is no limit, this block doesn't execute)
				if ((currentCount + amountToGive) == limit)
				{
					if (playSound)
					{
						playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					return true;
				}
				
				if (playSound)
				{
					playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				// if there is no limit, return true every time an item is given
				if (limit <= 0)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Check if the player has the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @return {@code true} if the player has the required count of the item
	 */
	protected static boolean hasItem(L2PcInstance player, ItemHolder item)
	{
		return hasItem(player, item, true);
	}
	
	/**
	 * Check if the player has the required count of the specified item in his inventory.
	 * @param player the player whose inventory to check for the specified item
	 * @param item the {@link ItemHolder} object containing the ID and count of the item to check
	 * @param checkCount if {@code true}, check if each item is at least of the count specified in the ItemHolder,<br>
	 *            otherwise check only if the player has the item at all
	 * @return {@code true} if the player has the item
	 */
	protected static boolean hasItem(L2PcInstance player, ItemHolder item, boolean checkCount)
	{
		if (item == null)
		{
			return false;
		}
		if (checkCount)
		{
			return (getQuestItemsCount(player, item.getId()) >= item.getCount());
		}
		return hasQuestItems(player, item.getId());
	}
	
	/**
	 * Check if the player has all the specified items in his inventory and, if necessary, if their count is also as required.
	 * @param player the player whose inventory to check for the specified item
	 * @param checkCount if {@code true}, check if each item is at least of the count specified in the ItemHolder,<br>
	 *            otherwise check only if the player has the item at all
	 * @param itemList a list of {@link ItemHolder} objects containing the IDs of the items to check
	 * @return {@code true} if the player has all the items from the list
	 */
	protected static boolean hasAllItems(L2PcInstance player, boolean checkCount, ItemHolder... itemList)
	{
		if ((itemList == null) || (itemList.length == 0))
		{
			return false;
		}
		for (ItemHolder item : itemList)
		{
			if (!hasItem(player, item, checkCount))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check for an item in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemId the ID of the item to check for
	 * @return {@code true} if the item exists in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(L2PcInstance player, int itemId)
	{
		return (player.getInventory().getItemByItemId(itemId) != null);
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param player the player whose inventory to check for quest items
	 * @param itemIds a list of item Ids to check for
	 * @return {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public static boolean hasQuestItems(L2PcInstance player, int... itemIds)
	{
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the amount of an item in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemId the ID of the item whose amount to get
	 * @return the amount of the specified item in player's inventory
	 */
	public static long getQuestItemsCount(L2PcInstance player, int itemId)
	{
		return player.getInventory().getInventoryItemCount(itemId, -1);
	}
	
	/**
	 * Get the total amount of all specified items in player's inventory.
	 * @param player the player whose inventory to check
	 * @param itemIds a list of IDs of items whose amount to get
	 * @return the summary amount of all listed items in player's inventory
	 */
	public long getQuestItemsCount(L2PcInstance player, int... itemIds)
	{
		long count = 0;
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (item == null)
			{
				continue;
			}
			
			for (int itemId : itemIds)
			{
				if (item.getId() == itemId)
				{
					if ((count + item.getCount()) > Long.MAX_VALUE)
					{
						return Long.MAX_VALUE;
					}
					count += item.getCount();
				}
			}
		}
		return count;
	}
	
	/**
	 * Remove all quest items associated with this quest from the specified player's inventory.
	 * @param player the player whose quest items to remove
	 */
	public void removeRegisteredQuestItems(L2PcInstance player)
	{
		takeItems(player, -1, questItemIds);
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param player the player whom to send the packet
	 * @param sound the name of the sound to play
	 */
	public static void playSound(L2PcInstance player, String sound)
	{
		player.sendPacket(QuestSound.getSound(sound));
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param player the player whom to send the packet
	 * @param sound the {@link QuestSound} object of the sound to play
	 */
	public static void playSound(L2PcInstance player, QuestSound sound)
	{
		player.sendPacket(sound.getPacket());
	}
	
	/**
	 * Add EXP and SP as quest reward.
	 * @param player the player whom to reward with the EXP/SP
	 * @param exp the amount of EXP to give to the player
	 * @param sp the amount of SP to give to the player
	 */
	public static void addExpAndSp(L2PcInstance player, long exp, int sp)
	{
		player.addExpAndSp((long) player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUEST_REWARD_XP, null, null), (int) player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUEST_REWARD_SP, null, null));
	}
	
	/**
	 * Get a random integer from 0 (inclusive) to {@code max} (exclusive).<br>
	 * Use this method instead of importing {@link br.xtreme.util.Rnd} utility.
	 * @param max the maximum value for randomization
	 * @return a random integer number from 0 to {@code max - 1}
	 */
	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * Get a random integer from {@code min} (inclusive) to {@code max} (inclusive).<br>
	 * Use this method instead of importing {@link br.xtreme.util.Rnd} utility.
	 * @param min the minimum value for randomization
	 * @param max the maximum value for randomization
	 * @return a random integer number from {@code min} to {@code max}
	 */
	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	/**
	 * Get a random boolean.<br>
	 * Use this method inbr.xtremerting {@link br.xtreme.util.Rnd} utility.
	 * @return {@code true} or {@code false} randomly
	 */
	public static boolean getRandomBoolean()
	{
		return Rnd.nextBoolean();
	}
	
	/**
	 * Get the ID of the item equipped in the specified inventory slot of the player.
	 * @param player the player whose inventory to check
	 * @param slot the location in the player's inventory to check
	 * @return the ID of the item equipped in the specified inventory slot or 0 if the slot is empty or item is {@code null}.
	 */
	public static int getItemEquipped(L2PcInstance player, int slot)
	{
		return player.getInventory().getPaperdollItemId(slot);
	}
	
	/**
	 * Execute a procedure for each player depending on the parameters.
	 * @param player the player on which the procedure will be executed
	 * @param npc the related NPC
	 * @param isPet {@code true} if the event that called this method was originated by the player's summon, {@code false} otherwise
	 * @param includeParty if {@code true}, #actionForEachPlayer(L2PcInstance, L2Npc, boolean) will be called with the player's party members
	 * @param includeCommandChannel if {@code true}, {@link #actionForEachPlayer(L2PcInstance, L2Npc, boolean)} will be called with the player's command channel members
	 * @see #actionForEachPlayer(L2PcInstance, L2Npc, boolean)
	 */
	public final void executeForEachPlayer(L2PcInstance player, final L2Npc npc, final boolean isPet, boolean includeParty, boolean includeCommandChannel)
	{
		if ((includeParty || includeCommandChannel) && player.isInParty())
		{
			if (includeCommandChannel && player.getParty().isInCommandChannel())
			{
				player.getParty().getCommandChannel().forEachMember(new IProcedure<L2PcInstance, Boolean>()
				{
					@Override
					public Boolean execute(L2PcInstance member)
					{
						actionForEachPlayer(member, npc, isPet);
						return true;
					}
				});
			}
			else if (includeParty)
			{
				player.getParty().forEachMember(new IProcedure<L2PcInstance, Boolean>()
				{
					@Override
					public Boolean execute(L2PcInstance member)
					{
						actionForEachPlayer(member, npc, isPet);
						return true;
					}
				});
			}
		}
		else
		{
			actionForEachPlayer(player, npc, isPet);
		}
	}
	
	/**
	 * Overridable method called from {@link #executeForEachPlayer(L2PcInstance, L2Npc, boolean, boolean, boolean)}
	 * @param player the player on which the action will be run
	 * @param npc the NPC related to this action
	 * @param isPet {@code true} if the event that called this method was originated by the player's summon
	 */
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isPet)
	{
		// To be overridden in quest scripts.
	}
	
	/**
	 * Open a door if it is present on the instance and its not open.
	 * @param doorId the ID of the door to open
	 * @param instanceId the ID of the instance the door is in (0 if the door is not not inside an instance)
	 */
	public void openDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": called openDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (!door.getOpen())
		{
			door.openMe();
		}
	}
	
	/**
	 * Close a door if it is present in a specified the instance and its open.
	 * @param doorId the ID of the door to close
	 * @param instanceId the ID of the instance the door is in (0 if the door is not not inside an instance)
	 */
	public void closeDoor(int doorId, int instanceId)
	{
		final L2DoorInstance door = getDoor(doorId, instanceId);
		if (door == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": called closeDoor(" + doorId + ", " + instanceId + "); but door wasnt found!", new NullPointerException());
		}
		else if (door.getOpen())
		{
			door.closeMe();
		}
	}
	
	/**
	 * Retrieve a door from an instance or the real world.
	 * @param doorId the ID of the door to get
	 * @param instanceId the ID of the instance the door is in (0 if the door is not not inside an instance)
	 * @return the found door or {@code null} if no door with that ID and instance ID was found
	 */
	public L2DoorInstance getDoor(int doorId, int instanceId)
	{
		L2DoorInstance door = null;
		if (instanceId <= 0)
		{
			door = DoorTable.getInstance().getDoor(doorId);
		}
		else
		{
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			if (inst != null)
			{
				door = inst.getDoor(doorId);
			}
		}
		return door;
	}
	
	/**
	 * Teleport a player into/out of an instance.
	 * @param player the player to teleport
	 * @param loc the {@link Location} object containing the destination coordinates
	 * @param instanceId the ID of the instance to teleport the player to (0 to teleport out of an instance)
	 */
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId)
	{
		teleportPlayer(player, loc, instanceId, true);
	}
	
	/**
	 * Teleport a player into/out of an instance.
	 * @param player the player to teleport
	 * @param loc the {@link Location} object containing the destination coordinates
	 * @param instanceId the ID of the instance to teleport the player to (0 to teleport out of an instance)
	 * @param allowRandomOffset if {@code true}, will randomize the teleport coordinates by +/-Config.MAX_OFFSET_ON_TELEPORT
	 */
	public void teleportPlayer(L2PcInstance player, Location loc, int instanceId, boolean allowRandomOffset)
	{
		loc.setInstanceId(instanceId);
		player.teleToLocation(loc, allowRandomOffset);
	}
	
	public L2Npc spawnNpc(int npcId, Location loc, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setLocx(loc.getX());
			npcSpawn.setLocy(loc.getY());
			npcSpawn.setLocz(loc.getZ());
			npcSpawn.setHeading(loc.getHeading());
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0, false, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param isSummonSpawn if {@code true}, displays a summon animation on NPC spawn
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean isSummonSpawn)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), false, 0, isSummonSpawn, 0);
	}
	
	/**
	 * Add a temporary spawn of the specified NPC.
	 * @param npcId the ID of the NPC to spawn
	 * @param pos the object containing the spawn location coordinates
	 * @param randomOffset if {@code true}, adds +/- 50~100 to X/Y coordinates of the spawn location
	 * @param despawnDelay time in milliseconds till the NPC is despawned (0 - only despawned on server shutdown)
	 * @return the {@link L2Npc} object of the newly spawned NPC or {@code null} if the NPC doesn't exist
	 * @see #addSpawn(int, IPositionable, boolean, long, boolean, int)
	 * @see #addSpawn(int, int, int, int, int, boolean, long, boolean, int)
	 */
	public static L2Npc addSpawn(int npcId, IPositionable pos, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading(), randomOffset, despawnDelay, false, 0);
	}
	
	/**
	 * Instantly cast a skill upon the given target.
	 * @param npc the caster NPC
	 * @param target the target of the cast
	 * @param skill the skill to cast
	 */
	protected void castSkill(L2Npc npc, L2Playable target, SkillHolder skill)
	{
		npc.setTarget(target);
		npc.doCast(skill.getSkill());
	}
	
	/**
	 * Instantly cast a skill upon the given target.
	 * @param npc the caster NPC
	 * @param target the target of the cast
	 * @param skill the skill to cast
	 */
	protected void castSkill(L2Npc npc, L2Playable target, L2Skill skill)
	{
		npc.setTarget(target);
		npc.doCast(skill);
	}
	
	/**
	 * Monster is running and attacking the playable.
	 * @param npc the NPC that performs the attack
	 * @param playable the player
	 */
	protected void addAttackPlayerDesire(L2Npc npc, L2Playable playable)
	{
		addAttackPlayerDesire(npc, playable, 999);
	}
	
	/**
	 * Monster is running and attacking the target.
	 * @param npc the NPC that performs the attack
	 * @param target the target of the attack
	 * @param desire the desire to perform the attack
	 */
	protected void addAttackPlayerDesire(L2Npc npc, L2Playable target, int desire)
	{
		if (npc instanceof L2Attackable)
		{
			((L2Attackable) npc).addDamageHate(target, 0, desire);
		}
		npc.setIsRunning(true);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}
	
	/**
	 * Adds the desire to cast a skill to the given NPC.
	 * @param npc the NPC whom cast the skill
	 * @param target the skill target
	 * @param skill the skill to cast
	 * @param desire the desire to cast the skill
	 */
	protected void addSkillCastDesire(L2Npc npc, L2Character target, SkillHolder skill, int desire)
	{
		addSkillCastDesire(npc, target, skill.getSkill(), desire);
	}
	
	/**
	 * Adds the desire to cast a skill to the given NPC.
	 * @param npc the NPC whom cast the skill
	 * @param target the skill target
	 * @param skill the skill to cast
	 * @param desire the desire to cast the skill
	 */
	protected void addSkillCastDesire(L2Npc npc, L2Character target, L2Skill skill, int desire)
	{
		if (npc instanceof L2Attackable)
		{
			((L2Attackable) npc).addDamageHate(target, 0, desire);
		}
		npc.setTarget(target);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
}
