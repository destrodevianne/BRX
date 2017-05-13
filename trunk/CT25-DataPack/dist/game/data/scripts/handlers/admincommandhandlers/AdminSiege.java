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

import java.util.Calendar;
import java.util.StringTokenizer;

import ct25.xtreme.gameserver.SevenSigns;
import ct25.xtreme.gameserver.datatables.ClanTable;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.instancemanager.AuctionManager;
import ct25.xtreme.gameserver.instancemanager.CastleManager;
import ct25.xtreme.gameserver.instancemanager.ClanHallManager;
import ct25.xtreme.gameserver.model.L2Clan;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Castle;
import ct25.xtreme.gameserver.model.entity.ClanHall;
import ct25.xtreme.gameserver.model.zone.type.L2ClanHallZone;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.util.StringUtil;

/**
 * This class handles all siege commands: Todo: change the class name, and neaten it up
 */
public class AdminSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_add_guard",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_endsiege",
		"admin_startsiege",
		"admin_setsiegetime",
		"admin_setcastle",
		"admin_removecastle",
		"admin_clanhall",
		"admin_clanhallset",
		"admin_clanhalldel",
		"admin_clanhallopendoors",
		"admin_clanhallclosedoors",
		"admin_clanhallteleportself"
	};

	@SuppressWarnings("null")
	@Override
	public boolean useAdminCommand(String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		final StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken(); // Get actual command

		// Get castle
		Castle castle = null;
		ClanHall clanhall = null;
		if (st.hasMoreTokens())
			if (command.startsWith("admin_clanhall"))
				try
				{
					clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(st.nextToken()));
				}
				catch (final Exception e)
				{
				}
			else
				castle = CastleManager.getInstance().getCastle(st.nextToken());
		if ((castle == null || castle.getCastleId() < 0) && clanhall == null)
			// No castle specified
			showCastleSelectPage(activeChar);
		else
		{
			String val = "";
			if (st.hasMoreTokens())
				val = st.nextToken();

			final L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance)
				player = (L2PcInstance) target;

			if (command.equalsIgnoreCase("admin_add_attacker"))
			{
				if (player == null)
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				else
					castle.getSiege().registerAttacker(player, true);
			}
			else if (command.equalsIgnoreCase("admin_add_defender"))
			{
				if (player == null)
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				else
					castle.getSiege().registerDefender(player, true);
			}
			else if (command.equalsIgnoreCase("admin_add_guard"))
				try
				{
					final int npcId = Integer.parseInt(val);
					castle.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId);
				}
				catch (final Exception e)
				{
					activeChar.sendMessage("Usage: //add_guard castle npcId");
				}
			else if (command.equalsIgnoreCase("admin_clear_siege_list"))
				castle.getSiege().clearSiegeClan();
			else if (command.equalsIgnoreCase("admin_endsiege"))
				castle.getSiege().endSiege();
			else if (command.equalsIgnoreCase("admin_list_siege_clans"))
			{
				castle.getSiege().listRegisterClan(activeChar);
				return true;
			}
			else if (command.equalsIgnoreCase("admin_move_defenders"))
				activeChar.sendMessage("Not implemented yet.");
			else if (command.equalsIgnoreCase("admin_setcastle"))
			{
				if (player == null || player.getClan() == null)
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				else
					castle.setOwner(player.getClan());
			}
			else if (command.equalsIgnoreCase("admin_removecastle"))
			{
				final L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
				if (clan != null)
					castle.removeOwner(clan);
				else
					activeChar.sendMessage("Unable to remove castle");
			}
			else if (command.equalsIgnoreCase("admin_setsiegetime"))
			{
				if (st.hasMoreTokens())
				{
					final Calendar newAdminSiegeDate = castle.getSiegeDate();
					if (val.equalsIgnoreCase("day"))
						newAdminSiegeDate.set(Calendar.DAY_OF_YEAR, Integer.parseInt(st.nextToken()));
					else if (val.equalsIgnoreCase("hour"))
						newAdminSiegeDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st.nextToken()));
					else if (val.equalsIgnoreCase("min"))
						newAdminSiegeDate.set(Calendar.MINUTE, Integer.parseInt(st.nextToken()));

					if (newAdminSiegeDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
						activeChar.sendMessage("Unable to change Siege Date");
					else if (newAdminSiegeDate.getTimeInMillis() != castle.getSiegeDate().getTimeInMillis())
					{
						castle.getSiegeDate().setTimeInMillis(newAdminSiegeDate.getTimeInMillis());
						castle.getSiege().saveSiegeDate();
					}
				}
				showSiegeTimePage(activeChar, castle);
				return true;
			}
			else if (command.equalsIgnoreCase("admin_clanhallset"))
			{
				if (player == null || player.getClan() == null)
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				else if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
					activeChar.sendMessage("This ClanHall isn't free!");
				else if (player.getClan().getHasHideout() == 0)
				{
					ClanHallManager.getInstance().setOwner(clanhall.getId(), player.getClan());
					if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null)
						AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
				}
				else
					activeChar.sendMessage("You have already a ClanHall!");
			}
			else if (command.equalsIgnoreCase("admin_clanhalldel"))
			{
				if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
				{
					ClanHallManager.getInstance().setFree(clanhall.getId());
					AuctionManager.getInstance().initNPC(clanhall.getId());
				}
				else
					activeChar.sendMessage("This ClanHall is already Free!");
			}
			else if (command.equalsIgnoreCase("admin_clanhallopendoors"))
				clanhall.openCloseDoors(true);
			else if (command.equalsIgnoreCase("admin_clanhallclosedoors"))
				clanhall.openCloseDoors(false);
			else if (command.equalsIgnoreCase("admin_clanhallteleportself"))
			{
				final L2ClanHallZone zone = clanhall.getZone();
				if (zone != null)
					activeChar.teleToLocation(zone.getSpawnLoc(), true);
			}
			else if (command.equalsIgnoreCase("admin_spawn_doors"))
				castle.spawnDoor();
			else if (command.equalsIgnoreCase("admin_startsiege"))
				castle.getSiege().startSiege();
			if (clanhall != null)
				showClanHallPage(activeChar, clanhall);
			else
				showSiegePage(activeChar, castle.getName());
		}
		return true;
	}

	private void showCastleSelectPage(final L2PcInstance activeChar)
	{
		int i = 0;
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/castles.htm");
		final StringBuilder cList = new StringBuilder(500);
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				final String name = castle.getName();
				StringUtil.append(cList, "<td fixwidth=90><a action=\"bypass -h admin_siege ", name, "\">", name, "</a></td>");
				i++;
			}
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%castles%", cList.toString());
		cList.setLength(0);
		i = 0;
		for (final ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(cList, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", String.valueOf(clanhall.getId()), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%clanhalls%", cList.toString());
		cList.setLength(0);
		i = 0;
		for (final ClanHall clanhall : ClanHallManager.getInstance().getFreeClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(cList, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", String.valueOf(clanhall.getId()), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%freeclanhalls%", cList.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showSiegePage(final L2PcInstance activeChar, final String castleName)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/castle.htm");
		adminReply.replace("%castleName%", castleName);
		activeChar.sendPacket(adminReply);
	}

	private void showSiegeTimePage(final L2PcInstance activeChar, final Castle castle)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/castlesiegetime.htm");
		adminReply.replace("%castleName%", castle.getName());
		adminReply.replace("%time%", castle.getSiegeDate().getTime().toString());
		final Calendar newDay = Calendar.getInstance();
		boolean isSunday = false;
		if (newDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			isSunday = true;
		else
			newDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		if (!SevenSigns.getInstance().isDateInSealValidPeriod(newDay))
			newDay.add(Calendar.DAY_OF_MONTH, 7);

		if (isSunday)
		{
			adminReply.replace("%sundaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%sunday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
			newDay.add(Calendar.DAY_OF_MONTH, 13);
			adminReply.replace("%saturdaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%saturday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
		}
		else
		{
			adminReply.replace("%saturdaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%saturday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
			newDay.add(Calendar.DAY_OF_MONTH, 1);
			adminReply.replace("%sundaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%sunday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
		}
		activeChar.sendPacket(adminReply);
	}

	private void showClanHallPage(final L2PcInstance activeChar, final ClanHall clanhall)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/clanhall.htm");
		adminReply.replace("%clanhallName%", clanhall.getName());
		adminReply.replace("%clanhallId%", String.valueOf(clanhall.getId()));
		final L2Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
		if (owner == null)
			adminReply.replace("%clanhallOwner%", "None");
		else
			adminReply.replace("%clanhallOwner%", owner.getName());
		activeChar.sendPacket(adminReply);
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

}
