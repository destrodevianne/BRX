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

import java.util.Collection;
import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - heal = restores HP/MP/CP on target, name or radius
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $ Small typo fix by Zoey76 24/02/2011
 */
public class AdminHeal implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminRes.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_heal"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.equals("admin_heal"))
			handleHeal(activeChar);
		else if (command.startsWith("admin_heal"))
			try
			{
				final String healTarget = command.substring(11);
				handleHeal(activeChar, healTarget);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.DEVELOPER)
					_log.warning("Heal error: " + e);
				activeChar.sendMessage("Incorrect target/radius specified.");
			}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleHeal(final L2PcInstance activeChar)
	{
		handleHeal(activeChar, null);
	}

	private void handleHeal(final L2PcInstance activeChar, final String player)
	{

		L2Object obj = activeChar.getTarget();
		if (player != null)
		{
			final L2PcInstance plyr = L2World.getInstance().getPlayer(player);

			if (plyr != null)
				obj = plyr;
			else
				try
				{
					final int radius = Integer.parseInt(player);
					final Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
					// synchronized (activeChar.getKnownList().getKnownObjects())
					{
						for (final L2Object object : objs)
							if (object instanceof L2Character)
							{
								final L2Character character = (L2Character) object;
								character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
								if (object instanceof L2PcInstance)
									character.setCurrentCp(character.getMaxCp());
							}
					}
					activeChar.sendMessage("Healed within " + radius + " unit radius.");
					return;
				}
				catch (final NumberFormatException nbe)
				{
				}
		}
		if (obj == null)
			obj = activeChar;
		if (obj instanceof L2Character)
		{
			final L2Character target = (L2Character) obj;
			target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
			if (target instanceof L2PcInstance)
				target.setCurrentCp(target.getMaxCp());
			if (Config.DEBUG)
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") healed character " + target.getName());
		}
		else
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
	}
}
