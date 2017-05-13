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
package instances.SecretArea;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 ** @author Gladicek Secret Area in the Keucereus Fortress (For Quest 10272 Light Fragment)
 */
public class SecretArea extends Quest
{
	
	private static final String qn = "SecretArea";
	// ID
	private static final int INSTANCE_ID = 117;
	// NPC
	private static final int GINBY = 32566;
	private static final int LELRIKIA = 32567;
	// Teleporty
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int[][] TELEPORTS =
	{
		{
			-23758,
			-8959,
			-5384
		},
		{
			-185057,
			242821,
			1576
		}
	};
	
	private void teleportPlayer(final L2PcInstance player, final int[] coords, final int instanceId)
	{
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
	}
	
	protected void enterInstance(final L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.templateId != INSTANCE_ID)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
				teleportPlayer(player, TELEPORTS[ENTER], world.instanceId);
		}
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("SecretArea.xml");
			world = new InstanceWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			world.allowed.add(player.getObjectId());
			teleportPlayer(player, TELEPORTS[ENTER], instanceId);
			_log.info("SecretArea: started instance: " + instanceId + " created by player: " + player.getName());
		}
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (npc.getId() == GINBY)
		{
			if (event.equalsIgnoreCase("enter"))
			{
				enterInstance(player);
				htmltext = "SecretAreaEnter.htm";
			}
		}
		else if (npc.getId() == LELRIKIA)
			if (event.equalsIgnoreCase("exit"))
			{
				teleportPlayer(player, TELEPORTS[EXIT], 0);
				htmltext = "SecretAreaExit.htm";
			}
		return htmltext;
	}
	
	public SecretArea(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(GINBY);
		addTalkId(GINBY);
		addTalkId(LELRIKIA);
	}
	
	public static void main(final String[] args)
	{
		new SecretArea(-1, qn, "instances");
	}
}