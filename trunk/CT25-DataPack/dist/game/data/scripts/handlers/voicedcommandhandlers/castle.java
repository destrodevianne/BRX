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
package handlers.voicedcommandhandlers;

import ct25.xtreme.gameserver.handler.IVoicedCommandHandler;
import ct25.xtreme.gameserver.instancemanager.CastleManager;
import ct25.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Castle;

/**
 *
 *
 */
public class castle implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"open doors",
		"close doors",
		"ride wyvern"
	};

	/**
	 * @see ct25.xtreme.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, ct25.xtreme.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String params)
	{
		if (command.startsWith("open doors") && params.equals("castle") && activeChar.isClanLeader())
		{
			final L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
			final Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
			if (door == null || castle == null)
				return false;
			if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
				door.openMe();

		}
		else if (command.startsWith("close doors") && params.equals("castle") && activeChar.isClanLeader())
		{
			final L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
			final Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().getHasCastle());
			if (door == null || castle == null)
				return false;
			if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
				door.closeMe();

		}
		else if (command.startsWith("ride wyvern") && params.equals("castle"))
			if (activeChar.getClan().getHasCastle() > 0 && activeChar.isClanLeader())
				activeChar.mount(12621, 0, true);
		return true;
	}

	/**
	 * @see ct25.xtreme.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
