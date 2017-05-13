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
package instances.HideoutOfTheDawn;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * Hideout of the Dawn instance zone.
 * @author Adry_85\ updates by Browser
 */
public final class HideoutOfTheDawn extends Quest
{
	protected class HotDWorld extends InstanceWorld
	{
		long storeTime = 0;
	}

	private static final int INSTANCEID = 113;

	// NPCs
	private static final int WOOD = 32593;
	private static final int JAINA = 32617;

	// Coords for Tele...
	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}

	private HideoutOfTheDawn()
	{
		super(-1, HideoutOfTheDawn.class.getSimpleName(), "instances");
		addStartNpc(WOOD);
		addTalkId(WOOD, JAINA);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance talker)
	{
		switch (npc.getId())
		{
			case WOOD:
			{
				final teleCoord tele = new teleCoord();
				tele.x = -23769;
				tele.y = -8961;
				tele.z = -5392;
				enterInstance(talker, "HideoutOfTheDawn.xml", tele);
				return "32593-01.htm";
			}
			case JAINA:
			{
				final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(talker);
				world.allowed.remove(world.allowed.indexOf(talker.getObjectId()));
				final teleCoord tele = new teleCoord();
				tele.instanceId = 0;
				tele.x = 147072;
				tele.y = 23743;
				tele.z = -1984;
				exitInstance(talker, tele);
				return "32617-01.htm";
			}
		}
		return super.onTalk(npc, talker);
	}

	protected int enterInstance(final L2PcInstance player, final String template, final teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof HotDWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new HotDWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		((HotDWorld) world).storeTime = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("HideoutoftheDawn started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());
		
		return instanceId;
	}

	private void teleportplayer(final L2PcInstance player, final teleCoord teleto)
	{
		removeBuffs(player);
		if (player.getPet() != null)
			removeBuffs(player.getPet());
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}

	protected void exitInstance(final L2PcInstance player, final teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}

	private static final void removeBuffs(final L2Character ch)
	{
		ch.stopAllEffectsExceptThoseThatLastThroughDeath();
		if (ch.hasPet())
			ch.getPet().stopAllEffectsExceptThoseThatLastThroughDeath();
	}

	public static void main(final String[] args)
	{
		new HideoutOfTheDawn();
	}
}