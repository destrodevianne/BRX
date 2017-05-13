/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_stats",
		"admin_skill_test",
		"admin_known"
	};

	/*
	 * (non-Javadoc)
	 * @see ct25.xtreme.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, ct25.xtreme.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.equals("admin_stats"))
			for (final String line : ThreadPoolManager.getInstance().getStats())
				activeChar.sendMessage(line);
		else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				final int id = Integer.parseInt(st.nextToken());
				if (command.startsWith("admin_skill_test"))
					adminTestSkill(activeChar, id, true);
				else
					adminTestSkill(activeChar, id, false);
			}
			catch (final NumberFormatException e)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
			catch (final NoSuchElementException nsee)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		else if (command.equals("admin_known on"))
			Config.CHECK_KNOWN = true;
		else if (command.equals("admin_known off"))
			Config.CHECK_KNOWN = false;
		return true;
	}

	/**
	 * @param activeChar
	 * @param id
	 * @param msu 
	 */
	private void adminTestSkill(final L2PcInstance activeChar, final int id, final boolean msu)
	{
		L2Character caster;
		final L2Object target = activeChar.getTarget();
		if (!(target instanceof L2Character))
			caster = activeChar;
		else
			caster = (L2Character) target;
		
		final L2Skill _skill = SkillTable.getInstance().getInfo(id, 1);
		if (_skill != null)
		{
			caster.setTarget(activeChar);
			if (msu)
				caster.broadcastPacket(new MagicSkillUse(caster, activeChar, id, 1, _skill.getHitTime(), _skill.getReuseDelay()));
			else
				caster.doCast(_skill);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ct25.xtreme.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

}
