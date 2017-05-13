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
package instances.IceQueenCastleUltimateBattle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2GrandBossInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2NpcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2QuestGuardInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2RaidBossInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.model.variables.NpcVariables;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.ExChangeAreaState;
import ct25.xtreme.gameserver.network.serverpackets.ExSendUIEvent;
import ct25.xtreme.gameserver.network.serverpackets.ExShowBroadcastMessage;
import ct25.xtreme.gameserver.network.serverpackets.OnEventTrigger;
import ct25.xtreme.gameserver.network.serverpackets.Scenkos;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Util;
import quests.Q10286_ReunionWithSirra.Q10286_ReunionWithSirra;

/**
 * Ice Queen's Castle (Ultimate Battle) instance zone.
 * @author Browser (refactor based on script normal battle )
 * @author original java script: St3eT Add Missing movie (22) This script is beta test (possible errors)
 */
public final class IceQueenCastleUltimateBattle extends L2AttackableAIScript
{
	protected class IQCUBWorld extends InstanceWorld
	{
		List<L2Npc> knightStatues = new ArrayList<>();
		List<L2Attackable> spawnedMobs = new CopyOnWriteArrayList<>();
		L2NpcInstance controller = null;
		L2GrandBossInstance freya = null;
		L2QuestGuardInstance supp_Jinia = null;
		L2QuestGuardInstance supp_Kegor = null;
		boolean isSupportActive = false;
		boolean canSpawnMobs = true;
	}

	// Only for GM tests
	private final boolean debug = false;

	// Npcs
	private static final int FREYA_THRONE = 29177; // First freya
	private static final int FREYA_SPELLING = 29178; // Second freya
	private static final int FREYA_STAND = 29180; // Last freya
	private static final int INVISIBLE_NPC = 18919;
	private static final int KNIGHT = 18856; // Archery Knight
	private static final int GLACIER = 18853; // Glacier
	private static final int BREATH = 18854; // Archer's Breath
	private static final int GLAKIAS = 25700; // Glakias (Archery Knight Captain)
	private static final int SIRRA = 32762; // Sirra
	private static final int JINIA = 32781; // Jinia
	private static final int SUPP_JINIA = 18850; // Jinia
	private static final int SUPP_KEGOR = 18851; // Kegor

