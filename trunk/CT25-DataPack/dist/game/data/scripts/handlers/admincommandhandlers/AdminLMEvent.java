/**
 *
 */
package handlers.admincommandhandlers;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.event.LMEvent;
import ct25.xtreme.gameserver.model.entity.event.LMEventTeleporter;
import ct25.xtreme.gameserver.model.entity.event.LMManager;

/**
 * @author L0ngh0rn
 */
public class AdminLMEvent implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_lm_add",
		"admin_lm_remove",
		"admin_lm_advance"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("admin_lm_add"))
		{
			final L2Object target = activeChar.getTarget();

			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			add(activeChar, (L2PcInstance) target);
		}
		else if (command.equals("admin_lm_remove"))
		{
			final L2Object target = activeChar.getTarget();

			if (!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			remove(activeChar, (L2PcInstance) target);
		}
		else if (command.equals("admin_lm_advance"))
			LMManager.getInstance().skipDelay();

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void add(final L2PcInstance activeChar, final L2PcInstance playerInstance)
	{
		if (LMEvent.isPlayerParticipant(playerInstance))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}

		if (!LMEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}

		if (LMEvent.isStarted())
			new LMEventTeleporter(playerInstance, true, false);
	}

	private void remove(final L2PcInstance activeChar, final L2PcInstance playerInstance)
	{
		if (!LMEvent.removeParticipant(playerInstance))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}

		new LMEventTeleporter(playerInstance, Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}
