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
package instances.HallOfSuffering;

import java.util.Calendar;
import java.util.Map;

import ct25.xtreme.gameserver.ai.CtrlEvent;
import ct25.xtreme.gameserver.cache.HtmCache;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.templates.skills.L2SkillType;
import ct25.xtreme.gameserver.util.Util;
import javolution.util.FastMap;
import quests.Q00694_BreakThroughTheHallOfSuffering.Q00694_BreakThroughTheHallOfSuffering;

/*
//TODO:
- after 15mins mobs are despawned
- bound instance to quests
Test depply increased points for next stage in Gracia Seeds Manager
Break Through The Hall of Suffering Quest implemented in Hall script
Contributing authors: Browser, Gigiikun, ZakaX, Didldak
Please maintain consistency between the Seed scripts.
 */
public class HallOfSuffering extends Quest
{

	private class HSWorld extends InstanceWorld
	{
		public Map<L2Npc, Boolean> npcList = new FastMap<>();
		public L2Npc klodekus = null;
		public L2Npc klanikus = null;
		public boolean isBossesAttacked = false;
		public long startTime = 0;
		public String ptLeaderName = "";
		public int rewardItemId = -1;
		public boolean rewardMark = false;
		public String rewardHtm = "";
		public boolean isRewarded = false;

		public HSWorld()
		{
		}
	}

	// Misc
	private static final String qn = "HallOfSuffering";
	private static final int INSTANCEID = 115; // this is the client number
	private static final boolean debug = false;

	// NPCs
	private static final int MOUTHOFEKIMUS = 32537;
	private static final int TEPIOS = 32530;

	// Teleports
	private static final int[] ENTER_TELEPORT =
	{
		-187567,
		205570,
		-9538
	};

	// Mobs
	private static final int KLODEKUS = 25665;
	private static final int KLANIKUS = 25666;
	private static final int TUMOR_ALIVE = 18704;
	private static final int TUMOR_DEAD = 18705;
	private static final int[] TUMOR_MOBIDS =
	{
		22509,
		22510,
		22511,
		22512,
		22513,
		22514,
		22515
	};
	private static final int[] TWIN_MOBIDS =
	{
		22509,
		22510,
		22511,
		22512,
		22513
	};

	// Items
	private static final int MARK_STAGE_1 = 13691;

