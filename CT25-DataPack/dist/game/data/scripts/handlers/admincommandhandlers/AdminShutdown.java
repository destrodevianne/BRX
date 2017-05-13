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
package handlers.admincommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.GameTimeController;
import ct25.xtreme.gameserver.Shutdown;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - server_shutdown [sec] = shows menu or shuts down server in sec seconds
 * @version $Revision: 1.5.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShutdown implements IAdminCommandHandler
{
	// private static Logger _log = Logger.getLogger(AdminShutdown.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_server_shutdown",
		"admin_server_restart",
		"admin_server_abort"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.startsWith("admin_server_shutdown"))
			try
			{
				final int val = Integer.parseInt(command.substring(22));
				serverShutdown(activeChar, val, false);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		else if (command.startsWith("admin_server_restart"))
			try
			{
				final int val = Integer.parseInt(command.substring(21));
				serverShutdown(activeChar, val, true);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		else if (command.startsWith("admin_server_abort"))
			serverAbort(activeChar);

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void sendHtmlForm(final L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final int t = GameTimeController.getInstance().getGameTime();
		final int h = t / 60;
		final int m = t % 60;
		final SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/shutdown.htm");
		adminReply.replace("%count%", String.valueOf(L2World.getInstance().getAllPlayersCount()));
		adminReply.replace("%used%", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		adminReply.replace("%xp%", String.valueOf(Config.RATE_XP));
		adminReply.replace("%sp%", String.valueOf(Config.RATE_SP));
		adminReply.replace("%adena%", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(57)));
		adminReply.replace("%drop%", String.valueOf(Config.RATE_DROP_ITEMS));
		adminReply.replace("%time%", String.valueOf(format.format(cal.getTime())));
		activeChar.sendPacket(adminReply);
	}

	private void serverShutdown(final L2PcInstance activeChar, final int seconds, final boolean restart)
	{
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}

	private void serverAbort(final L2PcInstance activeChar)
	{
		Shutdown.getInstance().abort(activeChar);
	}

}
