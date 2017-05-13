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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import ct25.xtreme.gameserver.cache.HtmCache;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.instancemanager.CastleManager;
import ct25.xtreme.gameserver.instancemanager.ClanHallManager;
import ct25.xtreme.gameserver.instancemanager.FortManager;
import ct25.xtreme.gameserver.instancemanager.SiegeManager;
import ct25.xtreme.gameserver.model.L2Clan;
import ct25.xtreme.gameserver.model.L2ClanMember;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.communityserver.CommunityServerThread;
import ct25.xtreme.gameserver.network.communityserver.writepackets.WorldInfo;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * @author ThE_PuNiSHeR a.k.a UnAfraid
 */
public class AdminClan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_clan_info",
		"admin_clan_changeleader"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		final StringTokenizer st = new StringTokenizer(command, " ");
		final String cmd = st.nextToken();
		if (cmd.startsWith("admin_clan_info"))
			try
			{
				int objectId = 0;
				try
				{
					objectId = Integer.parseInt(st.nextToken());
				}
				catch (final NoSuchElementException NSEE)
				{
					objectId = activeChar.getTargetId();
				}
				final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
				if (player != null)
				{
					final L2Clan clan = player.getClan();
					if (clan != null)
						try
						{
							final NpcHtmlMessage msg = new NpcHtmlMessage(0);
							final String htm = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/admin/claninfo.htm");
							msg.setHtml(htm.toString());
							msg.replace("%clan_name%", clan.getName());
							msg.replace("%clan_leader%", clan.getLeaderName());
							msg.replace("%clan_level%", String.valueOf(clan.getLevel()));
							msg.replace("%clan_has_castle%", clan.getHasCastle() > 0 ? CastleManager.getInstance().getCastleById(clan.getHasCastle()).getName() : "No");
							msg.replace("%clan_has_clanhall%", clan.getHasHideout() > 0 ? ClanHallManager.getInstance().getClanHallById(clan.getHasHideout()).getName() : "No");
							msg.replace("%clan_has_fortress%", clan.getHasFort() > 0 ? FortManager.getInstance().getFortById(clan.getHasFort()).getName() : "No");
							msg.replace("%clan_points%", String.valueOf(clan.getReputationScore()));
							msg.replace("%clan_players_count%", String.valueOf(clan.getMembersCount()));
							msg.replace("%clan_ally%", clan.getAllyId() > 0 ? clan.getAllyName() : "Not in ally");
							msg.replace("%current_player_objectId%", String.valueOf(objectId));
							msg.replace("%current_player_name%", player.getName());
							activeChar.sendPacket(msg);

						}
						catch (final NullPointerException npe)
						{
							npe.printStackTrace();
						}
					else
					{
						activeChar.sendMessage("Clan not found.");
						return false;
					}
				}
				else
				{
					activeChar.sendMessage("Player is offline!");
					return false;
				}
			}
			catch (final NumberFormatException nfe)
			{
				activeChar.sendMessage("This shouldn't happening");
				return false;
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		else if (cmd.startsWith("admin_clan_changeleader"))
			try
			{
				final int objectId = Integer.parseInt(st.nextToken());

				final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
				if (player != null)
				{
					final L2Clan clan = player.getClan();
					if (clan == null)
					{
						activeChar.sendMessage("Player don't have clan");
						return false;
					}
					for (final L2ClanMember member : clan.getMembers())
						if (member.getObjectId() == player.getObjectId())
						{
							final L2PcInstance exLeader = clan.getLeader().getPlayerInstance();
							if (exLeader != null)
							{
								SiegeManager.getInstance().removeSiegeSkills(exLeader);
								exLeader.setClan(clan);
								exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
								exLeader.broadcastUserInfo();
								exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
								exLeader.broadcastUserInfo();
								exLeader.checkItemRestriction();
							}
							else
							{
								// TODO: with query?
							}

							clan.setLeader(member);
							clan.updateClanInDB();

							final L2PcInstance newLeader = member.getPlayerInstance();
							newLeader.setClan(clan);
							newLeader.setPledgeClass(member.calculatePledgeClass(newLeader));
							newLeader.setClanPrivileges(L2Clan.CP_ALL);

							if (clan.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel())
								SiegeManager.getInstance().addSiegeSkills(newLeader);

							newLeader.broadcastUserInfo();

							clan.broadcastClanStatus();

							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1);
							sm.addString(newLeader.getName());
							clan.broadcastToOnlineMembers(sm);
							activeChar.sendMessage("Clan leader has been changed!");
							CommunityServerThread.getInstance().sendPacket(new WorldInfo(null, clan, WorldInfo.TYPE_UPDATE_CLAN_DATA));
						}
				}
				else
					activeChar.sendMessage("Player is offline");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
