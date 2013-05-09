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
package ai.individual.grandboss;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.MapRegionTable;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2GrandBossInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ExShowScreenMessage;
import ct25.xtreme.gameserver.network.serverpackets.SocialAction;
import ct25.xtreme.gameserver.network.serverpackets.SpecialCamera;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.templates.StatsSet;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;

public class Sailren extends L2AttackableAIScript
{
	// NPCs
	private static final int MANAGER = 32109;
	private static final int CUBE = 32107;
	
	// BOSS
	private static final int SAILREN = 29065;
	
	// MOBS
	private static final int VELOCIRAPTOR = 22196; 
	private static final int PTEROSAUR = 22199; 
	private static final int TYRANNOSAURUS = 22217; 
	
	// LOCS
	private static final int SAILREN_X = 27333;
	private static final int SAILREN_Y = -6835;
	private static final int SAILREN_Z = -1970;
	
	private static final int MOBS_X = 27213;
	private static final int MOBS_Y = -6539;
	private static final int MOBS_Z = -1976;
	
	private static final int SPAWN_X = 27190;
	private static final int SPAWN_Y = -7163;
	private static final int SPAWN_Z = -1968;
	
	// REQUERIMENTS 
	private static final int REQUIRED_ITEM = 8784;
	private static final int MIN_PLAYERS = 2;
	private static final int MAX_PLAYERS = 9;
	private static final int MIN_LEVEL = 75;
	
	//SAILREN Status Tracking :
	private static final byte DORMANT = 0; 
	private static final byte WAITING = 1;
	private static final byte FIGHTING = 2; 
	private static final byte DEAD = 3; 
	
	// Task
	protected ScheduledFuture<?> _activityCheckTask = null;
	protected long _LastAction = 0;
	private static final int INACTIVITYTIME = 900000;
	
	private List<L2Npc> velos;
	private List<L2Npc> ptero;
	private List<L2Npc> trex;
	private List<L2Npc> sailren;
	
	private static int[] MOBS = { SAILREN, VELOCIRAPTOR, PTEROSAUR, TYRANNOSAURUS };
	
	public Sailren(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MANAGER);
		addTalkId(MANAGER);
		addFirstTalkId(CUBE);
		for (int mob : MOBS)
		{
			addKillId(mob);
			addAttackId(mob);
		}
		
		StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
		int status = GrandBossManager.getInstance().getBossStatus(SAILREN);
		if (status == DEAD)
		{
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
				startQuestTimer("SAILREN_unlock", temp, null, null);
			else
				GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		}
		else if (status != DORMANT)
			GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		
		if (player.getInventory().getItemByItemId(REQUIRED_ITEM) == null)
		{
			player.sendMessage("You dont have required item.");
			return false;
		}
		
