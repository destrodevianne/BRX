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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.communitybbs.Manager.RegionBBSManager;
import ct25.xtreme.gameserver.datatables.CharNameTable;
import ct25.xtreme.gameserver.datatables.CharTemplateTable;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PetInstance;
import ct25.xtreme.gameserver.model.base.ClassId;
import ct25.xtreme.gameserver.network.L2GameClient;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.communityserver.CommunityServerThread;
import ct25.xtreme.gameserver.network.communityserver.writepackets.WorldInfo;
import ct25.xtreme.gameserver.network.serverpackets.CharInfo;
import ct25.xtreme.gameserver.network.serverpackets.ExBrExtraUserInfo;
import ct25.xtreme.gameserver.network.serverpackets.ExVoteSystemInfo;
import ct25.xtreme.gameserver.network.serverpackets.GMViewItemList;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.PartySmallWindowAll;
import ct25.xtreme.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import ct25.xtreme.gameserver.network.serverpackets.SetSummonRemainTime;
import ct25.xtreme.gameserver.network.serverpackets.StatusUpdate;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.network.serverpackets.UserInfo;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.StringUtil;

/**
 * This class handles following admin commands: - edit_character - current_player - character_list - show_characters - find_character - find_ip - find_account - rec - nokarma - setkarma - settitle - changename - setsex - setclass - fullfood - save_modifications - setcolor - settcolor - setpk -
 * setpvp - remove_clan_penalty - summon_info - unsummon - summon_setlvl - show_pet_inv - partyinfo
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/04/11 10:06:06 $ Typo fix, rework for admin_tracert, gatherCharacterInfo and editCharacter by Zoey76 28/02/2011
 */
