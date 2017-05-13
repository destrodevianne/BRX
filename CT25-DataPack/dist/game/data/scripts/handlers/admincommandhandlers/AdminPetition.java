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

import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.instancemanager.PetitionManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles commands for GMs to respond to petitions.
 * @author Tempy
 */
public class AdminPetition implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_view_petitions",
		"admin_view_petition",
		"admin_accept_petition",
		"admin_reject_petition",
		"admin_reset_petitions",
		"admin_force_peti"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		int petitionId = -1;

		try
		{
			petitionId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
		}

		if (command.equals("admin_view_petitions"))
			PetitionManager.getInstance().sendPendingPetitionList(activeChar);
		else if (command.startsWith("admin_view_petition"))
			PetitionManager.getInstance().viewPetition(activeChar, petitionId);
		else if (command.startsWith("admin_accept_petition"))
		{
			if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME));
				return true;
			}

			if (PetitionManager.getInstance().isPetitionInProcess(petitionId))
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
				return true;
			}

			if (!PetitionManager.getInstance().acceptPetition(activeChar, petitionId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION));
		}
		else if (command.startsWith("admin_reject_petition"))
		{
			if (!PetitionManager.getInstance().rejectPetition(activeChar, petitionId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER));
			PetitionManager.getInstance().sendPendingPetitionList(activeChar);
		}
		else if (command.equals("admin_reset_petitions"))
		{
			if (PetitionManager.getInstance().isPetitionInProcess())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
				return false;
			}
			PetitionManager.getInstance().clearPendingPetitions();
			PetitionManager.getInstance().sendPendingPetitionList(activeChar);
		}
		else if (command.startsWith("admin_force_peti"))
			try
			{
				final L2Object targetChar = activeChar.getTarget();
				if (targetChar == null || !(targetChar instanceof L2PcInstance))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return false;
				}
				final L2PcInstance targetPlayer = (L2PcInstance) targetChar;

				final String val = command.substring(15);

				petitionId = PetitionManager.getInstance().submitPetition(targetPlayer, val, 9);
				PetitionManager.getInstance().acceptPetition(activeChar, petitionId);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //force_peti text");
				return false;
			}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
