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
package hellbound.RankuFloor;

import java.util.Calendar;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Util;

public class RankuFloor extends Quest
{
	
	private class RWorld extends InstanceWorld
	{
		public RWorld()
		{
			super();
		}
	}
	
	// Misc
	private static final String qn = "RankuFloor";
	private static final int INSTANCEID = 143; // this is the client number
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	
	// NPCs
	private static final int GK_9 = 32752;
	private static final int CUBE = 32374;
	private static final int RANKU = 25542;

	// Item
	private static final int SEAL_BREAKER_10 = 15516;
	
	// Teleport Locs
	private static final int[] ENTRY_POINT =
	{
		-19008,
		277024,
		-15000
	};
	private static final int[] EXIT_POINT =
	{
		-19008,
		277122,
		-13376
	};
	
	public RankuFloor(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(GK_9, CUBE);
		addTalkId(GK_9, CUBE);
		addKillId(RANKU);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = null;
		
		if (npc.getId() == GK_9)
		{
			htmltext = checkConditions(player);
			
			if (htmltext == null)
				enterInstance(player, "Ranku.xml");
		}
		
		else if (npc.getId() == CUBE)
		{
			final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (world != null && world instanceof RWorld)
			{
				world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
				teleportPlayer(player, EXIT_POINT, 0);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int instanceId = npc.getInstanceId();
		if (instanceId > 0)
		{
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			inst.setSpawnLoc(EXIT_POINT);
			
			// Terminate instance in 10 min
			if (inst.getInstanceEndTime() - System.currentTimeMillis() > 600000)
				inst.setDuration(600000);
			
			inst.setEmptyDestroyTime(0);
			
			if (world != null && world instanceof RWorld)
				setReenterTime(world);
			
			addSpawn(CUBE, -19056, 278732, -15000, 0, false, 0, false, instanceId);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	private String checkConditions(final L2PcInstance player)
	{
		if (player.getParty() == null)
			return "gk-noparty.htm";

		else if (player.getParty().getPartyLeaderOID() != player.getObjectId())
			return "gk-noleader.htm";
		
		return null;
	}
	
	private boolean checkTeleport(final L2PcInstance player)
	{
		final L2Party party = player.getParty();

		if (party == null)
			return false;
		
		if (player.getObjectId() != party.getPartyLeaderOID())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}

		for (final L2PcInstance partyMember : party.getPartyMembers())
		{
			if (partyMember.getLevel() < 78)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}

			if (!Util.checkIfInRange(500, player, partyMember, true))
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}

			if (InstanceManager.getInstance().getPlayerWorld(player) != null)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
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
			
			if (partyMember.getInventory().getInventoryItemCount(SEAL_BREAKER_10, -1, false) < 1)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
		}

		return true;
	}
	
	private int enterInstance(final L2PcInstance player, final String template)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof RWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, ENTRY_POINT, world.instanceId);
			return world.instanceId;
		}
		if (!checkTeleport(player))
			return 0;

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new RWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		_log.info("Tower of Infinitum - Ranku floor started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		
		for (final L2PcInstance partyMember : player.getParty().getPartyMembers())
		{
			teleportPlayer(partyMember, ENTRY_POINT, instanceId);
			partyMember.destroyItemByItemId("Quest", SEAL_BREAKER_10, 1, null, true);
			world.allowed.add(partyMember.getObjectId());
		}
		
		return instanceId;
	}
	
	public void setReenterTime(final InstanceWorld world)
	{
		if (world instanceof RWorld)
		{

			// Reenter time should be cleared every Wed and Sat at 6:30 AM, so we set next suitable

			Calendar reenter;
			final Calendar now = Calendar.getInstance();
			final Calendar reenterPointWed = (Calendar) now.clone();
			reenterPointWed.set(Calendar.AM_PM, Calendar.AM);
			reenterPointWed.set(Calendar.MINUTE, RESET_MIN);
			reenterPointWed.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
			reenterPointWed.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
			final Calendar reenterPointSat = (Calendar) reenterPointWed.clone();
			reenterPointSat.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			
			if (now.after(reenterPointSat))
			{
				reenterPointWed.add(Calendar.WEEK_OF_MONTH, 1);
				reenter = (Calendar) reenterPointWed.clone();
			}
			else
				reenter = (Calendar) reenterPointSat.clone();
			
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
			sm.addString(InstanceManager.getInstance().getInstanceIdName(world.templateId));
			// set instance reenter time for all allowed players
			for (final int objectId : world.allowed)
			{
				final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
				if (player != null && player.isOnline())
				{
					InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, reenter.getTimeInMillis());
					player.sendPacket(sm);
				}
			}
		}
	}

	private void teleportPlayer(final L2PcInstance player, final int[] tele, final int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(tele[0], tele[1], tele[2], true);
	}
	
	public static void main(final String[] args)
	{
		// now call the constructor (starts up the)
		new RankuFloor(-1, qn, "hellbound");
	}
}
