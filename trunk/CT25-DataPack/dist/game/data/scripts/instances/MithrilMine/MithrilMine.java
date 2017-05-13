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
package instances.MithrilMine;

import java.util.List;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Util;
//import ct25.xtreme.util.Rnd;
import javolution.util.FastList;
import quests.Q10284_AcquisitionOfDivineSword.Q10284_AcquisitionOfDivineSword;

/**
 * Mithril Mine
 * @author Browser
 */
public class MithrilMine extends Quest
{
	private class MMWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
			0,
			0
		};
		public boolean underAttack = false;
		public L2Npc KEGOR = null;
		public List<L2Attackable> liveMobs;
		
		public MMWorld()
		{

		}
	}
	
	private static final String qn = "MithrilMine";
	private static final int INSTANCEID = 138;

	private static final int KROON = 32653;
	private static final int TAROON = 32654;
	private static final int KEGOR = 18846;
	private static final int MONSTER = 22766;

	private static final int ANTIDOTE = 15514;

	private static final int BUFF = 6286;
	
	private static final int[][] MOB_SPAWNS =
	{
		{
			185216,
			-184112,
			-3308,
			-15396
		},
		{
			185456,
			-184240,
			-3308,
			-19668
		},
		{
			185712,
			-184384,
			-3308,
			-26696
		},
		{
			185920,
			-184544,
			-3308,
			-32544
		},
		{
			185664,
			-184720,
			-3308,
			27892
		},
	};

	private static final int[] ENTRY_POINT =
	{
		186852,
		-173492,
		-3763
	};

	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}

	private void teleportplayer(final L2PcInstance player, final teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	
	private boolean checkConditions(final L2PcInstance player)
	{
		if (player.getLevel() < 82 || player.getLevel() > 85)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}

		return true;
	}
	
	protected void exitInstance(final L2PcInstance player, final teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}
	
	protected int enterInstance(final L2PcInstance player, final String template, final teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			// this instance
			if (!(world instanceof MMWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}
		// New instance
		if (!checkConditions(player))
			return 0;
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setSpawnLoc(new int[]
		{
			player.getX(),
			player.getY(),
			player.getZ()
		});
		world = new MMWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;

		((MMWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("MithrilMine started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());
		return instanceId;
	}
	
	@SuppressWarnings("null")
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (npc.getId() == KEGOR)
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld != null && tmpworld instanceof MMWorld)
			{
				final MMWorld world = (MMWorld) tmpworld;
				
				if (event.equalsIgnoreCase("spawn"))
				{
					world.liveMobs = new FastList<>();
					for (final int[] spawn : MOB_SPAWNS)
					{
						final L2Attackable spawnedMob = (L2Attackable) addSpawn(MONSTER, spawn[0], spawn[1], spawn[2], spawn[3], false, 0, false, world.instanceId);
						world.liveMobs.add(spawnedMob);
					}
				}

				else if (event.equalsIgnoreCase("buff"))
					// schedule mob attack
					if (world != null && world.liveMobs != null && !world.liveMobs.isEmpty())
					{
						for (final L2Attackable monster : world.liveMobs)
							if (monster.getKnownList().knowsObject(npc))
							{
								monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc);
								monster.addDamageHate(npc, 0, 999999);
							}
							else
								monster.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(npc.getX(), npc.getY(), npc.getZ(), 0));
							
						// buff player
						if (npc.getKnownList().getKnownPlayers().size() == 1)
						{
							final L2Skill buff = SkillTable.getInstance().getInfo(BUFF, 1);
							if (buff != null)
								for (final L2PcInstance pl : npc.getKnownList().getKnownPlayers().values())
									if (Util.checkIfInRange(buff.getCastRange(), npc, pl, false))
									{
										npc.setTarget(pl);
										npc.doCast(buff);
									}
						}
						startQuestTimer("buff", 30000, npc, player);
					}
			}
		}
		return null;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();
		String htmltext = getNoQuestMsg(player);

		final QuestState hostQuest = player.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());

		if (hostQuest == null)
			return htmltext;
		
		if (npcId == KROON || npcId == TAROON)
		{
			final teleCoord tele = new teleCoord();
			tele.x = ENTRY_POINT[0];
			tele.y = ENTRY_POINT[1];
			tele.z = ENTRY_POINT[2];
			
			htmltext = npcId == KROON ? "32653-07.htm" : "32654-07.htm";
			if (enterInstance(player, "MithrilMine.xml", tele) > 0)
			{
				htmltext = "";
				if (hostQuest.getInt("progress") == 2 && hostQuest.getQuestItemsCount(ANTIDOTE) == 0)
				{
					hostQuest.giveItems(ANTIDOTE, 1);
					hostQuest.playSound("ItemSound.quest_middle");
					hostQuest.set("cond", "4");
				}
			}
		}
		
		else if (npc.getId() == KEGOR)
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if (tmpworld != null && tmpworld instanceof MMWorld)
			{
				final MMWorld world = (MMWorld) tmpworld;
				if (hostQuest.getInt("progress") == 2 && hostQuest.getQuestItemsCount(ANTIDOTE) > 0 && !world.underAttack)
				{
					hostQuest.takeItems(ANTIDOTE, hostQuest.getQuestItemsCount(ANTIDOTE));
					hostQuest.playSound("ItemSound.quest_middle");
					hostQuest.set("cond", "5");
					htmltext = "18846-01.htm";
					world.underAttack = true;
					npc.setIsInvul(false);
					npc.setIsMortal(true);
					startQuestTimer("spawn", 3000, npc, player);
					startQuestTimer("buff", 3500, npc, player);
					// startQuestTimer("attack_mobs", 10000, npc, player);
				}
				
				else if (hostQuest.getState() == State.COMPLETED)
				{
					world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
					final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
					final teleCoord tele = new teleCoord();
					tele.instanceId = 0;
					tele.x = inst.getSpawnLoc()[0];
					tele.y = inst.getSpawnLoc()[1];
					tele.z = inst.getSpawnLoc()[2];
					exitInstance(player, tele);
					htmltext = "";
				}
			}
		}

		return htmltext;
	}
	
	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState hostQuest = player.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
		if (hostQuest == null)
			return null;
		
		if (npc.getId() == KEGOR)
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if (tmpworld != null && tmpworld instanceof MMWorld)
			{
				final MMWorld world = (MMWorld) tmpworld;

				if (world.KEGOR == null)
					world.KEGOR = npc;
				
				if (hostQuest.getState() != State.STARTED)
					return "18846-04.htm";
				
				if (!world.underAttack && hostQuest.getInt("progress") == 2)
					return "18846-00.htm";
				
				else if (hostQuest.getInt("progress") == 3)
				{
					hostQuest.giveItems(57, 296425);
					hostQuest.addExpAndSp(921805, 82230);
					hostQuest.playSound("ItemSound.quest_finish");
					hostQuest.exitQuest(false);
					return "18846-03.htm";
				}

				else
					return "18846-02.htm";
			}
		}
		
		return null;
	}
	
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final QuestState hostQuest = player.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
		if (hostQuest == null || hostQuest.getState() != State.STARTED)
			return null;
		
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld != null && tmpworld instanceof MMWorld)
		{
			final MMWorld world = (MMWorld) tmpworld;
			
			if (npc.getId() == MONSTER)
				if (world.liveMobs != null)
				{
					world.liveMobs.remove(npc);
					if (world.liveMobs.isEmpty() && world.KEGOR != null && !world.KEGOR.isDead() && hostQuest.getInt("progress") == 2)
					{
						world.underAttack = false;
						world.liveMobs = null;
						cancelQuestTimer("buff", world.KEGOR, null);
						world.KEGOR.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player, null);
						final NpcSay cs = new NpcSay(world.KEGOR.getObjectId(), Say2.ALL, world.KEGOR.getId(), 1801099);
						world.KEGOR.broadcastPacket(cs);
						hostQuest.set("progress", "3");
						hostQuest.set("cond", "6");
						hostQuest.playSound("ItemSound.quest_middle");
						
						// destroy instance after 3 min
						final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
						inst.setDuration(3 * 60000);
						inst.setEmptyDestroyTime(0);
					}
				}
				
				else if (npc.getId() == KEGOR)
				{
					world.KEGOR = null;
					final NpcSay cs = new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1801098);
					npc.broadcastPacket(cs);
					
					// destroy instance after 1 min
					final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
					inst.setDuration(60000);
					inst.setEmptyDestroyTime(0);
				}
		}
		return null;
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{

		// Doesn't work now. NPC doesn't wish to attack Monster
		/*
		 * else if (npcId == _mob && KEGOR != null) { if (getQuestTimer("attack_mobs", KEGOR, null) == null) startQuestTimer("attack_mobs", 10000, KEGOR, null); }
		 */
		return super.onSpawn(npc);
	}

	public MithrilMine(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(KEGOR);
		addStartNpc(KROON);
		addStartNpc(TAROON);
		addTalkId(KROON);
		addTalkId(TAROON);
		addTalkId(KEGOR);
		addKillId(KEGOR);
		addKillId(MONSTER);
		addSpawnId(KEGOR);
		addSpawnId(MONSTER);

	}

	public static void main(final String[] args)
	{
		new MithrilMine(-1, qn, "instances");
	}
}