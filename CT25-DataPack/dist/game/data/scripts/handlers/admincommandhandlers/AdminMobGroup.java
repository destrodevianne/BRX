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

import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.MobGroup;
import ct25.xtreme.gameserver.model.MobGroupTable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.gameserver.network.serverpackets.SetupGauge;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct25.xtreme.gameserver.util.Broadcast;

/**
 * @author littlecrow Admin commands handler for controllable mobs
 */
public class AdminMobGroup implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mobmenu",
		"admin_mobgroup_list",
		"admin_mobgroup_create",
		"admin_mobgroup_remove",
		"admin_mobgroup_delete",
		"admin_mobgroup_spawn",
		"admin_mobgroup_unspawn",
		"admin_mobgroup_kill",
		"admin_mobgroup_idle",
		"admin_mobgroup_attack",
		"admin_mobgroup_rnd",
		"admin_mobgroup_return",
		"admin_mobgroup_follow",
		"admin_mobgroup_casting",
		"admin_mobgroup_nomove",
		"admin_mobgroup_attackgrp",
		"admin_mobgroup_invul"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.equals("admin_mobmenu"))
		{
			showMainPage(activeChar, command);
			return true;
		}
		else if (command.equals("admin_mobgroup_list"))
			showGroupList(activeChar);
		else if (command.startsWith("admin_mobgroup_create"))
			createGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_delete") || command.startsWith("admin_mobgroup_remove"))
			removeGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_spawn"))
			spawnGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_unspawn"))
			unspawnGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_kill"))
			killGroup(command, activeChar);
		else if (command.startsWith("admin_mobgroup_attackgrp"))
			attackGrp(command, activeChar);
		else if (command.startsWith("admin_mobgroup_attack"))
		{
			if (activeChar.getTarget() instanceof L2Character)
			{
				final L2Character target = (L2Character) activeChar.getTarget();
				attack(command, activeChar, target);
			}
		}
		else if (command.startsWith("admin_mobgroup_rnd"))
			setNormal(command, activeChar);
		else if (command.startsWith("admin_mobgroup_idle"))
			idle(command, activeChar);
		else if (command.startsWith("admin_mobgroup_return"))
			returnToChar(command, activeChar);
		else if (command.startsWith("admin_mobgroup_follow"))
			follow(command, activeChar, activeChar);
		else if (command.startsWith("admin_mobgroup_casting"))
			setCasting(command, activeChar);
		else if (command.startsWith("admin_mobgroup_nomove"))
			noMove(command, activeChar);
		else if (command.startsWith("admin_mobgroup_invul"))
			invul(command, activeChar);
		else if (command.startsWith("admin_mobgroup_teleport"))
			teleportGroup(command, activeChar);
		showMainPage(activeChar, command);
		return true;
	}

	/**
	 * @param activeChar
	 * @param command 
	 */
	private void showMainPage(final L2PcInstance activeChar, final String command)
	{
		final String filename = "mobgroup.htm";
		AdminHelpPage.showHelpPage(activeChar, filename);
	}

	private void returnToChar(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Incorrect command arguments.");
			return;
		}
		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.returnGroup(activeChar);
	}

	private void idle(final String command, final L2PcInstance activeChar)
	{
		int groupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Incorrect command arguments.");
			return;
		}
		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.setIdleMode();
	}

	private void setNormal(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Incorrect command arguments.");
			return;
		}
		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.setAttackRandom();
	}

	private void attack(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Incorrect command arguments.");
			return;
		}
		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.setAttackTarget(target);
	}

	private void follow(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		int groupId;
		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Incorrect command arguments.");
			return;
		}
		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);
		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}
		group.setFollowMode(target);
	}

	private void createGroup(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		int templateId;
		int mobCount;

		try
		{
			final String[] cmdParams = command.split(" ");

			groupId = Integer.parseInt(cmdParams[1]);
			templateId = Integer.parseInt(cmdParams[2]);
			mobCount = Integer.parseInt(cmdParams[3]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_create <group> <npcid> <count>");
			return;
		}

		if (MobGroupTable.getInstance().getGroup(groupId) != null)
		{
			activeChar.sendMessage("Mob group " + groupId + " already exists.");
			return;
		}

		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(templateId);

		if (template == null)
		{
			activeChar.sendMessage("Invalid NPC ID specified.");
			return;
		}

		final MobGroup group = new MobGroup(groupId, template, mobCount);
		MobGroupTable.getInstance().addGroup(groupId, group);

		activeChar.sendMessage("Mob group " + groupId + " created.");
	}

	private void removeGroup(final String command, final L2PcInstance activeChar)
	{
		int groupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_remove <groupId>");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		doAnimation(activeChar);
		group.unspawnGroup();

		if (MobGroupTable.getInstance().removeGroup(groupId))
			activeChar.sendMessage("Mob group " + groupId + " unspawned and removed.");
	}

	private void spawnGroup(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		boolean topos = false;
		int posx = 0;
		int posy = 0;
		int posz = 0;

		try
		{
			final String[] cmdParams = command.split(" ");
			groupId = Integer.parseInt(cmdParams[1]);

			try
			{ // we try to get a position
				posx = Integer.parseInt(cmdParams[2]);
				posy = Integer.parseInt(cmdParams[3]);
				posz = Integer.parseInt(cmdParams[4]);
				topos = true;
			}
			catch (final Exception e)
			{
				// no position given
			}
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_spawn <group> [ x y z ]");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		doAnimation(activeChar);

		if (topos)
			group.spawnGroup(posx, posy, posz);
		else
			group.spawnGroup(activeChar);

		activeChar.sendMessage("Mob group " + groupId + " spawned.");
	}

	private void unspawnGroup(final String command, final L2PcInstance activeChar)
	{
		int groupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_unspawn <groupId>");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		doAnimation(activeChar);
		group.unspawnGroup();

		activeChar.sendMessage("Mob group " + groupId + " unspawned.");
	}

	private void killGroup(final String command, final L2PcInstance activeChar)
	{
		int groupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_kill <groupId>");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		doAnimation(activeChar);
		group.killGroup(activeChar);
	}

	private void setCasting(final String command, final L2PcInstance activeChar)
	{
		int groupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_casting <groupId>");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		group.setCastMode();
	}

	private void noMove(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		String enabled;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			enabled = command.split(" ")[2];
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_nomove <groupId> <on|off>");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true"))
			group.setNoMoveMode(true);
		else if (enabled.equalsIgnoreCase("off") || enabled.equalsIgnoreCase("false"))
			group.setNoMoveMode(false);
		else
			activeChar.sendMessage("Incorrect command arguments.");
	}

	private void doAnimation(final L2PcInstance activeChar)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, 1008, 1, 4000, 0), 2250000/* 1500 */);
		activeChar.sendPacket(new SetupGauge(0, 4000));
	}

	private void attackGrp(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		int othGroupId;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			othGroupId = Integer.parseInt(command.split(" ")[2]);
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_attackgrp <groupId> <TargetGroupId>");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		final MobGroup othGroup = MobGroupTable.getInstance().getGroup(othGroupId);

		if (othGroup == null)
		{
			activeChar.sendMessage("Incorrect target group.");
			return;
		}

		group.setAttackGroup(othGroup);
	}

	private void invul(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		String enabled;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			enabled = command.split(" ")[2];
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_invul <groupId> <on|off>");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		if (enabled.equalsIgnoreCase("on") || enabled.equalsIgnoreCase("true"))
			group.setInvul(true);
		else if (enabled.equalsIgnoreCase("off") || enabled.equalsIgnoreCase("false"))
			group.setInvul(false);
		else
			activeChar.sendMessage("Incorrect command arguments.");
	}

	private void teleportGroup(final String command, final L2PcInstance activeChar)
	{
		int groupId;
		String targetPlayerStr = null;
		L2PcInstance targetPlayer = null;

		try
		{
			groupId = Integer.parseInt(command.split(" ")[1]);
			targetPlayerStr = command.split(" ")[2];

			if (targetPlayerStr != null)
				targetPlayer = L2World.getInstance().getPlayer(targetPlayerStr);

			if (targetPlayer == null)
				targetPlayer = activeChar;
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Usage: //mobgroup_teleport <groupId> [playerName]");
			return;
		}

		final MobGroup group = MobGroupTable.getInstance().getGroup(groupId);

		if (group == null)
		{
			activeChar.sendMessage("Invalid group specified.");
			return;
		}

		group.teleportGroup(activeChar);
	}

	private void showGroupList(final L2PcInstance activeChar)
	{
		final MobGroup[] mobGroupList = MobGroupTable.getInstance().getGroups();

		activeChar.sendMessage("======= <Mob Groups> =======");

		for (final MobGroup mobGroup : mobGroupList)
			activeChar.sendMessage(mobGroup.getGroupId() + ": " + mobGroup.getActiveMobCount() + " alive out of " + mobGroup.getMaxMobCount() + " of NPC ID " + mobGroup.getTemplate().npcId + " (" + mobGroup.getStatus() + ")");

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FRIEND_LIST_FOOTER));
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}