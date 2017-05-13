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

import java.util.StringTokenizer;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.TradeController;
import ct25.xtreme.gameserver.handler.IBypassHandler;
import ct25.xtreme.gameserver.model.L2TradeList;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct25.xtreme.gameserver.network.serverpackets.ShopPreviewList;

public class Wear implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Wear"
	};

	@Override
	public boolean useBypass(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		if (!Config.ALLOW_WEAR)
			return false;

		try
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();

			if (st.countTokens() < 1)
				return false;

			showWearWindow(activeChar, Integer.parseInt(st.nextToken()));
			return true;
		}
		catch (final Exception e)
		{
			_log.info("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	private static final void showWearWindow(final L2PcInstance player, final int val)
	{
		player.tempInventoryDisable();

		if (Config.DEBUG)
			_log.fine("Showing wearlist");

		final L2TradeList list = TradeController.getInstance().getBuyList(val);

		if (list != null)
		{
			final ShopPreviewList bl = new ShopPreviewList(list, player.getAdena(), player.getExpertiseLevel());
			player.sendPacket(bl);
		}
		else
		{
			_log.warning("no buylist with id:" + val);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}