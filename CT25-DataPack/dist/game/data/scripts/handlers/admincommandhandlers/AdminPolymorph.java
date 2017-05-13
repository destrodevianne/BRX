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
import ct25.xtreme.gameserver.instancemanager.TransformationManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.gameserver.network.serverpackets.SetupGauge;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: polymorph
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2007/07/31 10:05:56 $
 */
public class AdminPolymorph implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_polymorph",
		"admin_unpolymorph",
		"admin_polymorph_menu",
		"admin_unpolymorph_menu",
		"admin_transform",
		"admin_untransform",
		"admin_transform_menu",
		"admin_untransform_menu",
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (activeChar.isMounted())
		{
			activeChar.sendMessage("You can't transform while mounted, please dismount and try again.");
			return false;
		}

		if (command.startsWith("admin_untransform"))
		{
			final L2Object obj = activeChar.getTarget();
			if (obj instanceof L2Character)
				((L2Character) obj).stopTransformation(true);
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		else if (command.startsWith("admin_transform"))
		{
			final L2Object obj = activeChar.getTarget();
			if (obj instanceof L2PcInstance)
			{
				final L2PcInstance cha = (L2PcInstance) obj;

				final String[] parts = command.split(" ");
				if (parts.length >= 2)
					try
					{
						final int id = Integer.parseInt(parts[1]);
						if (!TransformationManager.getInstance().transformPlayer(id, cha))
							cha.sendMessage("Unknow transformation id: " + id);
					}
					catch (final NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //transform <id>");
					}
				else if (parts.length == 1)
					cha.untransform();
				else
					activeChar.sendMessage("Usage: //transform <id>");
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		if (command.startsWith("admin_polymorph"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			final L2Object target = activeChar.getTarget();
			try
			{
				st.nextToken();
				final String p1 = st.nextToken();
				if (st.hasMoreTokens())
				{
					final String p2 = st.nextToken();
					doPolymorph(activeChar, target, p2, p1);
				}
				else
					doPolymorph(activeChar, target, p1, "npc");
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //polymorph [type] <id>");
			}
		}
		else if (command.equals("admin_unpolymorph"))
			doUnpoly(activeChar, activeChar.getTarget());
		if (command.contains("_menu"))
			showMainPage(activeChar, command);

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	/**
	 * @param activeChar
	 * @param obj
	 * @param id
	 * @param type
	 */
	private void doPolymorph(final L2PcInstance activeChar, final L2Object obj, final String id, final String type)
	{
		if (obj != null)
		{
			obj.getPoly().setPolyInfo(type, id);
			// animation
			if (obj instanceof L2Character)
			{
				final L2Character Char = (L2Character) obj;
				final MagicSkillUse msk = new MagicSkillUse(Char, 1008, 1, 4000, 0);
				Char.broadcastPacket(msk);
				final SetupGauge sg = new SetupGauge(0, 4000);
				Char.sendPacket(sg);
			}
			// end of animation
			obj.decayMe();
			obj.spawnMe(obj.getX(), obj.getY(), obj.getZ());
			activeChar.sendMessage("Polymorph succeed");
		}
		else
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
	}

	/**
	 * @param activeChar
	 * @param target
	 */
	private void doUnpoly(final L2PcInstance activeChar, final L2Object target)
	{
		if (target != null)
		{
			target.getPoly().setPolyInfo(null, "1");
			target.decayMe();
			target.spawnMe(target.getX(), target.getY(), target.getZ());
			activeChar.sendMessage("Unpolymorph succeed");
		}
		else
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
	}

	private void showMainPage(final L2PcInstance activeChar, final String command)
	{
		if (command.contains("transform"))
			AdminHelpPage.showHelpPage(activeChar, "transform.htm");
		else if (command.contains("abnormal"))
			AdminHelpPage.showHelpPage(activeChar, "abnormal.htm");
		else
			AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
	}
}
