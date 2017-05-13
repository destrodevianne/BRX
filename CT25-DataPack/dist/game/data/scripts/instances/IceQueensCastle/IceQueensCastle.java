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
package instances.IceQueensCastle;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.ExShowBroadcastMessage;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import javolution.util.FastList;
import quests.Q10285_MeetingSirra.Q10285_MeetingSirra;

/**
 * @author Browser
 */
public class IceQueensCastle extends Quest
{
	// Npcs
	private static final int CONTROLLER = 18930;
	private static final int FREYA = 18847;
	private static final int BATTALION_LEADER = 18848;
	private static final int LEGIONNAIRE = 18849;
	private static final int MERCENARY_ARCHER = 18926;
	private static final int ARCHERY_KNIGHT = 22767;
	private static final int JINIA = 32781;
	
	// Locs
	private static final int[] ENTRY_POINT =
	{
		114000,
		-112357,
		-11200
	};

	// Skill
	private static SkillHolder ETHERNAL_BLIZZARD = new SkillHolder(6276, 1);
	
	// Misc
	private static final int INSTANCEID = 137;
	private static final int MIN_LV = 82;
	private static final int ICE_QUEEN_DOOR = 23140101;

	private class FDWorld extends InstanceWorld
	{
		L2Attackable freya = null;
		L2Attackable jinia_guard1 = null;
		L2Attackable jinia_guard2 = null;
		L2Attackable jinia_guard3 = null;
		L2Attackable jinia_guard4 = null;
		L2Attackable jinia_guard5 = null;
		L2Attackable jinia_guard6 = null;
		L2Attackable freya_guard1 = null;
		L2Attackable freya_guard2 = null;
		L2Attackable freya_guard3 = null;
		L2Attackable freya_guard4 = null;
		L2Attackable freya_guard5 = null;
		L2Attackable controller = null;

		public FDWorld()
		{
		}
	}
	
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
	
