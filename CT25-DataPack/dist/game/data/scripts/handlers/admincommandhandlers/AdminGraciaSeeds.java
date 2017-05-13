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

import java.util.Calendar;
import java.util.StringTokenizer;

import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminGraciaSeeds implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gracia_seeds",
		"admin_kill_tiat",
		"admin_set_sodstate",
		"admin_kill_twin",
		"admin_kill_cohemenes",
		"admin_kill_ekimus",
		"admin_set_soistate"
	};

	/**
	 * @see ct25.xtreme.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, ct25.xtreme.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command

		String val = "";
		if (st.countTokens() >= 1)
			val = st.nextToken();

		// Destruction
		if (actualCommand.equalsIgnoreCase("admin_kill_tiat"))
			GraciaSeedsManager.getInstance().increaseSoDTiatKilled();
		else if (actualCommand.equalsIgnoreCase("admin_set_sodstate"))
			GraciaSeedsManager.getInstance().setSoDState(Integer.parseInt(val), true);

		// Infinity
		else if (actualCommand.equalsIgnoreCase("admin_kill_twin"))
			GraciaSeedsManager.getInstance().addTwinKill();
		else if (actualCommand.equalsIgnoreCase("admin_kill_cohemenes"))
			GraciaSeedsManager.getInstance().addCohemenesKill();
		else if (actualCommand.equalsIgnoreCase("admin_kill_ekimus"))
			GraciaSeedsManager.getInstance().addEkimusKill();
		else if (actualCommand.equalsIgnoreCase("admin_set_soistate"))
			GraciaSeedsManager.getInstance().setSoIStage(Integer.parseInt(val), true);

		showMenu(activeChar);
		return true;
	}

	private void showMenu(final L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/graciaseeds.htm");

		// Infinity
		html.replace("%soistate%", String.valueOf(GraciaSeedsManager.getInstance().getSoIState()));
		html.replace("%soitwinkill%", String.valueOf(GraciaSeedsManager.getInstance().getTwinKillCounts()));
		html.replace("%soicohemeneskill%", String.valueOf(GraciaSeedsManager.getInstance().getCohemenesKillCounts()));
		html.replace("%soiekimuskill%", String.valueOf(GraciaSeedsManager.getInstance().getEkimusKillCounts()));
		if (GraciaSeedsManager.getInstance().getSoITimeForNextStateChange() > 0)
		{
			final Calendar nextChangeDate = Calendar.getInstance();
			nextChangeDate.setTimeInMillis(System.currentTimeMillis() + GraciaSeedsManager.getInstance().getSoITimeForNextStateChange());
			html.replace("%soitime%", nextChangeDate.getTime().toString());
		}
		else
			html.replace("%soitime%", "-1");
		activeChar.sendPacket(html);

		// Destruction
		html.replace("%sodstate%", String.valueOf(GraciaSeedsManager.getInstance().getSoDState()));
		html.replace("%sodtiatkill%", String.valueOf(GraciaSeedsManager.getInstance().getSoDTiatKilled()));
		if (GraciaSeedsManager.getInstance().getSoDTimeForNextStateChange() > 0)
		{
			final Calendar nextChangeDate = Calendar.getInstance();
			nextChangeDate.setTimeInMillis(System.currentTimeMillis() + GraciaSeedsManager.getInstance().getSoDTimeForNextStateChange());
			html.replace("%sodtime%", nextChangeDate.getTime().toString());
		}
		else
			html.replace("%sodtime%", "-1");
		activeChar.sendPacket(html);
	}

	/**
	 * @see ct25.xtreme.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

}
