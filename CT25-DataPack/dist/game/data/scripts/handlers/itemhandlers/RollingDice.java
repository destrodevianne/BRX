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

import ct25.xtreme.gameserver.handler.IItemHandler;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.Dice;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Broadcast;
import ct25.xtreme.util.Rnd;

/**
 * This class ...
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:07 $
 */

public class RollingDice implements IItemHandler
{
	/**
	 * @see ct25.xtreme.gameserver.handler.IItemHandler#useItem(ct25.xtreme.gameserver.model.actor.L2Playable, ct25.xtreme.gameserver.model.L2ItemInstance, boolean)
	 */
	@Override
	public void useItem(final L2Playable playable, final L2ItemInstance item, final boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		final L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getId();

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if (itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628)
		{
			final int number = rollDice(activeChar);
			if (number == 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER));
				return;
			}

			Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), item.getId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));

			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ROLLED_S2);
			sm.addString(activeChar.getName());
			sm.addNumber(number);

			activeChar.sendPacket(sm);
			if (activeChar.isInsideZone(L2Character.ZONE_PEACE))
				Broadcast.toKnownPlayers(activeChar, sm);
			else if (activeChar.isInParty())
				activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
		}
	}

	/**
	 * @param player
	 * @return
	 */
	private int rollDice(final L2PcInstance player)
	{
		// Check if the dice is ready
		if (!player.getFloodProtectors().getRollDice().tryPerformAction("roll dice"))
			return 0;
		return Rnd.get(1, 6);
	}
}
