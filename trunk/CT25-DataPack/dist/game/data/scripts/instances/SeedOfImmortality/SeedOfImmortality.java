package instances.SeedOfImmortality;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2CommandChannel;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.zone.type.L2BossZone;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.gameserver.network.serverpackets.MoveToLocation;
import ct25.xtreme.gameserver.network.serverpackets.SocialAction;
import ct25.xtreme.gameserver.network.serverpackets.SpecialCamera;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Util;
import javolution.util.FastList;

/**
 * @author RosT, Kazumi
 */

public class SeedOfImmortality extends L2AttackableAIScript
{
	private class SoIWorld extends InstanceWorld
	{
		// public long endTime = 0;
		public SoIWorld(final Long time)
		{
			super();
			// endTime = time;
		}
	}
	
	private static final String qn = "SeedOfImmortality";
	private static final int INSTANCEID = 119;
	private static final int INSTANCEPENALTY = 86400000; // reenter time: 86400000ms(24h)
	private static final boolean debug = false;
	
	private static L2BossZone _Zone;
	
	private L2Npc camera1, camera3, preawakened, ekimus, hound1, hound2, realhound1, realhound2;
	// NPC IDs
	private static final int TEPIOS = 32530;
	// private static final int TUMOR_OF_DEATH_1 = 18704;
	// private static final int TUMOR_OF_DEATH_2 = 18708;
	// private static final int DESTROYED_TUMOR = 18705;
	// private static final int SOUL_COFFIN_MAIN = 18706;
	// private static final int SOUL_COFFIN_1 = 18709;
	// private static final int SOUL_COFFIN_2 = 18710;
	// private static final int SOUL_COFFIN_3 = 18711;
	// private static final int SOUL_COFFIN_4 = 18712;
	// private static final int SOUL_COFFIN_5 = 18713;
	// private static final int SOUL_COFFIN_6 = 18714;
	// private static final int CONQUERED_SOUL_1 = 18715;
	// private static final int CONQUERED_SOUL_2 = 18716;
	// private static final int CONQUERED_SOUL_3 = 18717;
	// MOB IDs
	// private static final int FANATIC_OF_INFINITY = 22509;
	// private static final int ROTTEN_MESSENGER_A = 22510;
	// private static final int ZEALOT_OF_INFINITY = 22511;
	// private static final int BODY_SEVERER = 22512;
	// private static final int BODY_HARVESTER_A = 22513;
	// private static final int SOUL_EXPLOITER_A = 22514;
	// private static final int SOUL_DEVOURER_A = 22515;
	// private static final int BUTCHER_OF_INFINITY = 22516;
	// private static final int DISCIPLE_OF_INFINITY = 22517;
	// private static final int ROTTEN_MESSENGER_B = 22518;
	// private static final int BODY_HARVESTER_B = 22519;
	// private static final int BODY_SEVERER = 22520;
	// private static final int SOUL_EXPLOITER_B = 22521;
	// private static final int SOUL_DEVOURER_B = 22522;
	// private static final int SOUL_DEVOURER_C = 22523;
	// private static final int EMISSARY_OF_DEATH_A = 22524;
	// private static final int EMISSARY_OF_DEATH_B = 22525;
	// private static final int EMISSARY_OF_DEATH_C = 22526;
	// private static final int EMISSARY_OF_DEATH_D = 22527;
	// private static final int CURSED_PREFECT_A = 22528;
	// private static final int CURSED_PREFECT_B = 22529;
	// private static final int CURSED_PREFECT_C = 22530;
	// private static final int CURSED_PREFECT_D = 22531;
	// private static final int LAW_SCHOLAR_OF_CONCLUSIONS_A = 22532;
	// private static final int LAW_SCHOLAR_OF_CONCLUSIONS_B = 22533;
	// private static final int LAW_SCHOLAR_OF_CONCLUSIONS_C = 22534;
	// private static final int LAW_SCHOLAR_OF_CONCLUSIONS_D = 22535;
	// RAID IDs
	private static final int PREAWAKENED = 29161;
	private static final int EKIMUS = 29150;
	private static final int HOUND = 29151;
	// private static final int HOUND_B = 29152;
	
	private static FastList<L2PcInstance> _PlayersInside = new FastList<>();
	
