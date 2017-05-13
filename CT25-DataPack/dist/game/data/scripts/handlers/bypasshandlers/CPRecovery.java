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
package handlers.bypasshandlers;

import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.handler.IBypassHandler;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;

public class CPRecovery implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"CPRecovery"
	};

	@Override
	public boolean useBypass(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		final L2Npc npc = (L2Npc) target;

		if (npc.getId() != 31225 && npc.getId() != 31226)
			return false;

		if (activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("Go away, you're not welcome here.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}

		if (!activeChar.reduceAdena("RestoreCP", 100, activeChar.getLastFolkNPC(), true))
			return false;

		final L2Skill skill = SkillTable.getInstance().getInfo(4380, 1);
		if (skill != null)
		{
			npc.setTarget(activeChar);
			npc.doCast(skill);
		}
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}