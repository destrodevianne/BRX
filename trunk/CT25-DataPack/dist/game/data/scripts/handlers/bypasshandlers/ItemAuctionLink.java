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
package handlers.bypasshandlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.IBypassHandler;
import ct25.xtreme.gameserver.instancemanager.ItemAuctionManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.itemauction.ItemAuction;
import ct25.xtreme.gameserver.model.itemauction.ItemAuctionInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ExItemAuctionInfoPacket;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

public class ItemAuctionLink implements IBypassHandler
{
	private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

	private static final String[] COMMANDS =
	{
		"ItemAuction"
	};

	@Override
	public boolean useBypass(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		if (!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_AUCTION_PERIOD));
			return true;
		}

		final ItemAuctionInstance au = ItemAuctionManager.getInstance().getManagerInstance(((L2Npc) target).getId());
		if (au == null)
			return false;

		try
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // bypass "ItemAuction"
			if (!st.hasMoreTokens())
				return false;

			final String cmd = st.nextToken();
			if ("show".equalsIgnoreCase(cmd))
			{
				if (!activeChar.getFloodProtectors().getItemAuction().tryPerformAction("RequestInfoItemAuction"))
					return false;

				if (activeChar.isItemAuctionPolling())
					return false;

				final ItemAuction currentAuction = au.getCurrentAuction();
				final ItemAuction nextAuction = au.getNextAuction();

				if (currentAuction == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_AUCTION_PERIOD));

					if (nextAuction != null) // used only once when database is empty
						activeChar.sendMessage("The next auction will begin on the " + fmt.format(new Date(nextAuction.getStartingTime())) + ".");
					return true;
				}

				activeChar.sendPacket(new ExItemAuctionInfoPacket(false, currentAuction, nextAuction));
			}
			else if ("cancel".equalsIgnoreCase(cmd))
			{
				final ItemAuction[] auctions = au.getAuctionsByBidder(activeChar.getObjectId());
				boolean returned = false;
				for (final ItemAuction auction : auctions)
					if (auction.cancelBid(activeChar))
						returned = true;
				if (!returned)
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR));
			}
			else
				return false;
		}
		catch (final Exception e)
		{
			_log.severe("Exception in: " + getClass().getSimpleName() + ":" + e.getMessage());
		}

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}