		L2Party party = player.getParty();
		
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		else if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		else if (party.getMemberCount() < MIN_PLAYERS || party.getMemberCount() > MAX_PLAYERS)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
			return false;
		}
		
		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			
			if (partyMember == null)
				continue;
			
			else if (partyMember.getLevel() < MIN_LEVEL)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			
			else if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("start"))
		{
			GrandBossManager.getInstance().setBossStatus(SAILREN,FIGHTING);
			
			velos = new FastList<L2Npc>();
			ptero = new FastList<L2Npc>();
			trex = new FastList<L2Npc>();
			sailren = new FastList<L2Npc>();
			int x, y;
			L2Npc temp;
			for (int i = 0; i < 3; i++)
			{
				x = SAILREN_X + Rnd.get(500);
				y = SAILREN_Y + Rnd.get(500);
				temp = this.addSpawn(VELOCIRAPTOR, x, y, MOBS_Z, 0, false, 0);
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setRunning();
				velos.add(temp);
			}
		}
		else if (event.equalsIgnoreCase("spawn"))
		{
			L2GrandBossInstance Sailren = (L2GrandBossInstance) addSpawn(SAILREN, SAILREN_X, SAILREN_Y, SAILREN_Z, 27306, false, 0);
			GrandBossManager.getInstance().addBoss(Sailren);
			sailren.add(Sailren);
			startQuestTimer("entry",0, Sailren, null);	
		}
		else if (event.equalsIgnoreCase("entry"))
		{
			npc.setRunning();
			npc.setIsInvul(true);
			npc.setIsParalyzed(true);
			npc.setIsImmobilized(true);
			startQuestTimer("camera_1", 2000, npc, null);
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(),300,0,32,2000,11000,0,0,1,0));
		}
		else if (event.equalsIgnoreCase("action_1"))
		{
			npc.broadcastPacket(new SocialAction(npc.getObjectId(),2));
			startQuestTimer("camera_6", 2500, npc, null);
		}
		else if (event.equalsIgnoreCase("camera_1"))
		{
			npc.setTarget(npc);
			npc.setIsParalyzed(true);
			startQuestTimer("camera_2", 4000, npc, null);
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(),300,90,24,4000,11000,0,0,1,0));
		}
		else if (event.equalsIgnoreCase("camera_2"))
		{
			npc.setTarget(npc);
			npc.setIsParalyzed(false);
			npc.doCast(SkillTable.getInstance().getInfo(5122,1));
			npc.setIsParalyzed(true);
			startQuestTimer("camera_3", 4000, npc, null);
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(),300,160,16,4000,11000,0,0,1,0));
		}
		else if (event.equalsIgnoreCase("camera_3"))
		{
			npc.setTarget(npc);
			npc.setIsParalyzed(false);
			npc.doCast(SkillTable.getInstance().getInfo(5118,1));
			npc.setIsParalyzed(true);
			startQuestTimer("camera_4", 4000, npc, null);
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(),300,250,8,4000,11000,0,0,1,0));
		}
		else if (event.equalsIgnoreCase("camera_4"))
		{
			npc.setTarget(npc);
			npc.setIsParalyzed(true);
			startQuestTimer("camera_5", 4000, npc, null);
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(),300,340,0,4000,11000,0,0,1,0));
		}
		else if (event.equalsIgnoreCase("camera_5"))
		{
			npc.broadcastPacket(new SocialAction(npc.getObjectId(),2));
			startQuestTimer("camera_6", 5000, npc, null);
		}
		else if (event.equalsIgnoreCase("camera_6"))
		{
			npc.setIsInvul(false);
			npc.setIsParalyzed(false);
			npc.setIsImmobilized(false);
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			if (npc != null)
				npc.deleteMe();
		}
		else if (event.equalsIgnoreCase("Sailren_unlock"))
		{
			GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		_LastAction = System.currentTimeMillis();
		
		return null;
	}
	
	private class CheckActivity implements Runnable
	{
		public void run()
		{
			long temp = (System.currentTimeMillis() - _LastAction);
			if (temp > INACTIVITYTIME)
			{
				GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
				if (velos != null)
				{
					for (L2Npc npc : velos)
						npc.deleteMe();
					velos.clear();
				}
				
				if (ptero != null)
				{
					for (L2Npc npc : ptero)
						npc.deleteMe();
					ptero.clear();
				}

				if (trex != null)
				{
					for (L2Npc npc : trex)
						npc.deleteMe();
					trex.clear();
				}

				if (sailren != null)
				{
					for (L2Npc npc : sailren)
						npc.deleteMe();
					sailren.clear();
				}
				_activityCheckTask.cancel(false);
			}
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		if (npc.getNpcId() == MANAGER)
		{
			if (GrandBossManager.getInstance().getBossStatus(SAILREN) == DORMANT || GrandBossManager.getInstance().getBossStatus(SAILREN) == WAITING)
			{
				if (!checkConditions(player))
					return htmltext;
				else
				{
					if (player.getQuestState("Sailren").hasQuestItems(REQUIRED_ITEM));
					{
						player.getQuestState("Sailren").takeItems(REQUIRED_ITEM,1);
					}
					for (L2PcInstance member : player.getParty().getPartyMembers())
					{
						if (member != null) // impossible NPE?
						{
							member.teleToLocation(SPAWN_X + Rnd.get(150), SPAWN_Y + Rnd.get(150), SPAWN_Z + Rnd.get(150), true);
							member.sendPacket(new ExShowScreenMessage(1,0,5,0,1,0,0,false,10000,1,"Sailren: Welcome to my Nest... Now all of will die..."));
							if (member.getPet() != null)
								member.getPet().teleToLocation(SPAWN_X + Rnd.get(50), SPAWN_Y + Rnd.get(50), SPAWN_Z, true);
						}
					}
					_LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
					
					if (GrandBossManager.getInstance().getBossStatus(SAILREN) == DORMANT)
					startQuestTimer("start", 60000, npc, player);
					GrandBossManager.getInstance().setBossStatus(SAILREN,WAITING);
					
				}
			}
			else if (GrandBossManager.getInstance().getBossStatus(SAILREN) == FIGHTING)
			{
				htmltext = "<html><body>Stone Statue of Shilen:<br><font color=\"LEVEL\">Sailren Lair is now full. </font></body></html>";
			}
			else
				htmltext = "<html><body>Stone Statue of Shilen:<br><font color=\"LEVEL\">Sailren is dead. Come back later.</font></body></html>";
		}
		else if (npc.getNpcId() == CUBE)
		{
			if (player != null)
			{
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				if (player.getPet() != null)
					player.getPet().teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == CUBE)
		{
			if (player != null)
			{
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				if (player.getPet() != null)
					player.getPet().teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == SAILREN)
		{
			_activityCheckTask.cancel(false);
			GrandBossManager.getInstance().setBossStatus(SAILREN, DEAD);
			sailren.remove(npc);
			long respawnTime = (long) Config.Interval_Of_Sailren_Spawn + Rnd.get(Config.Random_Of_Sailren_Spawn);
			startQuestTimer("Sailren_unlock", respawnTime, npc, null);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
			info.set("respawn_time", (System.currentTimeMillis()) + respawnTime);
			GrandBossManager.getInstance().setStatsSet(SAILREN, info);
			L2Npc cube = addSpawn(CUBE, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			startQuestTimer("despawn", 300000, cube, null);
		}
		else if (npc.getNpcId() == VELOCIRAPTOR)
		{
			if (velos == null)
				return "";
			velos.remove(npc);
			L2PcInstance target = (L2PcInstance) npc.getTarget();
			npc.deleteMe();
			if (velos.isEmpty())
			{
				velos.clear();
				velos = null;
				L2Npc temp = this.addSpawn(PTEROSAUR, MOBS_X + Rnd.get(500), MOBS_Y + Rnd.get(500), MOBS_Z, 0, false, 0);
				ptero.add(temp);
				temp.setTarget(target);
				temp.setRunning();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		else if (npc.getNpcId() == PTEROSAUR)
		{
			if (ptero == null)
				return "";
			ptero.remove(npc);
			L2PcInstance target = (L2PcInstance) npc.getTarget();
			npc.deleteMe();
			if (ptero.isEmpty())
			{
				ptero.clear();
				ptero = null;
				L2Npc temp = this.addSpawn(TYRANNOSAURUS, MOBS_X + Rnd.get(500), MOBS_Y + Rnd.get(500), MOBS_Z, 0, false, 0);
				trex.add(temp);
				temp.setTarget(target);
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				temp.setRunning();
			}
		}
		else if (npc.getNpcId() == TYRANNOSAURUS)
		{
			if (trex == null)
				return "";
			trex.remove(npc);
			trex.clear();
			trex = null;
			npc.deleteMe();
			this.startQuestTimer("spawn", 60000, null, null);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Sailren(-1, Sailren.class.getSimpleName(), "ai/individual/grandboss");
	}
}
