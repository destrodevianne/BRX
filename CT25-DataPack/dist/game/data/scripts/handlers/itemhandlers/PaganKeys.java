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
package handlers.itemhandlers;

import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.handler.IItemHandler;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * @author chris
 */
public class PaganKeys implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;

	/**
	 * @see ct25.xtreme.gameserver.handler.IItemHandler#useItem(ct25.xtreme.gameserver.model.actor.L2Playable, ct25.xtreme.gameserver.model.L2ItemInstance, boolean)
	 */
	@Override
	public void useItem(final L2Playable playable, final L2ItemInstance item, final boolean forceUse)
	{
		final int itemId = item.getId();
		if (!(playable instanceof L2PcInstance))
			return;
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final L2Object target = activeChar.getTarget();

		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final L2DoorInstance door = (L2DoorInstance) target;

		if (!activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false))
		{
			activeChar.sendMessage("Too far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
		{
			activeChar.sendMessage("You cannot use the key now.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return;

		switch (itemId)
		{
			case 9698:
				if (door.getId() == 24220020)
				{
					if (activeChar.getInstanceId() != door.getInstanceId())
					{
						for (final L2DoorInstance instanceDoor : InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoors())
							if (instanceDoor.getId() == door.getId())
								instanceDoor.openMe();
					}
					else
						door.openMe();
				}
				else
					activeChar.sendMessage("Incorrect Door.");
				break;
			case 9699:
				if (door.getId() == 24220022)
				{
					if (activeChar.getInstanceId() != door.getInstanceId())
					{
						for (final L2DoorInstance instanceDoor : InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoors())
							if (instanceDoor.getId() == door.getId())
								instanceDoor.openMe();
					}
					else
						door.openMe();
				}
				else
					activeChar.sendMessage("Incorrect Door.");
				break;
			case 8056:
				if (door.getId() == 23150004 || door.getId() == 23150003)
				{
					DoorTable.getInstance().getDoor(23150003).openMe();
					DoorTable.getInstance().getDoor(23150003).onOpen();
					DoorTable.getInstance().getDoor(23150004).openMe();
					DoorTable.getInstance().getDoor(23150004).onOpen();
				}
				else
					activeChar.sendMessage("Incorrect Door.");
				break;
		}
	}
}