public class AdminEditChar implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminEditChar.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_edit_character",
		"admin_current_player",
		"admin_nokarma", // this is to remove karma from selected char...
		"admin_setkarma", // sets karma of target char to any amount. //setkarma <karma>
		"admin_setfame", // sets fame of target char to any amount. //setfame <fame>
		"admin_character_list", // same as character_info, kept for compatibility purposes
		"admin_character_info", // given a player name, displays an information window
		"admin_show_characters", // list of characters
		"admin_find_character", // find a player by his name or a part of it (case-insensitive)
		"admin_find_ip", // find all the player connections from a given IPv4 number
		"admin_find_account", // list all the characters from an account (useful for GMs w/o DB access)
		"admin_find_dualbox", // list all the IPs with more than 1 char logged in (dualbox)
		"admin_strict_find_dualbox",
		"admin_tracert",
		"admin_save_modifications", // consider it deprecated...
		"admin_rec", // gives recommendation points
		"admin_settitle", // changes char title
		"admin_changename", // changes char name
		"admin_setsex", // changes characters' sex
		"admin_setcolor", // change charnames' color display
		"admin_settcolor", // change char title color
		"admin_setclass", // changes chars' classId
		"admin_setpk", // changes PK count
		"admin_setpvp", // changes PVP count
		"admin_fullfood", // fulfills a pet's food bar
		"admin_remove_clan_penalty", // removes clan penalties
		"admin_summon_info", // displays an information window about target summon
		"admin_unsummon",
		"admin_summon_setlvl",
		"admin_show_pet_inv",
		"admin_partyinfo"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.equals("admin_current_player"))
			showCharacterInfo(activeChar, activeChar);
		else if (command.startsWith("admin_character_info"))
		{
			final String[] data = command.split(" ");
			if (data.length > 1)
				showCharacterInfo(activeChar, L2World.getInstance().getPlayer(data[1]));
			else if (activeChar.getTarget() instanceof L2PcInstance)
				showCharacterInfo(activeChar, activeChar.getTarget().getActingPlayer());
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		else if (command.startsWith("admin_character_list"))
			listCharacters(activeChar, 0);
		else if (command.startsWith("admin_show_characters"))
			try
			{
				final String val = command.substring(22);
				final int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty page number
				activeChar.sendMessage("Usage: //show_characters <page_number>");
			}
		else if (command.startsWith("admin_find_character"))
			try
			{
				final String val = command.substring(21);
				findCharacter(activeChar, val);
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Usage: //find_character <character_name>");
				listCharacters(activeChar, 0);
			}
		else if (command.startsWith("admin_find_ip"))
			try
			{
				final String val = command.substring(14);
				findCharactersPerIp(activeChar, val);
			}
			catch (final Exception e)
			{ // Case of empty or malformed IP number
				activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
				listCharacters(activeChar, 0);
			}
		else if (command.startsWith("admin_find_account"))
			try
			{
				final String val = command.substring(19);
				findCharactersPerAccount(activeChar, val);
			}
			catch (final Exception e)
			{ // Case of empty or malformed player name
				activeChar.sendMessage("Usage: //find_account <player_name>");
				listCharacters(activeChar, 0);
			}
		else if (command.startsWith("admin_edit_character"))
		{
			final String[] data = command.split(" ");
			if (data.length > 1)
				editCharacter(activeChar, data[1]);
			else if (activeChar.getTarget() instanceof L2PcInstance)
				editCharacter(activeChar, null);
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		// Karma control commands
		else if (command.equals("admin_nokarma"))
			setTargetKarma(activeChar, 0);
		else if (command.startsWith("admin_setkarma"))
			try
			{
				final String val = command.substring(15);
				final int karma = Integer.parseInt(val);
				setTargetKarma(activeChar, karma);
			}
			catch (final Exception e)
			{
				if (Config.DEVELOPER)
					_log.warning("Set karma error: " + e);
				activeChar.sendMessage("Usage: //setkarma <new_karma_value>");
			}
		else if (command.startsWith("admin_setpk"))
			try
			{
				final String val = command.substring(12);
				final int pk = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setPkKills(pk);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendPacket(new ExBrExtraUserInfo(player));
					player.sendMessage("A GM changed your PK count to " + pk);
					activeChar.sendMessage(player.getName() + "'s PK count changed to " + pk);
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			}
			catch (final Exception e)
			{
				if (Config.DEVELOPER)
					_log.warning("Set pk error: " + e);
				activeChar.sendMessage("Usage: //setpk <pk_count>");
			}
		else if (command.startsWith("admin_setpvp"))
			try
			{
				final String val = command.substring(13);
				final int pvp = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setPvpKills(pvp);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendPacket(new ExBrExtraUserInfo(player));
					player.sendMessage("A GM changed your PVP count to " + pvp);
					activeChar.sendMessage(player.getName() + "'s PVP count changed to " + pvp);
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			}
			catch (final Exception e)
			{
				if (Config.DEVELOPER)
					_log.warning("Set pvp error: " + e);
				activeChar.sendMessage("Usage: //setpvp <pvp_count>");
			}
		else if (command.startsWith("admin_setfame"))
			try
			{
				final String val = command.substring(14);
				final int fame = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setFame(fame);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendPacket(new ExBrExtraUserInfo(player));
					player.sendMessage("A GM changed your Reputation points to " + fame);
					activeChar.sendMessage(player.getName() + "'s Fame changed to " + fame);
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			}
			catch (final Exception e)
			{
				if (Config.DEVELOPER)
					_log.warning("Set Fame error: " + e);
				activeChar.sendMessage("Usage: //setfame <new_fame_value>");
			}
		else if (command.startsWith("admin_save_modifications"))
			try
			{
				final String val = command.substring(24);
				adminModifyCharacter(activeChar, val);
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(activeChar, 0);
			}
		else if (command.startsWith("admin_rec"))
			try
			{
				final String val = command.substring(10);
				final int recVal = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) target;
					player.setRecomHave(recVal);
					player.broadcastUserInfo();
					player.sendPacket(new UserInfo(player));
					player.sendPacket(new ExBrExtraUserInfo(player));
					player.sendPacket(new ExVoteSystemInfo(player));
					player.sendMessage("A GM changed your Recommend points to " + recVal);
					activeChar.sendMessage(player.getName() + "'s Recommend changed to " + recVal);
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //rec number");
			}
		else if (command.startsWith("admin_setclass"))
			try
			{
				final String val = command.substring(15);
				final int classidval = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
					player = (L2PcInstance) target;
				else
					return false;
				boolean valid = false;
				for (final ClassId classid : ClassId.values())
					if (classidval == classid.getId())
						valid = true;
				if (valid && player.getClassId().getId() != classidval)
				{
					player.setClassId(classidval);
					if (!player.isSubClassActive())
						player.setBaseClass(classidval);
					final String newclass = player.getTemplate().className;
					player.store();
					player.sendMessage("A GM changed your class to " + newclass);
					player.broadcastUserInfo();
					activeChar.sendMessage(player.getName() + " is a " + newclass);
				}
				else
					activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
			}
		else if (command.startsWith("admin_settitle"))
			try
			{
				final String val = command.substring(15);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
					player = (L2PcInstance) target;
				else
					return false;
				player.setTitle(val);
				player.sendMessage("Your title has been changed by a GM");
				player.broadcastTitleInfo();
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character title
				activeChar.sendMessage("You need to specify the new title.");
			}
		else if (command.startsWith("admin_changename"))
			try
			{
				final String val = command.substring(17);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
					player = (L2PcInstance) target;
				else
					return false;
				if (CharNameTable.getInstance().getIdByName(val) > 0)
				{
					activeChar.sendMessage("Warning, player " + val + " already exists");
					return false;
				}
				player.setName(val);
				player.store();
				CharNameTable.getInstance().addName(player);

				activeChar.sendMessage("Changed name to " + val);
				player.sendMessage("Your name has been changed by a GM.");
				player.broadcastUserInfo();
				CommunityServerThread.getInstance().sendPacket(new WorldInfo(player, null, WorldInfo.TYPE_UPDATE_PLAYER_DATA));

				if (player.isInParty())
				{
					// Delete party window for other party members
					player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
					for (final L2PcInstance member : player.getParty().getPartyMembers())
						// And re-add
						if (member != player)
							member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
				}
				if (player.getClan() != null)
					player.getClan().broadcastClanStatus();

				RegionBBSManager.getInstance().changeCommunityBoard();
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				activeChar.sendMessage("Usage: //setname new_name_for_target");
			}
		else if (command.startsWith("admin_setsex"))
		{
			final L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance)
				player = (L2PcInstance) target;
			else
				return false;
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.sendMessage("Your gender has been changed by a GM");
			player.broadcastUserInfo();
			player.decayMe();
			player.spawnMe(player.getX(), player.getY(), player.getZ());
		}
		else if (command.startsWith("admin_setcolor"))
			try
			{
				final String val = command.substring(15);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
					player = (L2PcInstance) target;
				else
					return false;
				player.getAppearance().setNameColor(Integer.decode("0x" + val));
				player.sendMessage("Your name color has been changed by a GM");
				player.broadcastUserInfo();
			}
			catch (final Exception e)
			{ // Case of empty color or invalid hex string
				activeChar.sendMessage("You need to specify a valid new color.");
			}
		else if (command.startsWith("admin_settcolor"))
			try
			{
				final String val = command.substring(16);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if (target instanceof L2PcInstance)
					player = (L2PcInstance) target;
				else
					return false;
				player.getAppearance().setTitleColor(Integer.decode("0x" + val));
				player.sendMessage("Your title color has been changed by a GM");
				player.broadcastUserInfo();
			}
			catch (final Exception e)
			{ // Case of empty color or invalid hex string
				activeChar.sendMessage("You need to specify a valid new color.");
			}
		else if (command.startsWith("admin_fullfood"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PetInstance)
			{
				final L2PetInstance targetPet = (L2PetInstance) target;
				targetPet.setCurrentFed(targetPet.getMaxFed());
				targetPet.getOwner().sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
		else if (command.startsWith("admin_remove_clan_penalty"))
			try
			{
				final StringTokenizer st = new StringTokenizer(command, " ");
				if (st.countTokens() != 3)
				{
					activeChar.sendMessage("Usage: //remove_clan_penalty join|create charname");
					return false;
				}

				st.nextToken();

				final boolean changeCreateExpiryTime = st.nextToken().equalsIgnoreCase("create");

				final String playerName = st.nextToken();
				L2PcInstance player = null;
				player = L2World.getInstance().getPlayer(playerName);

				if (player == null)
				{
					final Connection con = L2DatabaseFactory.getInstance().getConnection();
					final PreparedStatement ps = con.prepareStatement("UPDATE characters SET " + (changeCreateExpiryTime ? "clan_create_expiry_time" : "clan_join_expiry_time") + " WHERE char_name=? LIMIT 1");

					ps.setString(1, playerName);
					ps.execute();
				}
				else // removing penalty
				if (changeCreateExpiryTime)
					player.setClanCreateExpiryTime(0);
				else
					player.setClanJoinExpiryTime(0);
				
				activeChar.sendMessage("Clan penalty successfully removed to character: " + playerName);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		else if (command.startsWith("admin_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				final String val = command.substring(19);
				multibox = Integer.parseInt(val);
				if (multibox < 1)
				{
					activeChar.sendMessage("Usage: //find_dualbox [number > 0]");
					return false;
				}
			}
			catch (final Exception e)
			{
			}
			findDualbox(activeChar, multibox);
		}
		else if (command.startsWith("admin_strict_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				final String val = command.substring(26);
				multibox = Integer.parseInt(val);
				if (multibox < 1)
				{
					activeChar.sendMessage("Usage: //strict_find_dualbox [number > 0]");
					return false;
				}
			}
			catch (final Exception e)
			{
			}
			findDualboxStrict(activeChar, multibox);
		}
		else if (command.startsWith("admin_tracert"))
		{
			final String[] data = command.split(" ");
			L2PcInstance pl = null;
			if (data.length > 1)
				pl = L2World.getInstance().getPlayer(data[1]);
			else
			{
				final L2Object target = activeChar.getTarget();
				if (target instanceof L2PcInstance)
					pl = (L2PcInstance) target;
			}
			
			if (pl == null)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
				return false;
			}
			
			if (pl.getClient() == null)
			{
				activeChar.sendMessage("Client is null.");
				return false;
			}
			
			if (pl.getClient().isDetached())
			{
				activeChar.sendMessage("Client is detached.");
				return false;
			}
			
			String ip;
			final int[][] trace = pl.getClient().getTrace();
			for (int i = 0; i < trace.length; i++)
			{
				ip = "";
				for (int o = 0; o < trace[0].length; o++)
				{
					ip = ip + trace[i][o];
					if (o != trace[0].length - 1)
						ip = ip + ".";
				}
				activeChar.sendMessage("Hop" + i + ": " + ip);
			}
		}
		else if (command.startsWith("admin_summon_info"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2Summon)
				gatherSummonInfo((L2Summon) target, activeChar);
			else
				activeChar.sendMessage("Invalid target.");
		}
		else if (command.startsWith("admin_unsummon"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2Summon)
				((L2Summon) target).unSummon(((L2Summon) target).getOwner());
			else
				activeChar.sendMessage("Usable only with Pets/Summons");
		}
		else if (command.startsWith("admin_summon_setlvl"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PetInstance)
			{
				final L2PetInstance pet = (L2PetInstance) target;
				try
				{
					final String val = command.substring(20);
					final int level = Integer.parseInt(val);
					long newexp, oldexp = 0;
					oldexp = pet.getStat().getExp();
					newexp = pet.getStat().getExpForLevel(level);
					if (oldexp > newexp)
						pet.getStat().removeExp(oldexp - newexp);
					else if (oldexp < newexp)
						pet.getStat().addExp(newexp - oldexp);
				}
				catch (final Exception e)
				{
				}
			}
			else
				activeChar.sendMessage("Usable only with Pets");
		}
		else if (command.startsWith("admin_show_pet_inv"))
		{
			String val;
			int objId;
			L2Object target;
			try
			{
				val = command.substring(19);
				objId = Integer.parseInt(val);
				target = L2World.getInstance().getPet(objId);
			}
			catch (final Exception e)
			{
				target = activeChar.getTarget();
			}

			if (target instanceof L2PetInstance)
				activeChar.sendPacket(new GMViewItemList((L2PetInstance) target));
			else
				activeChar.sendMessage("Usable only with Pets");

		}
		else if (command.startsWith("admin_partyinfo"))
		{
			String val;
			L2Object target;
			try
			{
				val = command.substring(16);
				target = L2World.getInstance().getPlayer(val);
				if (target == null)
					target = activeChar.getTarget();
			}
			catch (final Exception e)
			{
				target = activeChar.getTarget();
			}

			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isInParty())
					gatherPartyInfo((L2PcInstance) target, activeChar);
				else
					activeChar.sendMessage("Not in party.");
			}
			else
				activeChar.sendMessage("Invalid target.");

		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void listCharacters(final L2PcInstance activeChar, int page)
	{
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		L2PcInstance[] players;
		// synchronized (L2World.getInstance().getAllPlayers())
		{
			players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		}

		final int maxCharactersPerPage = 20;
		int maxPages = players.length / maxCharactersPerPage;

		if (players.length > maxCharactersPerPage * maxPages)
			maxPages++;

		// Check if number of users changed
		if (page > maxPages)
			page = maxPages;

		final int charactersStart = maxCharactersPerPage * page;
		int charactersEnd = players.length;
		if (charactersEnd - charactersStart > maxCharactersPerPage)
			charactersEnd = charactersStart + maxCharactersPerPage;

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/charlist.htm");

		final StringBuilder replyMSG = new StringBuilder(1000);

		for (int x = 0; x < maxPages; x++)
		{
			final int pagenr = x + 1;
			StringUtil.append(replyMSG, "<center><a action=\"bypass -h admin_show_characters ", String.valueOf(x), "\">Page ", String.valueOf(pagenr), "</a></center>");
		}

		adminReply.replace("%pages%", replyMSG.toString());
		replyMSG.setLength(0);

		for (int i = charactersStart; i < charactersEnd; i++)
			// Add player info into new Table row
			StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_info ", players[i].getName(), "\">", players[i].getName(), "</a></td><td width=110>", players[i].getTemplate().className, "</td><td width=40>", String.valueOf(players[i].getLevel()), "</td></tr>");

		adminReply.replace("%players%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showCharacterInfo(final L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
				player = (L2PcInstance) target;
			else
				return;
		}
		else
			activeChar.setTarget(player);
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}

	/**
	 * Retrieve and replace player's info in filename htm file, sends it to activeChar as NpcHtmlMessage.
	 * @param activeChar
	 * @param player
	 * @param filename
	 */
	private void gatherCharacterInfo(final L2PcInstance activeChar, final L2PcInstance player, final String filename)
	{
		String ip = "N/A";
		String account = "N/A";

		if (player != null)
		{
			account = player.getAccountName();
			if (player.getClient() != null)
			{
				if (player.getClient().isDetached())
					activeChar.sendMessage("Client is detached.");
				else
					ip = player.getClient().getConnection().getInetAddress().getHostAddress();
			}
			else
				activeChar.sendMessage("Client is null.");
		}
		else
		{
			activeChar.sendMessage("Player is null.");
			return;
		}

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%clan%", String.valueOf(player.getClan() != null ? "<a action=\"bypass -h admin_clan_info " + player.getObjectId() + "\">" + player.getClan().getName() + "</a>" : null));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().className);
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%baseclass%", CharTemplateTable.getInstance().getClassNameById(player.getBaseClass()));
		adminReply.replace("%x%", String.valueOf(player.getX()));
		adminReply.replace("%y%", String.valueOf(player.getY()));
		adminReply.replace("%z%", String.valueOf(player.getZ()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getKarma()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo((float) player.getCurrentLoad() / (float) player.getMaxLoad() * 100, 2)));
		adminReply.replace("%patk%", String.valueOf(player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf(player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%access%", String.valueOf(player.getAccessLevel().getLevel()));
		adminReply.replace("%account%", account);
		adminReply.replace("%ip%", ip);
		adminReply.replace("%ai%", String.valueOf(player.getAI().getIntention().name()));
		adminReply.replace("%inst%", player.getInstanceId() > 0 ? "<tr><td>InstanceId:</td><td><a action=\"bypass -h admin_instance_spawns " + String.valueOf(player.getInstanceId()) + "\">" + String.valueOf(player.getInstanceId()) + "</a></td></tr>" : "");
		activeChar.sendPacket(adminReply);
	}

	private void setTargetKarma(final L2PcInstance activeChar, final int newKarma)
	{
		// function to change karma of selected char
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
			player = (L2PcInstance) target;
		else
			return;

		if (newKarma >= 0)
		{
			// for display
			final int oldKarma = player.getKarma();
			// update karma
			player.setKarma(newKarma);
			// Common character information
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addString(String.valueOf(newKarma)));
			// Admin information
			activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
			if (Config.DEBUG)
				_log.fine("[SET KARMA] [GM]" + activeChar.getName() + " Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
		}
		else
		{
			// tell admin of mistake
			activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
			if (Config.DEBUG)
				_log.fine("[SET KARMA] ERROR: [GM]" + activeChar.getName() + " entered an incorrect value for new karma: " + newKarma + " for " + player.getName() + ".");
		}
	}

	private void adminModifyCharacter(final L2PcInstance activeChar, final String modifications)
	{
		final L2Object target = activeChar.getTarget();

		if (!(target instanceof L2PcInstance))
			return;

		final L2PcInstance player = (L2PcInstance) target;
		final StringTokenizer st = new StringTokenizer(modifications);

		if (st.countTokens() != 6)
		{
			editCharacter(activeChar, null);
			return;
		}

		final String hp = st.nextToken();
		final String mp = st.nextToken();
		final String cp = st.nextToken();
		final String pvpflag = st.nextToken();
		final String pvpkills = st.nextToken();
		final String pkkills = st.nextToken();

		final int hpval = Integer.parseInt(hp);
		final int mpval = Integer.parseInt(mp);
		final int cpval = Integer.parseInt(cp);
		final int pvpflagval = Integer.parseInt(pvpflag);
		final int pvpkillsval = Integer.parseInt(pvpkills);
		final int pkkillsval = Integer.parseInt(pkkills);

		// Common character information
		player.sendMessage("Admin has changed your stats." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP Flag: " + pvpflagval + " PvP/PK " + pvpkillsval + "/" + pkkillsval);
		player.setCurrentHp(hpval);
		player.setCurrentMp(mpval);
		player.setCurrentCp(cpval);
		player.setPvpFlag(pvpflagval);
		player.setPvpKills(pvpkillsval);
		player.setPkKills(pkkillsval);

		// Save the changed parameters to the database.
		player.store();

		final StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_HP, hpval);
		su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
		su.addAttribute(StatusUpdate.CUR_MP, mpval);
		su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
		su.addAttribute(StatusUpdate.CUR_CP, cpval);
		su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
		player.sendPacket(su);

		// Admin information
		activeChar.sendMessage("Changed stats of " + player.getName() + "." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval);

		if (Config.DEBUG)
			_log.fine("[GM]" + activeChar.getName() + " changed stats of " + player.getName() + ". " + " HP: " + hpval + " MP: " + mpval + " CP: " + cpval + " PvP: " + pvpflagval + " / " + pvpkillsval);

		showCharacterInfo(activeChar, null); // Back to start

		player.broadcastPacket(new CharInfo(player));
		player.sendPacket(new UserInfo(player));
		player.broadcastPacket(new ExBrExtraUserInfo(player));
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.decayMe();
		player.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}

	private void editCharacter(final L2PcInstance activeChar, final String targetName)
	{
		L2Object target = null;
		if (targetName != null)
			target = L2World.getInstance().getPlayer(targetName);
		else
			target = activeChar.getTarget();

		if (target instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) target;
			gatherCharacterInfo(activeChar, player, "charedit.htm");
		}
	}

	/**
	 * @param activeChar
	 * @param CharacterToFind
	 */
	private void findCharacter(final L2PcInstance activeChar, final String CharacterToFind)
	{
		int CharactersFound = 0;
		String name;
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		L2PcInstance[] players;
		// synchronized (L2World.getInstance().getAllPlayers())
		{
			players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		}
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/charfind.htm");

		final StringBuilder replyMSG = new StringBuilder(1000);

		for (final L2PcInstance player : players)
		{ // Add player info into new Table row
			name = player.getName();
			if (name.toLowerCase().contains(CharacterToFind.toLowerCase()))
			{
				CharactersFound = CharactersFound + 1;
				StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", player.getTemplate().className, "</td><td width=40>", String.valueOf(player.getLevel()), "</td></tr>");
			}
			if (CharactersFound > 20)
				break;
		}
		adminReply.replace("%results%", replyMSG.toString());

		final String replyMSG2;

		if (CharactersFound == 0)
			replyMSG2 = "s. Please try again.";
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than 20");
			replyMSG2 = "s.<br>Please refine your search to see all of the results.";
		}
		else if (CharactersFound == 1)
			replyMSG2 = ".";
		else
			replyMSG2 = "s.";

		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}

	/**
	 * @param activeChar
	 * @param IpAdress
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerIp(final L2PcInstance activeChar, final String IpAdress) throws IllegalArgumentException
	{
		boolean findDisconnected = false;

		if (IpAdress.equals("disconnected"))
			findDisconnected = true;
		else if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
			throw new IllegalArgumentException("Malformed IPv4 number");
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		L2PcInstance[] players;
		// synchronized (L2World.getInstance().getAllPlayers())
		{
			players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		}
		int CharactersFound = 0;
		L2GameClient client;
		String name, ip = "0.0.0.0";
		final StringBuilder replyMSG = new StringBuilder(1000);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/ipfind.htm");
		for (final L2PcInstance player : players)
		{
			client = player.getClient();
			if (client.isDetached())
			{
				if (!findDisconnected)
					continue;
			}
			else if (findDisconnected)
				continue;
			else
			{
				ip = client.getConnection().getInetAddress().getHostAddress();
				if (!ip.equals(IpAdress))
					continue;
			}

			name = player.getName();
			CharactersFound = CharactersFound + 1;
			StringUtil.append(replyMSG, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", player.getTemplate().className, "</td><td width=40>", String.valueOf(player.getLevel()), "</td></tr>");

			if (CharactersFound > 20)
				break;
		}
		adminReply.replace("%results%", replyMSG.toString());

		final String replyMSG2;

		if (CharactersFound == 0)
			replyMSG2 = "s. Maybe they got d/c? :)";
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than " + String.valueOf(CharactersFound));
			replyMSG2 = "s.<br>In order to avoid you a client crash I won't <br1>display results beyond the 20th character.";
		}
		else if (CharactersFound == 1)
			replyMSG2 = ".";
		else
			replyMSG2 = "s.";
		adminReply.replace("%ip%", IpAdress);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}

	/**
	 * @param activeChar
	 * @param characterName
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerAccount(final L2PcInstance activeChar, final String characterName) throws IllegalArgumentException
	{
		if (characterName.matches(Config.CNAME_TEMPLATE))
		{
			String account = null;
			Map<Integer, String> chars;
			final L2PcInstance player = L2World.getInstance().getPlayer(characterName);
			if (player == null)
				throw new IllegalArgumentException("Player doesn't exist");
			chars = player.getAccountChars();
			account = player.getAccountName();
			final StringBuilder replyMSG = new StringBuilder(chars.size() * 20);
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/accountinfo.htm");
			for (final String charname : chars.values())
				StringUtil.append(replyMSG, charname, "<br1>");

			adminReply.replace("%characters%", replyMSG.toString());
			adminReply.replace("%account%", account);
			adminReply.replace("%player%", characterName);
			activeChar.sendPacket(adminReply);
		}
		else
			throw new IllegalArgumentException("Malformed character name");
	}

	/**
	 * @param activeChar
	 * @param multibox 
	 */
	private void findDualbox(final L2PcInstance activeChar, final int multibox)
	{
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		final L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

		final Map<String, List<L2PcInstance>> ipMap = new HashMap<>();

		String ip = "0.0.0.0";
		L2GameClient client;

		final Map<String, Integer> dualboxIPs = new HashMap<>();

		for (final L2PcInstance player : players)
		{
			client = player.getClient();
			if (client == null || client.isDetached())
				continue;
			ip = client.getConnection().getInetAddress().getHostAddress();
			if (ipMap.get(ip) == null)
				ipMap.put(ip, new ArrayList<L2PcInstance>());
			ipMap.get(ip).add(player);

			if (ipMap.get(ip).size() >= multibox)
			{
				final Integer count = dualboxIPs.get(ip);
				if (count == null)
					dualboxIPs.put(ip, multibox);
				else
					dualboxIPs.put(ip, count + 1);
			}
		}

		final List<String> keys = new ArrayList<>(dualboxIPs.keySet());
		Collections.sort(keys, new Comparator<String>()
		{
			@Override
			public int compare(final String left, final String right)
			{
				return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
			}
		});
		Collections.reverse(keys);

		final StringBuilder results = new StringBuilder();
		for (final String dualboxIP : keys)
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "");
		activeChar.sendPacket(adminReply);
	}

	private void findDualboxStrict(final L2PcInstance activeChar, final int multibox)
	{
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		final L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

		final Map<IpPack, List<L2PcInstance>> ipMap = new HashMap<>();

		L2GameClient client;

		final Map<IpPack, Integer> dualboxIPs = new HashMap<>();

		for (final L2PcInstance player : players)
		{
			client = player.getClient();
			if (client == null || client.isDetached())
				continue;
			final IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
			if (ipMap.get(pack) == null)
				ipMap.put(pack, new ArrayList<L2PcInstance>());
			ipMap.get(pack).add(player);

			if (ipMap.get(pack).size() >= multibox)
			{
				final Integer count = dualboxIPs.get(pack);
				if (count == null)
					dualboxIPs.put(pack, multibox);
				else
					dualboxIPs.put(pack, count + 1);
			}
		}

		final List<IpPack> keys = new ArrayList<>(dualboxIPs.keySet());
		Collections.sort(keys, new Comparator<IpPack>()
		{
			@Override
			public int compare(final IpPack left, final IpPack right)
			{
				return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
			}
		});
		Collections.reverse(keys);

		final StringBuilder results = new StringBuilder();
		for (final IpPack dualboxIP : keys)
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP.ip + "\">" + dualboxIP.ip + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");

		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "strict_");
		activeChar.sendPacket(adminReply);
	}

	private final class IpPack
	{
		String ip;
		int[][] tracert;

		public IpPack(final String ip, final int[][] tracert)
		{
			this.ip = ip;
			this.tracert = tracert;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + (ip == null ? 0 : ip.hashCode());
			for (final int[] array : tracert)
				result = prime * result + Arrays.hashCode(array);
			return result;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final IpPack other = (IpPack) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (ip == null)
			{
				if (other.ip != null)
					return false;
			}
			else if (!ip.equals(other.ip))
				return false;
			for (int i = 0; i < tracert.length; i++)
				for (int o = 0; o < tracert[0].length; o++)
					if (tracert[i][o] != other.tracert[i][o])
						return false;
			return true;
		}

		private AdminEditChar getOuterType()
		{
			return AdminEditChar.this;
		}
	}

	private void gatherSummonInfo(final L2Summon target, final L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/petinfo.htm");
		final String name = target.getName();
		html.replace("%name%", name == null ? "N/A" : name);
		html.replace("%level%", Integer.toString(target.getLevel()));
		html.replace("%exp%", Long.toString(target.getStat().getExp()));
		final String owner = target.getActingPlayer().getName();
		html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + owner + "\">" + owner + "</a>");
		html.replace("%class%", target.getClass().getSimpleName());
		html.replace("%ai%", target.hasAI() ? String.valueOf(target.getAI().getIntention().name()) : "NULL");
		html.replace("%hp%", (int) target.getStatus().getCurrentHp() + "/" + target.getStat().getMaxHp());
		html.replace("%mp%", (int) target.getStatus().getCurrentMp() + "/" + target.getStat().getMaxMp());
		html.replace("%karma%", Integer.toString(target.getKarma()));
		html.replace("%undead%", target.isUndead() ? "yes" : "no");
		if (target instanceof L2PetInstance)
		{
			final int objId = target.getActingPlayer().getObjectId();
			html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + objId + "\">view</a>");
		}
		else
			html.replace("%inv%", "none");
		if (target instanceof L2PetInstance)
		{
			html.replace("%food%", ((L2PetInstance) target).getCurrentFed() + "/" + ((L2PetInstance) target).getPetLevelData().getPetMaxFeed());
			html.replace("%load%", ((L2PetInstance) target).getInventory().getTotalWeight() + "/" + ((L2PetInstance) target).getMaxLoad());
		}
		else
		{
			html.replace("%food%", "N/A");
			html.replace("%load%", "N/A");
		}
		activeChar.sendPacket(html);
	}

	private void gatherPartyInfo(final L2PcInstance target, final L2PcInstance activeChar)
	{
		boolean color = true;
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/partyinfo.htm");
		final StringBuilder text = new StringBuilder(400);
		for (final L2PcInstance member : target.getParty().getPartyMembers())
		{
			if (color)
				text.append("<tr><td><table width=270 border=0 bgcolor=131210 cellpadding=2><tr><td width=30 align=right>");
			else
				text.append("<tr><td><table width=270 border=0 cellpadding=2><tr><td width=30 align=right>");
			text.append(member.getLevel() + "</td><td width=130><a action=\"bypass -h admin_character_info " + member.getName() + "\">" + member.getName() + "</a>");
			text.append("</td><td width=110 align=right>" + member.getClassId().toString() + "</td></tr></table></td></tr>");
			color = !color;
		}
		html.replace("%player%", target.getName());
		html.replace("%party%", text.toString());
		activeChar.sendPacket(html);
	}
}