	private boolean checkConditions(final L2PcInstance player)
	{
		if (debug)
		{
			if (player.isGM())
				return true;
			return false;
		}
		else if (player.getParty() == null || player.getParty().getLeader() != player || player.getParty().getCommandChannel() == null || player.getParty().getCommandChannel().getChannelLeader() != player)
		{
			player.sendMessage("Only Command Channel Leader can try to enter.");
			return false;
		}
		
		final L2CommandChannel CC = player.getParty().getCommandChannel();
		_PlayersInside.add(player);
		for (final L2Party party : CC.getPartys())
			for (final L2PcInstance member : party.getPartyMembers())
			{
				if (member == null || member.getLevel() < 75)
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(member);
					party.broadcastToPartyMembers(sm);
					return false;
				}
				_PlayersInside.add(member);
				if (_PlayersInside.size() < 36)
				{
					_PlayersInside.clear();
					member.sendMessage("The number of challenges should be 36 at least, so can not enter.");
					return false;
				}
				if (_PlayersInside.size() > 45)
				{
					_PlayersInside.clear();
					member.sendMessage("The number of challenges have been full, so can not enter.");
					return false;
				}
				if (!Util.checkIfInRange(1000, player, member, true))
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
					sm.addPcName(member);
					party.broadcastToPartyMembers(sm);
					return false;
				}
				final Long reentertime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID);
				if (System.currentTimeMillis() < reentertime)
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
					sm.addPcName(member);
					party.broadcastToPartyMembers(sm);
					return false;
				}
			}
		return true;
	}
	
	private void teleportplayer(final L2PcInstance player, final Location teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.getInstanceId());
		player.teleToLocation(teleto.getX(), teleto.getY(), teleto.getZ());
		return;
	}
	
	protected int enterInstance(final L2PcInstance player, final String template, final Location teleto)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof SoIWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
			teleto.setInstanceId(world.instanceId);
			teleportplayer(player, teleto);
			return instanceId;
		}
		if (!checkConditions(player))
			return 0;
		final L2Party party = player.getParty();
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new SoIWorld(System.currentTimeMillis() + 5400000);
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		InstanceManager.getInstance().addWorld(world);
		// teleport players
		teleto.setInstanceId(instanceId);
		if (party == null)
		{
			// this can happen only if debug is true
			InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID, System.currentTimeMillis() + INSTANCEPENALTY);
			teleportplayer(player, teleto);
			world.allowed.add(player.getObjectId());
		}
		else
			for (final L2PcInstance p : _PlayersInside)
			{
				InstanceManager.getInstance().setInstanceTime(p.getObjectId(), INSTANCEID, System.currentTimeMillis() + INSTANCEPENALTY);
				teleportplayer(p, teleto);
				world.allowed.add(p.getObjectId());
				_PlayersInside.clear();
			}
		return instanceId;
	}
	
	protected void exitInstance(final L2PcInstance player, final Location tele)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(0);
		player.teleToLocation(tele.getX(), tele.getY(), tele.getZ());
	}
	
	public SeedOfImmortality(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		_Zone = GrandBossManager.getInstance().getZone(-179539, 209312, -15499);
		addEventId(TEPIOS, QuestEventType.QUEST_START);
		addEventId(TEPIOS, QuestEventType.ON_TALK);
		addEventId(EKIMUS, QuestEventType.ON_ATTACK);
		addEventId(HOUND, QuestEventType.ON_ATTACK);
		addEventId(EKIMUS, QuestEventType.ON_KILL);
		addEventId(HOUND, QuestEventType.ON_KILL);
		// For Test Start
		addEventId(90000, QuestEventType.ON_FIRST_TALK);
		// For Test End
	}
	
	public void broadcastCamera(final L2Npc npc, final int dist, final int yaw, final int pitch, final int time, final int duration, final int turn, final int rise)
	{
		for (final L2Character cha : _Zone.getCharactersInside().values())
			if (cha instanceof L2PcInstance)
			{
				cha.abortAttack();
				cha.abortCast();
				cha.setTarget(null);
				cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				cha.sendPacket(new SpecialCamera(npc.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, 1, 0));
				final L2Summon pet = cha.getPet();
				if (pet != null)
				{
					pet.abortAttack();
					pet.abortCast();
					pet.setTarget(null);
					pet.breakAttack();
					pet.breakCast();
					pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
	}
	
	public void broadcastSocialAction(final int npc, final int socialActionId)
	{
		for (final L2Character cha : _Zone.getCharactersInside().values())
			if (cha instanceof L2PcInstance)
				cha.sendPacket(new SocialAction(npc, socialActionId));
	}
	
	public void broadcastMSU(final L2Npc npc, final int skillId, final int skillLevel)
	{
		final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		final MagicSkillUse MSU = new MagicSkillUse(npc, npc, skill.getId(), skill.getLevel(), skill.getHitTime(), 0);
		for (final L2Character cha : _Zone.getCharactersInside().values())
			if (cha instanceof L2PcInstance)
				cha.sendPacket(MSU);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		// System.out.println(event);
		if (event.equalsIgnoreCase("start"))
		{
			camera1 = addSpawn(29153, -179479, 208513, -15506, 0, false, 0);
			broadcastCamera(camera1, 0, 110, 350, 0, 15000, 0, 0);
			startQuestTimer("1", 6000, null, null);
			startQuestTimer("camera2", 14500, null, null);
			startQuestTimer("deleteNpc", 15000, camera1, null);
		}
		else if (event.equalsIgnoreCase("camera2"))
		{
			broadcastCamera(preawakened, 150, 90, 355, 0, 15000, 0, 0);
			startQuestTimer("camera3", 14500, null, null);
		}
		else if (event.equalsIgnoreCase("camera3"))
		{
			camera3 = addSpawn(29155, -179535, 208700, -15497, 0, false, 0);
			broadcastCamera(camera3, 0, 90, 0, 0, 6000, 0, 0);
			camera3.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(-179553, 209338, -15495, 0));
			for (final L2Character cha : _Zone.getCharactersInside().values())
				if (cha instanceof L2PcInstance)
					cha.sendPacket(new MoveToLocation(camera3));
			startQuestTimer("camera4", 5000, null, null);
			startQuestTimer("deleteNpc", 5500, camera3, null);
		}
		else if (event.equalsIgnoreCase("camera4"))
		{
			broadcastCamera(ekimus, 100, 120, -10, 0, 4000, 0, 0);
			startQuestTimer("camera7", 9750, null, null);
		}
		else if (event.equalsIgnoreCase("camera7"))
			broadcastCamera(ekimus, 500, 90, 0, 0, 8000, 0, 0);
		else if (event.equalsIgnoreCase("1"))
		{
			broadcastMSU(preawakened, 5786, 1);
			startQuestTimer("2", 22000, null, null);
		}
		else if (event.equalsIgnoreCase("2"))
		{
			preawakened.deleteMe();
			ekimus = addSpawn(EKIMUS, -179539, 209312, -15499, 16383, false, 0);
			ekimus.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			startQuestTimer("anim1", 100, null, null);
			startQuestTimer("3", 19000, null, null);
			startQuestTimer("h1", 9250, null, null);
		}
		else if (event.equalsIgnoreCase("anim1"))
			broadcastMSU(ekimus, 5787, 1);
		else if (event.equalsIgnoreCase("3"))
		{
			broadcastMSU(ekimus, 5788, 1);
			startQuestTimer("4", 4400, null, null);
		}
		else if (event.equalsIgnoreCase("4"))
		{
			ekimus.deleteMe();
			ekimus = addSpawn(EKIMUS, -179536, 208892, -15497, 16383, false, 0);
			ekimus.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			startQuestTimer("anim2", 50, null, null);
			startQuestTimer("spawnHounds", 1000, null, null);
		}
		else if (event.equalsIgnoreCase("anim2"))
			broadcastMSU(ekimus, 5790, 1);
		else if (event.equalsIgnoreCase("h1"))
		{
			hound1 = addSpawn(HOUND, -179281, 209138, -15496, 21663, false, 0);
			hound1.setIsNoRndWalk(true);
			broadcastCamera(hound1, 250, 120, 10, 0, 4000, 0, 0);
			startQuestTimer("h2", 3250, null, null);
			startQuestTimer("hAnim", 50, hound1, null);
			startQuestTimer("deleteNpc", 13500, hound1, null);
		}
		else if (event.equalsIgnoreCase("h2"))
		{
			hound2 = addSpawn(HOUND, -179787, 209136, -15496, 9346, false, 0);
			hound2.setIsNoRndWalk(true);
			broadcastCamera(hound2, 150, 80, 10, 0, 4000, 0, 0);
			startQuestTimer("hAnim", 50, hound2, null);
			startQuestTimer("deleteNpc", 11000, hound2, null);
		}
		else if (event.equalsIgnoreCase("hAnim"))
			broadcastMSU(npc, 5791, 1);
		else if (event.equalsIgnoreCase("deleteNpc"))
			npc.deleteMe();
		else if (event.equalsIgnoreCase("spawnHounds"))
		{
			realhound1 = addSpawn(HOUND, -178344, 209395, -15497, 27439, false, 0);
			realhound1.setIsRaidMinion(true);
			realhound1.setIsNoRndWalk(true);
			realhound1.setIsInvul(true);
			realhound2 = addSpawn(HOUND, -180695, 209387, -15497, 320, false, 0);
			realhound2.setIsRaidMinion(true);
			realhound2.setIsNoRndWalk(true);
			realhound2.setIsInvul(true);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		if (npcId == TEPIOS)
		{
			final Location tele = new Location(-242760, 219984, -9985);
			enterInstance(player, "SeedOfImmortality.xml", tele);
			return null;
		}
		return null;
	}
	
	// For Test Start
	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (npc != null && npc.getId() == 90000)
		{
			npc.deleteMe();
			preawakened = addSpawn(PREAWAKENED, -179546, 208499, -15506, 16880, false, 0);
			startQuestTimer("start", 2000, null, null);
		}
		return super.onFirstTalk(npc, player);
	}
	// For Test End
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getId() == EKIMUS)
		{
			realhound1.deleteMe();
			realhound2.deleteMe();
		}
		return null;
	}
	
	public static void main(final String[] args)
	{
		new SeedOfImmortality(-1, "SeedOfImmortality", "instances");
	}
}