	// Doors/Walls/Zones
	private static final int[][] ROOM_1_MOBS =
	{
		{
			22509,
			-186296,
			208200,
			-9544
		},
		{
			22509,
			-186161,
			208345,
			-9544
		},
		{
			22509,
			-186296,
			208403,
			-9544
		},
		{
			22510,
			-186107,
			208113,
			-9528
		},
		{
			22510,
			-186350,
			208200,
			-9544
		}
	};
	private static final int[][] ROOM_2_MOBS =
	{
		{
			22511,
			-184433,
			210953,
			-9536
		},
		{
			22511,
			-184406,
			211301,
			-9536
		},
		{
			22509,
			-184541,
			211272,
			-9544
		},
		{
			22510,
			-184244,
			211098,
			-9536
		},
		{
			22510,
			-184352,
			211243,
			-9536
		},
		{
			22510,
			-184298,
			211330,
			-9528
		}
	};
	private static final int[][] ROOM_3_MOBS =
	{
		{
			22512,
			-182611,
			213984,
			-9520
		},
		{
			22512,
			-182908,
			214071,
			-9520
		},
		{
			22512,
			-182962,
			213868,
			-9512
		},
		{
			22509,
			-182881,
			213955,
			-9512
		},
		{
			22511,
			-182827,
			213781,
			-9504
		},
		{
			22511,
			-182530,
			213984,
			-9528
		},
		{
			22510,
			-182935,
			213723,
			-9512
		},
		{
			22510,
			-182557,
			213868,
			-9520
		}
	};
	private static final int[][] ROOM_4_MOBS =
	{
		{
			22514,
			-180958,
			216860,
			-9544
		},
		{
			22514,
			-181012,
			216628,
			-9536
		},
		{
			22514,
			-181120,
			216715,
			-9536
		},
		{
			22513,
			-180661,
			216599,
			-9536
		},
		{
			22513,
			-181039,
			216599,
			-9536
		},
		{
			22511,
			-180715,
			216599,
			-9536
		},
		{
			22511,
			-181012,
			216889,
			-9536
		},
		{
			22512,
			-180931,
			216918,
			-9536
		},
		{
			22512,
			-180742,
			216628,
			-9536
		}
	};
	private static final int[][] ROOM_5_MOBS =
	{
		{
			22512,
			-177372,
			217854,
			-9536
		},
		{
			22512,
			-177237,
			218140,
			-9536
		},
		{
			22512,
			-177021,
			217647,
			-9528
		},
		{
			22513,
			-177372,
			217792,
			-9544
		},
		{
			22513,
			-177372,
			218053,
			-9536
		},
		{
			22514,
			-177291,
			217734,
			-9544
		},
		{
			22514,
			-177264,
			217792,
			-9544
		},
		{
			22514,
			-177264,
			218053,
			-9536
		},
		{
			22515,
			-177156,
			217792,
			-9536
		},
		{
			22515,
			-177075,
			217647,
			-9528
		}
	};
	private static final int[][] TUMOR_SPAWNS =
	{
		{
			-186327,
			208286,
			-9544
		},
		{
			-184429,
			211155,
			-9544
		},
		{
			-182811,
			213871,
			-9496
		},
		{
			-181039,
			216633,
			-9528
		},
		{
			-177264,
			217760,
			-9544
		}
	};
	private static final int[][] TWIN_SPAWNS =
	{
		{
			25665,
			-173727,
			218169,
			-9536
		},
		{
			25666,
			-173727,
			218049,
			-9536
		}
	};
	private static final int[] TEPIOS_SPAWN =
	{
		-173727,
		218109,
		-9536
	};

	// Etc
	private static final int BOSS_INVUL_TIME = 30000; // in milisex
	private static final int BOSS_MINION_SPAWN_TIME = 60000; // in milisex
	private static final int BOSS_RESSURECT_TIME = 20000; // in milisex
	
