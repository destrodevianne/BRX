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

import ct25.xtreme.gameserver.handler.IBypassHandler;
import ct25.xtreme.gameserver.instancemanager.SiegeManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct25.xtreme.gameserver.network.serverpackets.ItemList;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

public class Observation implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"observesiege",
		"observeoracle",
		"observe"
	};

	@Override
	public boolean useBypass(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		try
		{
			if (command.toLowerCase().startsWith(COMMANDS[0])) // siege
			{
				final String val = command.substring(13);
				final StringTokenizer st = new StringTokenizer(val);
				st.nextToken(); // Bypass cost

				if (SiegeManager.getInstance().getSiege(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())) != null)
					doObserve(activeChar, (L2Npc) target, val);
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_VIEW_SIEGE));
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1])) // oracle
			{
				final String val = command.substring(13);
				final StringTokenizer st = new StringTokenizer(val);
				st.nextToken(); // Bypass cost
				doObserve(activeChar, (L2Npc) target, val);
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[2])) // observe
			{
				doObserve(activeChar, (L2Npc) target, command.substring(8));
				return true;
			}

			return false;
		}
		catch (final Exception e)
		{
			_log.info("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	private static final void doObserve(final L2PcInstance player, final L2Npc npc, final String val)
	{
		final StringTokenizer st = new StringTokenizer(val);
		final long cost = Long.parseLong(st.nextToken());
		final int x = Integer.parseInt(st.nextToken());
		final int y = Integer.parseInt(st.nextToken());
		final int z = Integer.parseInt(st.nextToken());

		if (player.reduceAdena("Broadcast", cost, npc, true))
		{
			// enter mode
			player.enterObserverMode(x, y, z);
			player.sendPacket(new ItemList(player, false));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}