	// Skills
	private static final SkillHolder BLIZZARD = new SkillHolder(6275, 1); // Eternal Blizzard
	private static final SkillHolder BLIZZARD_BREATH = new SkillHolder(6299, 1); // Breath of Ice Palace - Ice Storm
	private static final SkillHolder SUICIDE_BREATH = new SkillHolder(6300, 1); // Self-Destruction
	private static final SkillHolder JINIA_SUPPORT = new SkillHolder(6288, 1); // Jinia's Prayer
	private static final SkillHolder KEGOR_SUPPORT = new SkillHolder(6289, 1); // Kegor's Courage
	private static final SkillHolder ICE_STONE = new SkillHolder(6301, 1); // Cold Mana's Fragment
	private static final SkillHolder CANCEL = new SkillHolder(4618, 1); // NPC Cancel PC Target
	private static final SkillHolder POWER_STRIKE = new SkillHolder(6293, 1); // Power Strike
	private static final SkillHolder POINT_TARGET = new SkillHolder(6295, 1); // Point Target
	private static final SkillHolder CYLINDER_THROW = new SkillHolder(6297, 1); // Cylinder Throw
	private static final SkillHolder SelfRangeBuff = new SkillHolder(6294, 1); // Leader's Roar
	private static final SkillHolder LEADER_RUSH = new SkillHolder(6296, 1); // Rush
	private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1); // Hinder Strider
	private static final SkillHolder ICE_BALL = new SkillHolder(6278, 1); // Ice Ball
	private static final SkillHolder SUMMON_ELEMENTAL = new SkillHolder(6277, 1); // Summon Spirits
	private static final SkillHolder SELF_NOVA = new SkillHolder(6279, 1); // Attack Nearby Range
	private static final SkillHolder REFLECT_MAGIC = new SkillHolder(6282, 1); // Reflect Magic

	// Locations
	private static final Location FREYA_SPAWN = new Location(114720, -117085, -11088, 15956);
	private static final Location FREYA_SPELLING_SPAWN = new Location(114723, -117502, -10672, 15956);
	private static final Location MIDDLE_POINT = new Location(114730, -114805, -11200);
	private static final Location GLAKIAS_SPAWN = new Location(114707, -114799, -11199, 15956);
	private static final Location SUPP_JINIA_SPAWN = new Location(114751, -114781, -11205);
	private static final Location SUPP_KEGOR_SPAWN = new Location(114659, -114796, -11205);
	private static final Location BATTLE_PORT = new Location(114694, -113700, -11200);
	private static final Location CONTROLLER_LOC = new Location(114394, -112383, -11200);
	private static final Location[] STATUES_LOC =
	{
		new Location(113845, -116091, -11168, 8264),
		new Location(113381, -115622, -11168, 8264),
		new Location(113380, -113978, -11168, -8224),
		new Location(113845, -113518, -11168, -8224),
		new Location(115591, -113516, -11168, -24504),
		new Location(116053, -113981, -11168, -24504),
		new Location(116061, -115611, -11168, 24804),
		new Location(115597, -116080, -11168, 24804),
		new Location(112942, -115480, -10960, 52),
		new Location(112940, -115146, -10960, 52),
		new Location(112945, -114453, -10960, 52),
		new Location(112945, -114123, -10960, 52),
		new Location(116497, -114117, -10960, 32724),
		new Location(116499, -114454, -10960, 32724),
		new Location(116501, -115145, -10960, 32724),
		new Location(116502, -115473, -10960, 32724),
	};
	private static Location[] KNIGHTS_LOC =
	{
		new Location(114502, -115315, -11205, 15451),
		new Location(114937, -115323, -11205, 18106),
		new Location(114722, -115185, -11205, 16437),
	};

	// For Effect Castle Destroy
	private static int[] EMMITERS =
	{
		23140202,
		23140204,
		23140206,
		23140208,
		23140212,
		23140214,
		23140216
	};
	private static int STAT_CHANGE = 0;
	
	// Misc
	private static final int MAX_PLAYERS = 27;
	private static final int MIN_PLAYERS = 10;
	private static final int MIN_LEVEL = 82;
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int RESET_DAY_1 = 4; // Wednesday
	private static final int RESET_DAY_2 = 7; // Saturday
	private static final int INSTANCEID = 144; // Ice Queen's Castle
	private static final int DOOR_ID = 23140101;

	private IceQueenCastleUltimateBattle(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(SIRRA, SUPP_KEGOR, SUPP_JINIA);
		addTalkId(SIRRA, JINIA);
		addAttackId(FREYA_THRONE, FREYA_STAND, GLAKIAS, GLACIER, BREATH, KNIGHT);
		addKillId(GLAKIAS, FREYA_STAND, KNIGHT, GLACIER, BREATH);
		addSpawnId(GLAKIAS, FREYA_STAND, KNIGHT, GLACIER, BREATH);
		addSpellFinishedId(GLACIER, BREATH);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equals("enter"))
			enterInstance(player, "IceQueenCastleUltimateBattle.xml");
		else
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

			if (tmpworld != null && tmpworld instanceof IQCUBWorld)
			{
				final IQCUBWorld world = (IQCUBWorld) tmpworld;
				switch (event)
				{
					case "openDoor":
					{
						if (npc.isScriptValue(0))
						{
							npc.setScriptValue(1);
							openDoor(DOOR_ID, world.instanceId);
							world.controller = (L2NpcInstance) addSpawn(INVISIBLE_NPC, CONTROLLER_LOC, false, 0, true, world.instanceId);
							for (final Location loc : STATUES_LOC)
								if (loc.getZ() == -11168)
								{
									final L2Npc statue = addSpawn(INVISIBLE_NPC, loc, false, 0, false, world.instanceId);
									world.knightStatues.add(statue);
								}
							startQuestTimer("stage_1_movie", 60000, world.controller, null);
						}
						break;
					}
					case "portInside":
					{
						teleportPlayer(player, BATTLE_PORT, world.instanceId);
						break;
					}
					case "stage_1_movie":
					{
						closeDoor(DOOR_ID, world.instanceId);
						world.status = 1;
						manageMovie(world, 15);
						startQuestTimer("stage_1_start", 53500, world.controller, null);
						break;
					}
					case "stage_1_start":
					{
						world.freya = (L2GrandBossInstance) addSpawn(FREYA_THRONE, FREYA_SPAWN, false, 0, true, world.instanceId);
						world.freya.setIsMortal(false);
						exShowStageMsg(world, 1801086); // Begin stage 1!
						startQuestTimer("cast_blizzard", 50000, world.controller, null);
						startQuestTimer("stage_1_spawn", 2000, world.freya, null);
						break;
					}
					case "stage_1_spawn":
					{
						notifyEvent("start_spawn", world.controller, null);
						break;
					}
					case "stage_1_finish":
					{
						world.freya.deleteMe();
						world.freya = null;
						manageDespawnMinions(world);
						manageMovie(world, 16);
						startQuestTimer("stage_1_pause", 24100 - 1000, world.controller, null);
						break;
					}
					case "stage_1_pause":
					{
						world.freya = (L2GrandBossInstance) addSpawn(FREYA_SPELLING, FREYA_SPELLING_SPAWN, false, 0, true, world.instanceId);
						world.freya.setIsInvul(true);
						world.freya.disableCoreAI(true);
						manageTimer(world, 60);
						world.status = 2;
						startQuestTimer("stage_2_start", 60000, world.controller, null);
						break;
					}
					case "stage_2_start":
					{
						world.canSpawnMobs = true;
						notifyEvent("start_spawn", world.controller, null);
						exShowStageMsg(world, 1801087); // Begin stage 2!
						break;
					}
					case "stage_2_movie":
					{
						manageMovie(world, 23);
						startQuestTimer("stage_2_glakias", 7000, world.controller, null);
						break;
					}
					case "stage_2_glakias":
					{
						for (final Location loc : STATUES_LOC)
							if (loc.getZ() == -10960)
							{
								final L2Npc statue = addSpawn(INVISIBLE_NPC, loc, false, 0, false, world.instanceId);
								world.knightStatues.add(statue);
								startQuestTimer("spawn_knight", 5000, statue, null);
							}
						final L2RaidBossInstance glakias = (L2RaidBossInstance) addSpawn(GLAKIAS, GLAKIAS_SPAWN, false, 0, true, world.instanceId);
						startQuestTimer("leader_delay", 5000, glakias, null);
						break;
					}
					case "stage_3_movie":
					{
						world.freya.deleteMe();
						manageMovie(world, 17);
						startQuestTimer("static_destruction", 21500, world.controller, null);
						break;
					}
					case "static_destruction":
					{
						final ExChangeAreaState as = new ExChangeAreaState(STAT_CHANGE, 2);
						Scenkos.toPlayersInInstance(as, world.instanceId);
						for (final int emitter : EMMITERS)
						{
							final OnEventTrigger et = new OnEventTrigger(emitter, false);
							Scenkos.toPlayersInInstance(et, world.instanceId);
						}
						startQuestTimer("freya_awakening", 1000, world.controller, null);
						break;
					}
					case "freya_awakening":
					{
						manageMovie(world, 22);
						startQuestTimer("stage_3_start", 20500, world.controller, null);
						break;
					}
					case "stage_3_start":
					{
						world.status = 4;
						world.canSpawnMobs = true;
						world.freya = (L2GrandBossInstance) addSpawn(FREYA_STAND, FREYA_SPAWN, false, 0, true, world.instanceId);
						world.controller.getVariables().set("freya_move", 0);
						notifyEvent("start_spawn", world.controller, null);
						startQuestTimer("start_move", 10000, world.controller, null);
						startQuestTimer("cast_blizzard", 50000, world.controller, null);
						exShowStageMsg(world, 1801088); // Begin stage 3!
						break;
					}
					case "start_move":
					{
						if (npc.getVariables().getInt("freya_move") == 0)
						{
							world.controller.getVariables().set("freya_move", 1);
							world.freya.setIsRunning(true);
							world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
						}
						break;
					}
					case "cast_blizzard":
					{
						if (!world.freya.isInvul())
						{
							world.freya.doCast(BLIZZARD.getSkill());
							manageScreenMsg(world, 1801111); // Strong magic power can be felt from somewhere!!!
						}

						for (final L2Attackable minion : world.spawnedMobs)
							if (minion != null && !minion.isDead() && !minion.isInCombat())
								manageRandomAttack(world, minion);
						startQuestTimer("cast_blizzard", getRandom(55, 60) * 1000, world.controller, null);
						break;
					}
					case "spawn_support":
					{
						for (final int objId : world.allowed)
						{
							final L2PcInstance players = L2World.getInstance().getPlayer(objId);
							if (players != null && players.getInstanceId() == world.instanceId)
								players.setIsInvul(false);
						}
						world.freya.setIsInvul(false);
						world.freya.disableCoreAI(false);
						exShowStageMsg(world, 1801089); // Begin stage 4!
						notifyEvent("start_spawn", world.controller, null);
						world.supp_Jinia = (L2QuestGuardInstance) addSpawn(SUPP_JINIA, SUPP_JINIA_SPAWN, false, 0, true, world.instanceId);
						world.supp_Jinia.setIsRunning(true);
						world.supp_Jinia.setIsInvul(true);
						world.supp_Jinia.setCanReturnToSpawnPoint(false);
						world.supp_Kegor = (L2QuestGuardInstance) addSpawn(SUPP_KEGOR, SUPP_KEGOR_SPAWN, false, 0, true, world.instanceId);
						world.supp_Kegor.setIsRunning(true);
						world.supp_Kegor.setIsInvul(true);
						world.supp_Kegor.setCanReturnToSpawnPoint(false);
						startQuestTimer("attack_freya", 5000, world.supp_Jinia, null);
						startQuestTimer("attack_freya", 5000, world.supp_Kegor, null);
						startQuestTimer("give_support", 1000, world.controller, null);
						break;
					}
					case "give_support":
					{
						if (world.isSupportActive)
						{
							world.supp_Jinia.doCast(JINIA_SUPPORT.getSkill());
							world.supp_Kegor.doCast(KEGOR_SUPPORT.getSkill());
							startQuestTimer("give_support", 25000, world.controller, null);
						}
						break;
					}
					case "finish_stage":
					{
						world.supp_Jinia.deleteMe();
						world.supp_Jinia = null;
						world.supp_Kegor.deleteMe();
						world.supp_Kegor = null;
						break;
					}
					case "start_spawn":
					{
						for (final L2Npc statues : world.knightStatues)
							notifyEvent("spawn_knight", statues, null);

						for (final Location loc : KNIGHTS_LOC)
						{
							final L2Attackable knight = (L2Attackable) addSpawn(KNIGHT, loc, false, 0, false, world.instanceId);
							knight.disableCoreAI(true);
							knight.setDisplayEffect(1);
							knight.getSpawn().setLocation(loc);
							world.spawnedMobs.add(knight);
							startQuestTimer("ice_rupture", getRandom(2, 5) * 1000, knight, null);
						}

						for (int i = 0; i < world.status; i++)
							notifyEvent("spawn_glacier", world.controller, null);
						break;
					}
					case "spawn_knight":
					{
						if (world.canSpawnMobs)
						{
							final Location loc = new Location(MIDDLE_POINT.getX() + getRandom(-1000, 1000), MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ());
							final L2Attackable knight = (L2Attackable) addSpawn(KNIGHT, npc.getLocation(), false, 0, false, world.instanceId);
							knight.getVariables().set("spawned_npc", npc);
							knight.disableCoreAI(true);
							knight.setIsImmobilized(true);
							knight.setDisplayEffect(1);
							knight.getSpawn().setLocation(loc);
							world.spawnedMobs.add(knight);
							startQuestTimer("ice_rupture", getRandom(5, 10) * 1000, knight, null);
						}
						break;
					}
					case "spawn_glacier":
					{
						if (world.canSpawnMobs)
						{
							final Location loc = new Location(MIDDLE_POINT.getX() + getRandom(-1000, 1000), MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ());
							final L2Attackable glacier = (L2Attackable) addSpawn(GLACIER, loc, false, 0, false, world.instanceId);
							glacier.setDisplayEffect(1);
							glacier.disableCoreAI(true);
							glacier.setIsImmobilized(true);
							world.spawnedMobs.add(glacier);
							startQuestTimer("change_state", 1400, glacier, null);
						}
						break;
					}
					case "ice_rupture":
					{
						if (npc.isCoreAIDisabled())
						{
							npc.disableCoreAI(false);
							npc.setIsImmobilized(false);
							npc.setDisplayEffect(2);
							manageRandomAttack(world, (L2Attackable) npc);
						}
						break;
					}
					case "find_target":
					{
						manageRandomAttack(world, (L2Attackable) npc);
						break;
					}
					case "change_state":
					{
						npc.setDisplayEffect(2);
						startQuestTimer("cast_skill", 20000, npc, null);
						break;
					}
					case "cast_skill":
					{
						if (npc.isScriptValue(0) && !npc.isDead())
						{
							npc.setTarget(npc);
							npc.doCast(ICE_STONE.getSkill());
							npc.setScriptValue(1);
						}
						break;
					}
					case "suicide":
					{
						npc.setDisplayEffect(3);
						npc.setIsMortal(true);
						npc.doDie(null);
						break;
					}
					case "blizzard":
					{
						npc.getVariables().set("suicide_count", npc.getVariables().getInt("suicide_count") + 1);

						if (npc.getVariables().getInt("suicide_on") == 0)
							if (npc.getVariables().getInt("suicide_count") == 2)
								startQuestTimer("elemental_suicide", 20000, npc, null);
							else
							{
								if (npc.checkDoCastConditions(BLIZZARD_BREATH.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(npc);
									npc.doCast(BLIZZARD_BREATH.getSkill());
								}
								startQuestTimer("blizzard", 20000, npc, null);
							}
						break;
					}
					case "elemental_suicide":
					{
						npc.setTarget(npc);
						npc.doCast(SUICIDE_BREATH.getSkill());
						break;
					}
					case "elemental_killed":
					{
						if (npc.getVariables().getInt("suicide_on") == 1)
						{
							npc.setTarget(npc);
							npc.doCast(SUICIDE_BREATH.getSkill());
						}
						break;
					}
					case "attack_freya":
					{
						if (npc.isInsideRadius(world.freya, 100, true, false))
						{
							startQuestTimer("attack_freya", 5000, npc, null);
						}
						else
						{
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, world.freya);
							startQuestTimer("attack_freya", 5000, npc, null);
						}
						break;
					}
					case "finish_world":
					{
						final ExChangeAreaState as = new ExChangeAreaState(STAT_CHANGE, 1);
						Scenkos.toPlayersInInstance(as, world.instanceId);
						for (final int emitter : EMMITERS)
						{
							final OnEventTrigger et = new OnEventTrigger(emitter, false);
							Scenkos.toPlayersInInstance(et, world.instanceId);
						}

						InstanceManager.getInstance().destroyInstance(world.instanceId);
						break;
					}
					case "leader_rangebuff":
					{
						if (npc.checkDoCastConditions(SelfRangeBuff.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(npc);
							npc.doCast(SelfRangeBuff.getSkill());
						}
						else
							startQuestTimer("leader_rangebuff", 30000, npc, null);
						break;
					}
					case "leader_randomize":
					{
						final L2Attackable mob = (L2Attackable) npc;
						mob.clearAggroList();

						for (final L2Character characters : npc.getKnownList().getKnownPlayersInRadius(1000))
							if (characters != null)
								mob.addDamageHate(characters, 0, getRandom(10000, 20000));
						startQuestTimer("leader_randomize", 25000, npc, null);
						break;
					}
					case "leader_dash":
					{
						final L2Character mostHated = ((L2Attackable) npc).getMostHated();
						if (getRandomBoolean() && !npc.isCastingNow() && mostHated != null && !mostHated.isDead() && npc.calculateDistance(mostHated, true, false) < 1000)
						{
							npc.setTarget(mostHated);
							npc.doCast(LEADER_RUSH.getSkill());
						}
						startQuestTimer("leader_dash", 10000, npc, null);
						break;
					}
					case "leader_destroy":
					{
						final L2Attackable mob = (L2Attackable) npc;
						if (npc.getVariables().getInt("off_shout") == 0)
							switch (getRandom(4))
							{
								case 0:
								{
									npc.broadcastNpcSay(Say2.SHOUT, "Archer. Give your breath for the intruder!");
									break;
								}
								case 1:
								{
									npc.broadcastNpcSay(Say2.SHOUT, "My knights. Show your loyalty!!");
									break;
								}
								case 2:
								{
									npc.broadcastNpcSay(Say2.SHOUT, "I can take it no longer!!!");
									break;
								}
								case 3:
								{
									npc.broadcastNpcSay(Say2.SHOUT, "Archer. Heed my call!");
									for (int i = 0; i < 3; i++)
									{
										final L2Attackable breath = (L2Attackable) addSpawn(BREATH, npc.getLocation(), true, 0, false, world.instanceId);
										breath.setIsRunning(true);
										breath.addDamageHate(mob.getMostHated(), 0, 999);
										breath.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, mob.getMostHated());
										startQuestTimer("blizzard", 20000, breath, null);
										world.spawnedMobs.add(breath);
									}
									break;
								}
							}
						break;
					}
					case "leader_delay":
					{
						if (npc.getVariables().getInt("delay_val") == 0)
							npc.getVariables().set("delay_val", 1);
						break;
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		((L2Attackable) npc).setOnKillDelay(0);
		return super.onSpawn(npc);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if (tmpworld != null && tmpworld instanceof IQCUBWorld)
		{
			final IQCUBWorld world = (IQCUBWorld) tmpworld;
			switch (npc.getId())
			{
				case FREYA_THRONE:
				{
					if (world.controller.getVariables().getInt("freya_move") == 0 && world.status == 1)
					{
						world.controller.getVariables().set("freya_move", 1);
						manageScreenMsg(world, 1801097); // Freya has started to move.
						world.freya.setIsRunning(true);
						world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					}

					if (npc.getCurrentHp() < npc.getMaxHp() * 0.02)
					{
						notifyEvent("stage_1_finish", world.controller, null);
						cancelQuestTimer("cast_blizzard", world.controller, null);
					}
					else
					{
						if (attacker.getMountType() == 1 && !attacker.isAffected(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow())
							if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
							{
								npc.setTarget(attacker);
								npc.doCast(ANTI_STRIDER.getSkill());
							}

						final L2Character mostHated = ((L2Attackable) npc).getMostHated();
						final boolean canReachMostHated = mostHated != null && !mostHated.isDead() && npc.calculateDistance(mostHated, true, false) <= 800;

						if (getRandom(10000) < 3333)
						{
							if (getRandomBoolean())
							{
								if (npc.calculateDistance(attacker, true, false) <= 800 && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(ICE_BALL.getSkill());
								}
							}
							else if (canReachMostHated && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(mostHated);
								npc.doCast(ICE_BALL.getSkill());
							}
						}
						else if (getRandom(10000) < 800)
						{
							if (getRandomBoolean())
							{
								if (npc.calculateDistance(attacker, true, false) <= 800 && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(SUMMON_ELEMENTAL.getSkill());
								}
							}
							else if (canReachMostHated && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(mostHated);
								npc.doCast(SUMMON_ELEMENTAL.getSkill());
							}
						}
						else if (getRandom(10000) < 1500)
							if (!npc.isAffected(SELF_NOVA.getSkillId()) && npc.checkDoCastConditions(SELF_NOVA.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(npc);
								npc.doCast(SELF_NOVA.getSkill());
							}
					}
					break;
				}
				case FREYA_STAND:
				{
					if (world.controller.getVariables().getInt("freya_move") == 0)
					{
						world.controller.getVariables().set("freya_move", 1);
						world.freya.setIsRunning(true);
						world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					}

					if (npc.getCurrentHp() < npc.getMaxHp() * 0.2 && !world.isSupportActive)
					{
						world.isSupportActive = true;
						world.freya.setIsInvul(true);
						world.freya.disableCoreAI(true);
						world.freya.setTarget(null);
						manageDespawnMinions(world);
						for (final int objId : world.allowed)
						{
							final L2PcInstance players = L2World.getInstance().getPlayer(objId);
							players.setIsInvul(true);
							players.setTarget(null);
							players.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						}
						manageMovie(world, 18);
						startQuestTimer("spawn_support", 27000, world.controller, null);
					}
					
					if (attacker.getMountType() == 1 && !attacker.isAffected(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow())
						if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
						{
							npc.setTarget(attacker);
							npc.doCast(ANTI_STRIDER.getSkill());
						}

					final L2Character mostHated = ((L2Attackable) npc).getMostHated();
					final boolean canReachMostHated = mostHated != null && !mostHated.isDead() && npc.calculateDistance(mostHated, true, false) <= 800;

					if (getRandom(10000) < 3333)
					{
						if (getRandomBoolean())
						{
							if (npc.calculateDistance(attacker, true, false) <= 800 && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(attacker);
								npc.doCast(ICE_BALL.getSkill());
							}
						}
						else if (canReachMostHated && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(mostHated);
							npc.doCast(ICE_BALL.getSkill());
						}
					}
					else if (getRandom(10000) < 1333)
					{
						if (getRandomBoolean())
						{
							if (npc.calculateDistance(attacker, true, false) <= 800 && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
							{
								npc.setTarget(attacker);
								npc.doCast(SUMMON_ELEMENTAL.getSkill());
							}
						}
						else if (canReachMostHated && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(mostHated);
							npc.doCast(SUMMON_ELEMENTAL.getSkill());
						}
					}
					else if (getRandom(10000) < 1500)
					{
						if (!npc.isAffected(SELF_NOVA.getSkillId()) && npc.checkDoCastConditions(SELF_NOVA.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(npc);
							npc.doCast(SELF_NOVA.getSkill());
						}
					}
					else if (getRandom(10000) < 1333)
						if (!npc.isAffected(REFLECT_MAGIC.getSkillId()) && npc.checkDoCastConditions(REFLECT_MAGIC.getSkill()) && !npc.isCastingNow())
						{
							npc.setTarget(npc);
							npc.doCast(REFLECT_MAGIC.getSkill());
						}
					break;
				}
				case GLACIER:
				{
					if (npc.isScriptValue(0) && npc.getCurrentHp() < npc.getMaxHp() * 0.5)
					{
						npc.setTarget(attacker);
						npc.doCast(ICE_STONE.getSkill());
						npc.setScriptValue(1);
					}
					break;
				}
				case BREATH:
				{
					if (npc.getCurrentHp() < npc.getMaxHp() / 20 && npc.getVariables().getInt("suicide_on", 0) == 0)
					{
						npc.getVariables().set("suicide_on", 1);
						startQuestTimer("elemental_killed", 1000, npc, null);
					}
					break;
				}
				case KNIGHT:
				{
					if (npc.isCoreAIDisabled())
					{
						manageRandomAttack(world, (L2Attackable) npc);
						npc.disableCoreAI(false);
						npc.setIsImmobilized(false);
						npc.setDisplayEffect(2);
						cancelQuestTimer("ice_rupture", npc, null);
					}
					break;
				}
				case GLAKIAS:
				{
					if (npc.getCurrentHp() < npc.getMaxHp() * 0.02)
					{
						if (npc.getVariables().getInt("off_shout") == 0)
						{
							npc.getVariables().set("off_shout", 1);
							npc.getVariables().set("delay_val", 2);
							npc.setTarget(attacker);
							npc.doCast(CANCEL.getSkill());
						}
						else if (npc.getVariables().getInt("off_shout") == 1)
						{
							npc.setTarget(attacker);
							npc.doCast(CANCEL.getSkill());
						}
					}
					else if (npc.getVariables().getInt("off_shout") == 0 && npc.getVariables().getInt("delay_val") == 1)
					{
						final L2Character mostHated = ((L2Attackable) npc).getMostHated();
						final boolean canReachMostHated = mostHated != null && !mostHated.isDead() && npc.calculateDistance(mostHated, true, false) < 1000;

						if (npc.getVariables().getInt("timer_on") == 0)
						{
							npc.getVariables().set("timer_on", 1);
							startQuestTimer("leader_rangebuff", getRandom(5, 30) * 1000, npc, null);
							startQuestTimer("leader_randomize", 25000, npc, null);
							startQuestTimer("leader_dash", 5000, npc, null);
							startQuestTimer("leader_destroy", 60000, npc, null);
						}

						if (getRandom(10000) < 2500)
						{
							if (getRandom(10000) < 2500)
							{
								if (npc.checkDoCastConditions(POWER_STRIKE.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(POWER_STRIKE.getSkill());
								}
							}
							else if (npc.checkDoCastConditions(POWER_STRIKE.getSkill()) && !npc.isCastingNow() && canReachMostHated)
							{
								npc.setTarget(((L2Attackable) npc).getMostHated());
								npc.doCast(POWER_STRIKE.getSkill());
							}
						}
						else if (getRandom(10000) < 1500)
						{
							if (getRandomBoolean())
							{
								if (npc.checkDoCastConditions(POINT_TARGET.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(POINT_TARGET.getSkill());
								}
							}
							else if (npc.checkDoCastConditions(POINT_TARGET.getSkill()) && !npc.isCastingNow() && canReachMostHated)
							{
								npc.setTarget(((L2Attackable) npc).getMostHated());
								npc.doCast(POINT_TARGET.getSkill());
							}
						}
						else if (getRandom(10000) < 1500)
							if (getRandomBoolean())
							{
								if (npc.checkDoCastConditions(CYLINDER_THROW.getSkill()) && !npc.isCastingNow())
								{
									npc.setTarget(attacker);
									npc.doCast(CYLINDER_THROW.getSkill());
								}
							}
							else if (npc.checkDoCastConditions(CYLINDER_THROW.getSkill()) && !npc.isCastingNow() && canReachMostHated)
							{
								npc.setTarget(((L2Attackable) npc).getMostHated());
								npc.doCast(CYLINDER_THROW.getSkill());
							}
					}
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onSpellFinished(final L2Npc npc, final L2PcInstance player, final L2Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if (tmpworld != null && tmpworld instanceof IQCUBWorld)
		{
			final IQCUBWorld world = (IQCUBWorld) tmpworld;

			switch (npc.getId())
			{
				case GLACIER:
				{
					if (skill == ICE_STONE.getSkill())
					{
						if (getRandom(100) < 75)
						{
							final L2Attackable breath = (L2Attackable) addSpawn(BREATH, npc.getLocation(), false, 0, false, world.instanceId);
							if (player != null)
							{
								breath.setIsRunning(true);
								breath.addDamageHate(player, 0, 999);
								breath.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
							}
							else
								manageRandomAttack(world, breath);
							world.spawnedMobs.add(breath);
							startQuestTimer("blizzard", 20000, breath, null);
						}
						notifyEvent("suicide", npc, null);
					}
					break;
				}
				case BREATH:
				{
					if (skill == SUICIDE_BREATH.getSkill())
						npc.doDie(null);
					break;
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if (tmpworld != null && tmpworld instanceof IQCUBWorld)
		{
			final IQCUBWorld world = (IQCUBWorld) tmpworld;
			switch (npc.getId())
			{
				case GLAKIAS:
				{
					manageDespawnMinions(world);
					manageTimer(world, 60);
					startQuestTimer("stage_3_movie", 60000, world.controller, null);
					break;
				}
				case FREYA_STAND:
				{
					for (final int objId : world.allowed)
					{
						final L2PcInstance player = L2World.getInstance().getPlayer(objId);
						if (player != null && player.getInstanceId() == world.instanceId)
						{
							final Calendar reenter = Calendar.getInstance();
							Calendar.getInstance().set(Calendar.MINUTE, RESET_MIN);
							Calendar.getInstance().set(Calendar.HOUR_OF_DAY, RESET_HOUR);

							if (reenter.getTimeInMillis() <= System.currentTimeMillis())
								reenter.add(Calendar.DAY_OF_MONTH, 1);
							if (reenter.get(Calendar.DAY_OF_WEEK) <= RESET_DAY_1)
								while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_1)
									reenter.add(Calendar.DAY_OF_MONTH, 1);
							else
								while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_2)
									reenter.add(Calendar.DAY_OF_MONTH, 1);
							InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID, reenter.getTimeInMillis());
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
							sm.addInstanceName(INSTANCEID);
							player.sendPacket(sm);
						}
					}
					world.isSupportActive = false;
					manageMovie(world, 19);
					manageDespawnMinions(world);
					cancelQuestTimer("attack_freya", world.supp_Jinia, null);
					cancelQuestTimer("attack_freya", world.supp_Kegor, null);
					cancelQuestTimer("give_support", world.controller, null);
					cancelQuestTimer("cast_blizzard", world.controller, null);
					startQuestTimer("finish_stage", 16000, world.controller, null);
					startQuestTimer("finish_world", 48000, world.controller, null);
					break;
				}
				case KNIGHT:
				{
					final L2Npc spawnedBy = npc.getVariables().getObject("spawned_npc", L2Npc.class);
					final NpcVariables var = world.controller.getVariables();
					int knightCount = var.getInt("knight_count");

					if (var.getInt("freya_move") == 0 && world.status == 1)
					{
						var.set("freya_move", 1);
						manageScreenMsg(world, 1801097); // Freya has started to move.

						world.freya.setIsRunning(true);
						world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					}

					if (knightCount < 10 && world.status == 2)
					{
						knightCount++;
						var.set("knight_count", knightCount);

						if (knightCount == 10)
						{
							notifyEvent("stage_2_movie", world.controller, null);
							world.status = 3;
						}
					}

					if (spawnedBy != null)
						startQuestTimer("spawn_knight", getRandom(30, 60) * 1000, spawnedBy, null);
					world.spawnedMobs.remove(npc);
					break;
				}
				case GLACIER:
				{
					startQuestTimer("spawn_glacier", getRandom(30, 60) * 1000, world.controller, null);
					world.spawnedMobs.remove(npc);
					break;
				}
				case BREATH:
				{
					world.spawnedMobs.remove(npc);
					break;
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	private void enterInstance(final L2PcInstance player, final String template)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof IQCUBWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player, (IQCUBWorld) world);
			return;
		}
		// New instance
		if (!checkConditions(player))
			return;
		final L2Party party = player.getParty();
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new IQCUBWorld();
		
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		
		InstanceManager.getInstance().addWorld(world);
		_log.info("IceQueenCastleUltimateBattle started " + template + " Instance: " + instanceId + " created by player: " + player.getName());

		if (debug)
		{
			world.allowed.add(player.getObjectId());
			teleportPlayer(player, (IQCUBWorld) world);
			return;
		}

		if (party != null && party.isInCommandChannel())
		{
			for (final L2PcInstance plr : party.getCommandChannel().getMembers())
			{
				world.allowed.add(plr.getObjectId());
				teleportPlayer(plr, (IQCUBWorld) world);
			}
			return;
		}
	}
	
	private boolean checkConditions(final L2PcInstance player)
	{
		if (debug || player.isGM())
			return true;
		
		if (player.getParty() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		
		if (player.getParty().getCommandChannel() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		
		if (player.getObjectId() != player.getParty().getCommandChannel().getChannelLeader().getObjectId())
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		
		if (player.getParty().getCommandChannel().getMemberCount() < MIN_PLAYERS)
		{
			player.getParty().getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(2793).addNumber(10));
			return false;
		}
		
		if (player.getParty().getCommandChannel().getMemberCount() > MAX_PLAYERS)
		{
			player.getParty().getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(2102));
			return false;
		}
		
		for (final L2PcInstance partyMember : player.getParty().getCommandChannel().getMembers())
		{
			if (partyMember.getLevel() < MIN_LEVEL)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(2097);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
				return false;
			}
			
			final QuestState _prev = player.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
			if (_prev != null && _prev.getState() == State.COMPLETED)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
				return false;
			}
			
			final Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastToChannelMembers(sm);
				return false;
			}
		}
		return true;
	}
	
	private void teleportPlayer(final L2PcInstance player, final IQCUBWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(world.instanceId);
		player.teleToLocation(113991, -112297, -11200);
		if (player.getPet() != null)
		{
			player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getPet().setInstanceId(world.instanceId);
			player.getPet().teleToLocation(113991, -112297, -11200);
		}
		return;
	}

	private void manageRandomAttack(final IQCUBWorld world, final L2Attackable mob)
	{
		final List<L2PcInstance> players = new ArrayList<>();
		for (final int objId : world.allowed)
		{
			final L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if (player != null && !player.isDead() && player.getInstanceId() == world.instanceId)
				players.add(player);
		}

		Collections.shuffle(players);
		final L2PcInstance target = !players.isEmpty() ? players.get(0) : null;
		if (target != null)
		{
			mob.addDamageHate(target, 0, 999);
			mob.setIsRunning(true);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		else
			startQuestTimer("find_target", 10000, mob, null);
	}

	private void manageDespawnMinions(final IQCUBWorld world)
	{
		world.canSpawnMobs = false;
		for (final L2Attackable mobs : world.spawnedMobs)
			if (mobs != null && !mobs.isDead())
				mobs.doDie(null);
	}

	private void manageTimer(final IQCUBWorld world, final int time)
	{
		for (final int objId : world.allowed)
		{
			final L2PcInstance players = L2World.getInstance().getPlayer(objId);
			{
				players.sendPacket(new ExSendUIEvent(players, false, false, time, 0, "Time remaining until next battle"));
			}
		}
	}

	private void manageScreenMsg(final IQCUBWorld world, final int stringMsg)
	{
		for (final int objId : world.allowed)
		{
			final L2PcInstance players = L2World.getInstance().getPlayer(objId);
			{
				players.sendPacket(new ExShowBroadcastMessage(stringMsg, 3000, ExShowBroadcastMessage.ScreenMessageAlign.MIDDLE_CENTER, true, false, -1, true));
			}
		}
	}

	private void exShowStageMsg(final IQCUBWorld world, final int stringMsg)
	{
		for (final int objId : world.allowed)
		{
			final L2PcInstance players = L2World.getInstance().getPlayer(objId);
			{
				players.sendPacket(new ExShowBroadcastMessage(stringMsg, 3000, ExShowBroadcastMessage.ScreenMessageAlign.TOP_CENTER, true, false, -1, true));
			}
		}
	}

	private void manageMovie(final IQCUBWorld world, final int movie)
	{
		for (final int objId : world.allowed)
		{
			final L2PcInstance players = L2World.getInstance().getPlayer(objId);
			{
				players.showQuestMovie(movie);
			}
		}
	}

	public static void main(final String[] args)
	{
		new IceQueenCastleUltimateBattle(-1, IceQueenCastleUltimateBattle.class.getSimpleName(), "instances");
	}
}