	// Instance reenter time
	// Reset 6:30 AM
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	private boolean checkConditions(final L2PcInstance player)
	{
		if (debug)
		{
			if (player.isGM())
				return true;
			return false;
		}
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		if (GraciaSeedsManager.getInstance().getSoIState() > 2)
		{
			player.sendMessage("You can enter in Hall Of Suffering only on 1 or 2 stages Seed Of Infinity.");
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			party.broadcastToPartyMembers(sm);
			return false;
		}
		for (final L2PcInstance partyMember : party.getPartyMembers())
		{
			final QuestState quest = partyMember.getQuestState(Q00694_BreakThroughTheHallOfSuffering.class.getSimpleName());
			if (quest == null || quest.getState() != State.STARTED)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			if (partyMember.getLevel() < 75)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}

			final Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
		}
		return true;
	}

	private void teleportPlayer(final L2PcInstance player, final int[] coords, final int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	protected int enterInstance(final L2PcInstance player, final String template, final int[] coords)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof HSWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, coords, world.instanceId);
			return world.instanceId;
		}
		// New instance
		if (!checkConditions(player))
			return 0;
		final L2Party party = player.getParty();
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new HSWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		((HSWorld) world).startTime = System.currentTimeMillis();
		((HSWorld) world).ptLeaderName = player.getName();
		InstanceManager.getInstance().addWorld(world);
		_log.info("Hall Of Suffering started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		runTumors((HSWorld) world);

		// teleport players
		if (player.getParty() == null)
		{
			teleportPlayer(player, coords, instanceId);
			world.allowed.add(player.getObjectId());
		}
		else
			for (final L2PcInstance partyMember : party.getPartyMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.allowed.add(partyMember.getObjectId());
				if (partyMember.getQuestState(qn) == null)
					newQuestState(partyMember);
			}
		return instanceId;
	}

	protected boolean checkKillProgress(final L2Npc mob, final HSWorld world)
	{
		if (world.npcList.containsKey(mob))
			world.npcList.put(mob, true);
		for (final boolean isDead : world.npcList.values())
			if (!isDead)
				return false;
		return true;
	}

	protected int[][] getRoomSpawns(final int room)
	{
		switch (room)
		{
			case 0:
				return ROOM_1_MOBS;
			case 1:
				return ROOM_2_MOBS;
			case 2:
				return ROOM_3_MOBS;
			case 3:
				return ROOM_4_MOBS;
			case 4:
				return ROOM_5_MOBS;
		}
		_log.warning("");
		return new int[][] {};
	}

	protected void runTumors(final HSWorld world)
	{
		for (final int[] mob : getRoomSpawns(world.status))
		{
			final L2Npc npc = addSpawn(mob[0], mob[1], mob[2], mob[3], 0, false, 0, false, world.instanceId);
			world.npcList.put(npc, false);
		}
		final L2Npc mob = addSpawn(TUMOR_ALIVE, TUMOR_SPAWNS[world.status][0], TUMOR_SPAWNS[world.status][1], TUMOR_SPAWNS[world.status][2], 0, false, 0, false, world.instanceId);
		mob.disableCoreAI(true);
		mob.setIsImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp() * 0.5);
		world.npcList.put(mob, false);
		world.status++;
	}

	protected void runTwins(final HSWorld world)
	{
		world.status++;
		world.klodekus = addSpawn(TWIN_SPAWNS[0][0], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, world.instanceId);
		world.klanikus = addSpawn(TWIN_SPAWNS[1][0], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, world.instanceId);
		world.klanikus.setIsMortal(false);
		world.klodekus.setIsMortal(false);
	}

	protected void bossSimpleDie(final L2Npc boss)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (boss.isDead())
				return;
			// now reset currentHp to zero
			boss.setCurrentHp(0);
			boss.setIsDead(true);
		}

		// Set target to null and cancel Attack or Cast
		boss.setTarget(null);

		// Stop movement
		boss.stopMove(null);

		// Stop HP/MP/CP Regeneration task
		boss.getStatus().stopHpMpRegeneration();

		boss.stopAllEffectsExceptThoseThatLastThroughDeath();

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		boss.broadcastStatusUpdate();

		// Notify L2Character AI
		boss.getAI().notifyEvent(CtrlEvent.EVT_DEAD);

		if (boss.getWorldRegion() != null)
			boss.getWorldRegion().onDeath(boss);
	}

	private void calcRewardItemId(final HSWorld world)
	{
		final Long finishDiff = System.currentTimeMillis() - world.startTime;
		if (finishDiff < 1260000)
		{
			world.rewardHtm = "32530-00.htm";
			world.rewardItemId = 13777;
		}
		else if (finishDiff < 1380000)
		{
			world.rewardHtm = "32530-01.htm";
			world.rewardItemId = 13778;
		}
		else if (finishDiff < 1500000)
		{
			world.rewardHtm = "32530-02.htm";
			world.rewardItemId = 13779;
		}
		else if (finishDiff < 1620000)
		{
			world.rewardHtm = "32530-03.htm";
			world.rewardItemId = 13780;
		}
		else if (finishDiff < 1740000)
		{
			world.rewardHtm = "32530-04.htm";
			world.rewardItemId = 13781;
		}
		else if (finishDiff < 1860000)
		{
			world.rewardHtm = "32530-05.htm";
			world.rewardItemId = 13782;
		}
		else if (finishDiff < 1980000)
		{
			world.rewardHtm = "32530-06.htm";
			world.rewardItemId = 13783;
		}
		else if (finishDiff < 2100000)
		{
			world.rewardHtm = "32530-07.htm";
			world.rewardItemId = 13784;
		}
		else if (finishDiff < 2220000)
		{
			world.rewardHtm = "32530-08.htm";
			world.rewardItemId = 13785;
		}
		else
		{
			world.rewardHtm = "32530-09.htm";
			world.rewardItemId = 13786;
		}
	}

	private String getPtLeaderText(final L2PcInstance player, final HSWorld world)
	{
		String htmltext = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "/data/scripts/instances/HallOfSuffering/32530-10.htm");
		htmltext = htmltext.replaceAll("%ptLeader%", String.valueOf(world.ptLeaderName));
		return htmltext;
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (skill.getSkillType() == L2SkillType.BALANCE_LIFE || skill.getSkillType() == L2SkillType.HEAL || skill.getSkillType() == L2SkillType.HEAL_PERCENT || skill.getSkillType() == L2SkillType.HEAL_STATIC)
		{
			int hate = 2 * skill.getAggroPoints();
			if (hate < 2)
				hate = 1000;
			((L2Attackable) npc).addDamageHate(caster, 0, hate);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HSWorld)
		{
			final HSWorld world = (HSWorld) tmpworld;
			if (event.equalsIgnoreCase("spawnBossGuards"))
			{
				if (!world.klanikus.isInCombat() && !world.klodekus.isInCombat())
				{
					world.isBossesAttacked = false;
					return "";
				}
				L2Npc mob = addSpawn(TWIN_MOBIDS[getRandom(TWIN_MOBIDS.length)], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, npc.getInstanceId());
				((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				if (getRandom(100) < 33)
				{
					mob = addSpawn(TWIN_MOBIDS[getRandom(TWIN_MOBIDS.length)], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, npc.getInstanceId());
					((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
			}
			else if (event.equalsIgnoreCase("isTwinSeparated"))
			{
				if (Util.checkIfInRange(500, world.klanikus, world.klodekus, false))
				{
					world.klanikus.setIsInvul(false);
					world.klodekus.setIsInvul(false);
				}
				else
				{
					world.klanikus.setIsInvul(true);
					world.klodekus.setIsInvul(true);
				}
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (event.equalsIgnoreCase("ressurectTwin"))
			{
				final L2Skill skill = SkillTable.getInstance().getInfo(5824, 1);
				final L2Npc aliveTwin = world.klanikus == npc ? world.klodekus : world.klanikus;
				npc.doRevive();
				npc.doCast(skill);
				npc.setCurrentHp(aliveTwin.getCurrentHp());

				// get most hated of other boss
				final L2Character hated = ((L2MonsterInstance) aliveTwin).getMostHated();
				if (hated != null) // to prevent revived idling
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, 1000);

				aliveTwin.setIsInvul(true); // make other boss invul
				startQuestTimer("uninvul", BOSS_INVUL_TIME, aliveTwin, null);
			}
			else if (event.equals("uninvul"))
				npc.setIsInvul(false);
		}
		return "";
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HSWorld)
			if (!((HSWorld) tmpworld).isBossesAttacked)
			{
				((HSWorld) tmpworld).isBossesAttacked = true;

				final Calendar reenter = Calendar.getInstance();
				reenter.set(Calendar.MINUTE, RESET_MIN);
				// if time is >= RESET_HOUR - roll to the next day
				if (reenter.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
					reenter.add(Calendar.DATE, 1);
				reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);

				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(tmpworld.templateId));

				// set instance reenter time for all allowed players
				for (final int objectId : tmpworld.allowed)
				{
					final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
					if (player != null && player.isOnline())
					{
						InstanceManager.getInstance().setInstanceTime(objectId, tmpworld.templateId, reenter.getTimeInMillis());
						player.sendPacket(sm);
					}
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (damage >= npc.getCurrentHp())
				if (((HSWorld) tmpworld).klanikus.isDead())
				{
					((HSWorld) tmpworld).klanikus.setIsDead(false);
					((HSWorld) tmpworld).klanikus.doDie(attacker);
					((HSWorld) tmpworld).klodekus.doDie(attacker);
				}
				else if (((HSWorld) tmpworld).klodekus.isDead())
				{
					((HSWorld) tmpworld).klodekus.setIsDead(false);
					((HSWorld) tmpworld).klodekus.doDie(attacker);
					((HSWorld) tmpworld).klanikus.doDie(attacker);
				}
				else
				{
					bossSimpleDie(npc);
					startQuestTimer("ressurectTwin", BOSS_RESSURECT_TIME, npc, null);
				}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof HSWorld)
		{
			final HSWorld world = (HSWorld) tmpworld;

			if (npc.getId() == TUMOR_ALIVE)
				addSpawn(TUMOR_DEAD, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, npc.getInstanceId());
			if (world.status < 5)
			{
				if (checkKillProgress(npc, world))
					runTumors(world);
			}
			else if (world.status == 5)
			{
				if (checkKillProgress(npc, world))
					runTwins(world);
			}
			else if (world.status == 6 && (npc.getId() == KLODEKUS || npc.getId() == KLANIKUS))
				if (world.klanikus.isDead() && world.klodekus.isDead())
				{
					world.status++;
					// instance end
					calcRewardItemId(world);
					world.klanikus = null;
					world.klodekus = null;
					cancelQuestTimers("ressurectTwin");
					cancelQuestTimers("spawnBossGuards");
					cancelQuestTimers("isTwinSeparated");
					addSpawn(TEPIOS, TEPIOS_SPAWN[0], TEPIOS_SPAWN[1], TEPIOS_SPAWN[2], 0, false, 0, false, world.instanceId);
					GraciaSeedsManager.getInstance().addTwinKill(); // Send data for Gracia Seeds Manager
				}
		}
		return "";
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (npc.getId() == TEPIOS)
		{
			final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if (((HSWorld) world).rewardItemId == -1)
			{
				_log.warning("Hall of Suffering: " + player.getName() + "(" + player.getObjectId() + ") is try to cheat!");
				return getPtLeaderText(player, (HSWorld) world);
			}
			else if (((HSWorld) world).isRewarded)
				return "32530-11.htm";
			else if (player.getParty() != null && player.getParty().getPartyLeaderOID() == player.getObjectId())
				return ((HSWorld) world).rewardHtm;

			return getPtLeaderText(player, (HSWorld) world);
		}
		return "";
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		if (npcId == MOUTHOFEKIMUS)
		{
			enterInstance(player, "HallOfSuffering.xml", ENTER_TELEPORT);
			return null;
		}
		else if (npcId == TEPIOS)
		{
			final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if (((HSWorld) world).rewardItemId == -1)
			{
				_log.warning("Hall of Suffering: " + player.getName() + "(" + player.getObjectId() + ") is try to cheat!");
				return getPtLeaderText(player, (HSWorld) world);
			}
			else if (((HSWorld) world).isRewarded)
				return "32530-11.htm";
			else if (player.getParty() != null && player.getParty().getPartyLeaderOID() == player.getObjectId())
			{
				((HSWorld) world).isRewarded = true;
				for (final L2PcInstance pl : player.getParty().getPartyMembers())
				{
					final QuestState quest = pl.getQuestState(Q00694_BreakThroughTheHallOfSuffering.class.getSimpleName());
					if (quest != null && quest.getState() == State.STARTED)
					{
						st = pl.getQuestState(qn);
						st.giveItems(736);
						st.giveItems(((HSWorld) world).rewardItemId);
						if (((HSWorld) world).rewardMark && st.getQuestItemsCount(MARK_STAGE_1) < 1)
							st.giveItems(MARK_STAGE_1);
						quest.unset("cond");
						quest.playSound("ItemSound.quest_finish");
						quest.exitQuest(true);
					}
				}
				return null;
			}
			return getPtLeaderText(player, (HSWorld) world);
		}
		return null;
	}

	public HallOfSuffering(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
		addStartNpc(TEPIOS);
		addFirstTalkId(TEPIOS);
		addTalkId(TEPIOS);
		addKillId(TUMOR_ALIVE);
		addKillId(KLODEKUS);
		addKillId(KLANIKUS);
		addAttackId(KLODEKUS);
		addAttackId(KLANIKUS);
		for (final int mobId : TUMOR_MOBIDS)
		{
			addSkillSeeId(mobId);
			addKillId(mobId);
		}
	}

	public static void main(final String[] args)
	{
		// now call the constructor (starts up the)
		new HallOfSuffering(-1, qn, "instances");
	}
}