	public IceQueensCastle(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(JINIA);
		addTalkId(JINIA);
		addAggroRangeEnterId(CONTROLLER);
		addAttackId(BATTALION_LEADER, LEGIONNAIRE, MERCENARY_ARCHER);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		if (npcId == JINIA)
		{
			final teleCoord tele = new teleCoord();
			tele.x = ENTRY_POINT[0];
			tele.y = ENTRY_POINT[1];
			tele.z = ENTRY_POINT[2];
			
			final QuestState hostQuest = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
			
			if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getProgress() == 2)
				hostQuest.setCond(9, true);
			
			if (enterInstance(player, "IceQueensCastle.xml", tele) <= 0)
				return "32781-10.htm";
		}
		return "";
	}
	
	@SuppressWarnings("null")
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof FDWorld)
		{
			final FDWorld world = (FDWorld) tmpworld;
			
			if (event.equalsIgnoreCase("check_guards"))
			{
				if (world != null)
				{
					if ((world.freya_guard1 == null || world.freya_guard1.isDead()) && getQuestTimer("spawn_ice_guard1", null, player) == null)
						startQuestTimer("spawn_ice_guard1", 30000, null, player);
					if ((world.freya_guard2 == null || world.freya_guard2.isDead()) && getQuestTimer("spawn_ice_guard2", null, player) == null)
						startQuestTimer("spawn_ice_guard2", 30000, null, player);
					if ((world.freya_guard3 == null || world.freya_guard3.isDead()) && getQuestTimer("spawn_ice_guard3", null, player) == null)
						startQuestTimer("spawn_ice_guard3", 30000, null, player);
					if ((world.freya_guard4 == null || world.freya_guard4.isDead()) && getQuestTimer("spawn_ice_guard4", null, player) == null)
						startQuestTimer("spawn_ice_guard4", 30000, null, player);
					if ((world.freya_guard5 == null || world.freya_guard5.isDead()) && getQuestTimer("spawn_ice_guard5", null, player) == null)
						startQuestTimer("spawn_ice_guard5", 30000, null, player);
					
					if ((world.jinia_guard1 == null || world.jinia_guard1.isDead()) && getQuestTimer("spawn_guard1", null, player) == null)
						startQuestTimer("spawn_guard1", 60000, null, player);
					else
						world.jinia_guard1.stopHating(player);
					if ((world.jinia_guard2 == null || world.jinia_guard2.isDead()) && getQuestTimer("spawn_guard2", null, player) == null)
						startQuestTimer("spawn_guard2", 45000, null, player);
					else
						world.jinia_guard2.stopHating(player);
					if ((world.jinia_guard3 == null || world.jinia_guard3.isDead()) && getQuestTimer("spawn_guard3", null, player) == null)
						startQuestTimer("spawn_guard3", 45000, null, player);
					else
						world.jinia_guard3.stopHating(player);
					if ((world.jinia_guard4 == null || world.jinia_guard4.isDead()) && getQuestTimer("spawn_guard4", null, player) == null)
						startQuestTimer("spawn_guard4", 60000, null, player);
					else
						world.jinia_guard4.stopHating(player);
					if ((world.jinia_guard5 == null || world.jinia_guard5.isDead()) && getQuestTimer("spawn_guard5", null, player) == null)
						startQuestTimer("spawn_guard5", 45000, null, player);
					else
						world.jinia_guard5.stopHating(player);
					if ((world.jinia_guard6 == null || world.jinia_guard6.isDead()) && getQuestTimer("spawn_guard6", null, player) == null)
						startQuestTimer("spawn_guard6", 45000, null, player);
					else
						world.jinia_guard6.stopHating(player);
				}
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard1"))
			{
				if (world != null)
				{
					world.freya_guard1 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 114713, -115109, -11198, 16456, false, 0, false, world.instanceId);
					final L2Character target = getRandomTargetFreya(world);
					world.freya_guard1.addDamageHate(target, 9999, 9999);
					world.freya_guard1.setRunning();
					world.freya_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard2"))
			{
				if (world != null)
				{
					world.freya_guard2 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 114008, -115080, -11198, 3568, false, 0, false, world.instanceId);
					final L2Character target = getRandomTargetFreya(world);
					world.freya_guard2.addDamageHate(target, 9999, 9999);
					world.freya_guard2.setRunning();
					world.freya_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard3"))
			{
				if (world != null)
				{
					world.freya_guard3 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 114422, -115508, -11198, 12400, false, 0, false, world.instanceId);
					final L2Character target = getRandomTargetFreya(world);
					world.freya_guard3.addDamageHate(target, 9999, 9999);
					world.freya_guard3.setRunning();
					world.freya_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard4"))
			{
				if (world != null)
				{
					world.freya_guard4 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 115023, -115508, -11198, 20016, false, 0, false, world.instanceId);
					final L2Character target = getRandomTargetFreya(world);
					world.freya_guard4.addDamageHate(target, 9999, 9999);
					world.freya_guard4.setRunning();
					world.freya_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_ice_guard5"))
			{
				if (world != null)
				{
					world.freya_guard5 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 115459, -115079, -11198, 27936, false, 0, false, world.instanceId);
					final L2Character target = getRandomTargetFreya(world);
					world.freya_guard5.addDamageHate(target, 9999, 9999);
					world.freya_guard5.setRunning();
					world.freya_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_guard1"))
			{
				if (world != null)
				{
					world.jinia_guard1 = (L2Attackable) addSpawn(BATTALION_LEADER, 114861, -113615, -11198, -21832, false, 0, false, world.instanceId);
					world.jinia_guard1.setRunning();
					final L2Character target = getRandomTargetGuard(world);
					world.jinia_guard1.addDamageHate(target, 9999, 9999);
					world.jinia_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_guard2"))
			{
				if (world != null)
				{
					world.jinia_guard2 = (L2Attackable) addSpawn(LEGIONNAIRE, 114950, -113647, -11198, -20880, false, 0, false, world.instanceId);
					world.jinia_guard2.setRunning();
					final L2Character target = getRandomTargetGuard(world);
					world.jinia_guard2.addDamageHate(target, 9999, 9999);
					world.jinia_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_guard3"))
			{
				if (world != null)
				{
					world.jinia_guard3 = (L2Attackable) addSpawn(MERCENARY_ARCHER, 115041, -113694, -11198, -22440, false, 0, false, world.instanceId);
					world.jinia_guard3.setRunning();
					final L2Character target = getRandomTargetGuard(world);
					world.jinia_guard3.addDamageHate(target, 9999, 9999);
					world.jinia_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_guard4"))
			{
				if (world != null)
				{
					world.jinia_guard4 = (L2Attackable) addSpawn(BATTALION_LEADER, 114633, -113619, -11198, -12224, false, 0, false, world.instanceId);
					world.jinia_guard4.setRunning();
					final L2Character target = getRandomTargetGuard(world);
					world.jinia_guard4.addDamageHate(target, 9999, 9999);
					world.jinia_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_guard5"))
			{
				if (world != null)
				{
					world.jinia_guard5 = (L2Attackable) addSpawn(LEGIONNAIRE, 114540, -113654, -11198, -12880, false, 0, false, world.instanceId);
					world.jinia_guard5.setRunning();
					final L2Character target = getRandomTargetGuard(world);
					world.jinia_guard5.addDamageHate(target, 9999, 9999);
					world.jinia_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("spawn_guard6"))
			{
				if (world != null)
				{
					world.jinia_guard6 = (L2Attackable) addSpawn(MERCENARY_ARCHER, 114446, -113698, -11198, -11264, false, 0, false, world.instanceId);
					world.jinia_guard6.setRunning();
					final L2Character target = getRandomTargetGuard(world);
					world.jinia_guard6.addDamageHate(target, 9999, 9999);
					world.jinia_guard6.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("go_fight"))
			{
				if (world != null)
				{
					final NpcSay ns = new NpcSay(world.jinia_guard1.getObjectId(), 0, world.jinia_guard1.getId(), 1801096);
					ns.addStringParameter(player.getAppearance().getVisibleName());
					player.sendPacket(ns);
					
					world.jinia_guard1.setRunning();
					world.jinia_guard2.setRunning();
					world.jinia_guard3.setRunning();
					world.jinia_guard4.setRunning();
					world.jinia_guard5.setRunning();
					world.jinia_guard6.setRunning();

					world.jinia_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114673, -114324, -11200, 0));
					world.jinia_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114745, -114324, -11200, 0));
					world.jinia_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114711, -114324, -11200, 0));
					world.jinia_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114662, -114324, -11200, 0));
					world.jinia_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(115041, -114324, -11200, 0));
					world.jinia_guard6.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114446, -114324, -11200, 0));

					world.freya_guard1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114713, -114920, -11200, 0));
					world.freya_guard2.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114008, -114920, -11200, 0));
					world.freya_guard3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114422, -114920, -11200, 0));
					world.freya_guard4.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(115023, -114920, -11200, 0));
					world.freya_guard5.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(115459, -114920, -11200, 0));

					world.freya.setIsRunning(true);
					world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114722, -114798, -11205, 15956));
					startQuestTimer("freya", 17000, null, player);
				}
			}
			else if (event.equalsIgnoreCase("freya"))
			{
				if (world != null)
				{
					final L2Character target = getRandomTargetFreya(world);
					world.freya.addDamageHate(target, 9999, 9999);
					world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			}
			else if (event.equalsIgnoreCase("time_blizzard"))
			{
				cancelQuestTimer("spawn_guard1", null, player);
				cancelQuestTimer("spawn_guard2", null, player);
				cancelQuestTimer("spawn_guard3", null, player);
				cancelQuestTimer("spawn_guard4", null, player);
				cancelQuestTimer("spawn_guard5", null, player);
				cancelQuestTimer("spawn_guard6", null, player);
				cancelQuestTimer("check_guards", null, player);
				cancelQuestTimer("spawn_ice_guard1", null, player);
				cancelQuestTimer("spawn_ice_guard2", null, player);
				cancelQuestTimer("spawn_ice_guard3", null, player);
				cancelQuestTimer("spawn_ice_guard4", null, player);
				cancelQuestTimer("spawn_ice_guard5", null, player);

				if (world != null)
				{
					world.freya.broadcastNpcSay(Say2.ALL, "I can no longer stand by.");
					world.freya.stopMove(null);
					world.freya.setTarget(player);
					world.freya.doCast(ETHERNAL_BLIZZARD.getSkill());
					startQuestTimer("timer_leave", 7000, null, player);
				}
			}
			else if (event.equalsIgnoreCase("timer_leave"))
			{
				player.sendPacket(new ExShowBroadcastMessage(1801111, 3000, ExShowBroadcastMessage.ScreenMessageAlign.MIDDLE_CENTER, true, false, -1, true));
				startQuestTimer("scene_21", 3000, null, player);
				
				final QuestState st = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
				if (st != null && st.getState() == State.STARTED && st.getProgress() == 2)
				{
					st.setCond(10, true);
					st.setProgress(3);
				}
			}
			else if (event.equalsIgnoreCase("scene_21"))
			{
				player.showQuestMovie(21);
				player.setInstanceId(0);
				player.teleToLocation(113851, -108987, -837);
				if (world != null)
					InstanceManager.getInstance().destroyInstance(world.instanceId);
			}
		}
		return null;
	}

	protected int enterInstance(final L2PcInstance player, final String template, final teleCoord teleto)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof FDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}
		// New instance
		if (!checkCond(player))
			return 0;
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new FDWorld();

		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;

		world.allowed.add(player.getObjectId());

		InstanceManager.getInstance().addWorld(world);
		_log.info("IceQueensCastle started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());
		spawnFirst((FDWorld) world);
		
		return instanceId;
	}

	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof FDWorld)
		{
			final FDWorld world = (FDWorld) tmpworld;
			if (npc.getId() == CONTROLLER)
			{
				world.jinia_guard1.setIsImmobilized(false);
				world.jinia_guard2.setIsImmobilized(false);
				world.jinia_guard3.setIsImmobilized(false);
				world.jinia_guard4.setIsImmobilized(false);
				world.jinia_guard5.setIsImmobilized(false);
				world.jinia_guard6.setIsImmobilized(false);
				world.freya.setIsImmobilized(false);
				world.freya_guard1.setIsImmobilized(false);
				world.freya_guard2.setIsImmobilized(false);
				world.freya_guard3.setIsImmobilized(false);
				world.freya_guard4.setIsImmobilized(false);
				world.freya_guard5.setIsImmobilized(false);

				startQuestTimer("go_fight", 300, npc, player);
				startQuestTimer("time_blizzard", 120000, npc, player);
				startQuestTimer("check_guards", 1000, null, player, true);
				world.controller.deleteMe();
				world.controller = null;
			}
		}
		return null;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		final int npcId = npc.getId();
		if (npcId == BATTALION_LEADER || npcId == LEGIONNAIRE || npcId == MERCENARY_ARCHER)
		{
			npc.setCurrentHp(npc.getCurrentHp() + damage);
			((L2Attackable) npc).stopHating(attacker);
		}
		return onAttack(npc, attacker, damage, isPet);
	}

	private void spawnFirst(final FDWorld world)
	{
		world.freya = (L2Attackable) addSpawn(FREYA, 114722, -114798, -11205, 15956, false, 0, false, world.instanceId);
		world.freya.teleToLocation(114720, -117085, -11088, 15956, false);

		world.jinia_guard1 = (L2Attackable) addSpawn(BATTALION_LEADER, 114861, -113615, -11198, -21832, false, 0, false, world.instanceId);
		world.jinia_guard2 = (L2Attackable) addSpawn(LEGIONNAIRE, 114950, -113647, -11198, -20880, false, 0, false, world.instanceId);
		world.jinia_guard3 = (L2Attackable) addSpawn(MERCENARY_ARCHER, 115041, -113694, -11198, -22440, false, 0, false, world.instanceId);
		world.jinia_guard4 = (L2Attackable) addSpawn(BATTALION_LEADER, 114633, -113619, -11198, -12224, false, 0, false, world.instanceId);
		world.jinia_guard5 = (L2Attackable) addSpawn(LEGIONNAIRE, 114540, -113654, -11198, -12880, false, 0, false, world.instanceId);
		world.jinia_guard6 = (L2Attackable) addSpawn(MERCENARY_ARCHER, 114446, -113698, -11198, -11264, false, 0, false, world.instanceId);
		world.freya_guard1 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 114713, -115109, -11198, 16456, false, 0, false, world.instanceId);
		world.freya_guard2 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 114008, -115080, -11198, 3568, false, 0, false, world.instanceId);
		world.freya_guard3 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 114422, -115508, -11198, 12400, false, 0, false, world.instanceId);
		world.freya_guard4 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 115023, -115508, -11198, 20016, false, 0, false, world.instanceId);
		world.freya_guard5 = (L2Attackable) addSpawn(ARCHERY_KNIGHT, 115459, -115079, -11198, 27936, false, 0, false, world.instanceId);
		world.controller = (L2Attackable) addSpawn(CONTROLLER, 114713, -113578, -11200, 27936, false, 0, false, world.instanceId);

		world.controller.setIsImmobilized(true);
		world.jinia_guard1.setIsImmobilized(true);
		world.jinia_guard2.setIsImmobilized(true);
		world.jinia_guard3.setIsImmobilized(true);
		world.jinia_guard4.setIsImmobilized(true);
		world.jinia_guard5.setIsImmobilized(true);
		world.jinia_guard6.setIsImmobilized(true);
		world.freya.setIsImmobilized(true);
		world.freya_guard1.setIsImmobilized(true);
		world.freya_guard2.setIsImmobilized(true);
		world.freya_guard3.setIsImmobilized(true);
		world.freya_guard4.setIsImmobilized(true);
		world.freya_guard5.setIsImmobilized(true);
		world.freya_guard1.setRunning();
		world.freya_guard2.setRunning();
		world.freya_guard3.setRunning();
		world.freya_guard4.setRunning();
		world.freya_guard5.setRunning();

		InstanceManager.getInstance().getInstance(world.instanceId).getDoor(ICE_QUEEN_DOOR).openMe();
	}

	private L2Npc getRandomTargetFreya(final FDWorld world)
	{
		final FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world.jinia_guard1;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.jinia_guard2;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.jinia_guard3;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.jinia_guard4;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.jinia_guard5;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.jinia_guard6;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		if (npcList.size() > 0)
			return npcList.get(getRandom(npcList.size() - 1));
		return null;
	}

	private L2Npc getRandomTargetGuard(final FDWorld world)
	{
		final FastList<L2Npc> npcList = new FastList<>();
		L2Npc victim = null;
		victim = world.freya_guard1;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.freya_guard2;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.freya_guard3;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.freya_guard4;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		victim = world.freya_guard5;
		if (victim != null && !victim.isDead())
			npcList.add(victim);
		if (npcList.size() > 0)
			return npcList.get(getRandom(npcList.size() - 1));
		return null;
	}

	private boolean checkCond(final L2PcInstance player)
	{
		if (player.getLevel() < MIN_LV)
		{
			player.sendPacket(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			return false;
		}
		return true;
	}
	
	public static void main(final String[] args)
	{
		new IceQueensCastle(-1, IceQueensCastle.class.getSimpleName(), "instances");
	}
}