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

import java.util.StringTokenizer;

import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;

/**
 * @author evill33t, GodKratos
 */
public class AdminInstance implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_setinstance",
		"admin_ghoston",
		"admin_ghostoff",
		"admin_createinstance",
		"admin_destroyinstance",
		"admin_listinstances"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		// create new instance
		if (command.startsWith("admin_createinstance"))
		{
			final String[] parts = command.split(" ");
			if (parts.length < 2)
				activeChar.sendMessage("Example: //createinstance <id> <templatefile> - ids => 300000 are reserved for dynamic instances");
			else
				try
				{
					final int id = Integer.parseInt(parts[1]);
					if (InstanceManager.getInstance().createInstanceFromTemplate(id, parts[2]) && id < 300000)
					{
						activeChar.sendMessage("Instance created");
						return true;
					}
					activeChar.sendMessage("Failed to create instance");
					return true;
				}
				catch (final Exception e)
				{
					activeChar.sendMessage("Failed loading: " + parts[2]);
					return false;
				}
		}
		else if (command.startsWith("admin_listinstances"))
			for (final Instance temp : InstanceManager.getInstance().getInstances().values())
				activeChar.sendMessage("Id: " + temp.getId() + " Name: " + temp.getName());
		else if (command.startsWith("admin_setinstance"))
			try
			{
				final int val = Integer.parseInt(st.nextToken());
				if (InstanceManager.getInstance().getInstance(val) == null)
				{
					activeChar.sendMessage("Instance " + val + " doesnt exist.");
					return false;
				}
				final L2Object target = activeChar.getTarget();
				if (target == null || target instanceof L2Summon) // Don't separate summons from masters
				{
					activeChar.sendMessage("Incorrect target.");
					return false;
				}
				target.setInstanceId(val);
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.sendMessage("Admin set your instance to:" + val);
					player.teleToLocation(player.getX(), player.getY(), player.getZ());
					final L2Summon pet = player.getPet();
					if (pet != null)
					{
						pet.setInstanceId(val);
						pet.teleToLocation(pet.getX(), pet.getY(), pet.getZ());
						player.sendMessage("Admin set " + pet.getName() + "'s instance to:" + val);
					}
				}
				activeChar.sendMessage("Moved " + target.getName() + " to instance " + target.getInstanceId() + ".");
				return true;
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Use //setinstance id");
			}
		else if (command.startsWith("admin_destroyinstance"))
			try
			{
				final int val = Integer.parseInt(st.nextToken());
				InstanceManager.getInstance().destroyInstance(val);
				activeChar.sendMessage("Instance destroyed");
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Use //destroyinstance id");
			}
		else if (command.startsWith("admin_ghoston"))
		{
			activeChar.getAppearance().setGhostMode(true);
			activeChar.sendMessage("Ghost mode enabled");
			activeChar.broadcastUserInfo();
			activeChar.decayMe();
			activeChar.spawnMe();
		}
		// ghost mode off
		else if (command.startsWith("admin_ghostoff"))
		{
			activeChar.getAppearance().setGhostMode(false);
			activeChar.sendMessage("Ghost mode disabled");
			activeChar.broadcastUserInfo();
			activeChar.decayMe();
			activeChar.spawnMe();
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}