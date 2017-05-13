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

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.TvTEvent;
import ct25.xtreme.gameserver.model.entity.TvTEventTeleporter;
import ct25.xtreme.gameserver.model.entity.TvTManager;

/**
 * @author FBIagent
 */
public class AdminTvTEvent implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_tvt_add",
		"admin_tvt_remove",
		"admin_tvt_advance"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.equals("admin_tvt_add"))
		{
			final L2Object target = activeChar.getTarget();

			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			add(activeChar, (L2PcInstance) target);
		}
		else if (command.equals("admin_tvt_remove"))
		{
			final L2Object target = activeChar.getTarget();

			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			remove(activeChar, (L2PcInstance) target);
		}
		else if (command.equals("admin_tvt_advance"))
			TvTManager.getInstance().skipDelay();

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void add(final L2PcInstance activeChar, final L2PcInstance playerInstance)
	{
		if (TvTEvent.isPlayerParticipant(playerInstance.getObjectId()))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}

		if (!TvTEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}

		if (TvTEvent.isStarted())
			new TvTEventTeleporter(playerInstance, TvTEvent.getParticipantTeamCoordinates(playerInstance.getObjectId()), true, false);
	}

	private void remove(final L2PcInstance activeChar, final L2PcInstance playerInstance)
	{
		if (!TvTEvent.removeParticipant(playerInstance.getObjectId()))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}

		new TvTEventTeleporter(playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}
