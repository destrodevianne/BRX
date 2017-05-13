/**
 *
 */
package handlers.admincommandhandlers;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.event.DMEvent;
import ct25.xtreme.gameserver.model.entity.event.DMEventTeleporter;
import ct25.xtreme.gameserver.model.entity.event.DMManager;

/**
 * @author L0ngh0rn
 */
public class AdminDMEvent implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_dm_add",
		"admin_dm_remove",
		"admin_dm_advance"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_dm_add"))
		{
			final L2Object target = activeChar.getTarget();

			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			add(activeChar, (L2PcInstance) target);
		}
		else if (command.equals("admin_dm_remove"))
		{
			final L2Object target = activeChar.getTarget();

			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			remove(activeChar, (L2PcInstance) target);
		}
		else if (command.equals("admin_dm_advance"))
			DMManager.getInstance().skipDelay();

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void add(final L2PcInstance activeChar, final L2PcInstance playerInstance)
	{
		if (DMEvent.isPlayerParticipant(playerInstance))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}

		if (!DMEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}

		if (DMEvent.isStarted())
			new DMEventTeleporter(playerInstance, true, false);
	}

	private void remove(final L2PcInstance activeChar, final L2PcInstance playerInstance)
	{
		if (!DMEvent.removeParticipant(playerInstance))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}

		new DMEventTeleporter(playerInstance, Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}
