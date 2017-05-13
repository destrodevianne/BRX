package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.SkillCoolTime;
import ct25.xtreme.gameserver.util.GMAudit;
import ct25.xtreme.util.StringUtil;

public class AdminBuffs implements IAdminCommandHandler
{
	private final static int PAGE_LIMIT = 20;

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_getbuffs",
		"admin_stopbuff",
		"admin_stopallbuffs",
		"admin_areacancel",
		"admin_removereuse"
	};

	@Override
	public boolean useAdminCommand(String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.startsWith("admin_getbuffs"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();

			if (st.hasMoreTokens())
			{
				L2PcInstance player = null;
				final String playername = st.nextToken();

				try
				{
					player = L2World.getInstance().getPlayer(playername);
				}
				catch (final Exception e)
				{
				}

				if (player != null)
				{
					int page = 1;
					if (st.hasMoreTokens())
						page = Integer.parseInt(st.nextToken());
					showBuffs(activeChar, player, page);
					return true;
				}
				activeChar.sendMessage("The player " + playername + " is not online");
				return false;
			}
			else if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2Character)
			{
				showBuffs(activeChar, (L2Character) activeChar.getTarget(), 1);
				return true;
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
		}

		else if (command.startsWith("admin_stopbuff"))
			try
			{
				final StringTokenizer st = new StringTokenizer(command, " ");

				st.nextToken();
				final int objectId = Integer.parseInt(st.nextToken());
				final int skillId = Integer.parseInt(st.nextToken());

				removeBuff(activeChar, objectId, skillId);
				return true;
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Failed removing effect: " + e.getMessage());
				activeChar.sendMessage("Usage: //stopbuff <objectId> <skillId>");
				return false;
			}
		else if (command.startsWith("admin_stopallbuffs"))
			try
			{
				final StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				final int objectId = Integer.parseInt(st.nextToken());
				removeAllBuffs(activeChar, objectId);
				return true;
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Failed removing all effects: " + e.getMessage());
				activeChar.sendMessage("Usage: //stopallbuffs <objectId>");
				return false;
			}
		else if (command.startsWith("admin_areacancel"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			final String val = st.nextToken();
			try
			{
				final int radius = Integer.parseInt(val);

				for (final L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
					if (knownChar instanceof L2PcInstance && !knownChar.equals(activeChar))
						knownChar.stopAllEffects();
					
				activeChar.sendMessage("All effects canceled within raidus " + radius);
				return true;
			}
			catch (final NumberFormatException e)
			{
				activeChar.sendMessage("Usage: //areacancel <radius>");
				return false;
			}
		}
		else if (command.startsWith("admin_removereuse"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();

			L2PcInstance player = null;
			if (st.hasMoreTokens())
			{
				final String playername = st.nextToken();

				try
				{
					player = L2World.getInstance().getPlayer(playername);
				}
				catch (final Exception e)
				{
				}

				if (player == null)
				{
					activeChar.sendMessage("The player " + playername + " is not online.");
					return false;
				}
			}
			else if (activeChar.getTarget() instanceof L2PcInstance)
				player = (L2PcInstance) activeChar.getTarget();
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}

			try
			{
				player.getReuseTimeStamp().clear();
				player.getDisabledSkills().clear();
				player.sendPacket(new SkillCoolTime(player));
				activeChar.sendMessage("Skill reuse was removed from " + player.getName() + ".");
				return true;
			}
			catch (final NullPointerException e)
			{
				return false;
			}
		}
		else
			return true;

	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	public void showBuffs(final L2PcInstance activeChar, final L2Character target, final int page)
	{
		final L2Effect[] effects = target.getAllEffects();

		if (page > effects.length / PAGE_LIMIT + 1 || page < 1)
			return;

		int max = effects.length / PAGE_LIMIT;
		if (effects.length > PAGE_LIMIT * max)
			max++;

		final StringBuilder html = StringUtil.startAppend(500 + effects.length
			* 200, "<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center><font color=\"LEVEL\">Effects of ", target.getName(), "</font></td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><table width=\"100%\"><tr><td width=200>Skill</td><td width=30>Rem. Time</td><td width=70>Action</td></tr>");

		final int start = (page - 1) * PAGE_LIMIT;
		final int end = Math.min((page - 1) * PAGE_LIMIT + PAGE_LIMIT, effects.length);

		for (int i = start; i < end; i++)
		{
			final L2Effect e = effects[i];
			if (e != null)
				StringUtil.append(html, "<tr><td>", e.getSkill().getName(), "</td><td>", e.getSkill().isToggle() ? "toggle" : e.getAbnormalTime() - e.getTime()
					+ "s", "</td><td><button value=\"Remove\" action=\"bypass -h admin_stopbuff ", Integer.toString(target.getObjectId()), " ", String.valueOf(e.getSkill().getId()), "\" width=60 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}

		html.append("</table><table width=300 bgcolor=444444><tr>");
		for (int x = 0; x < max; x++)
		{
			final int pagenr = x + 1;
			if (page == pagenr)
			{
				html.append("<td>Page ");
				html.append(pagenr);
				html.append("</td>");
			}
			else
			{
				html.append("<td><a action=\"bypass -h admin_getbuffs ");
				html.append(target.getName());
				html.append(" ");
				html.append(x + 1);
				html.append("\"> Page ");
				html.append(pagenr);
				html.append(" </a></td>");
			}
		}

		html.append("</tr></table>");

		StringUtil.append(html, "<br><center><button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs ", Integer.toString(target.getObjectId()), "\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></html>");

		final NpcHtmlMessage ms = new NpcHtmlMessage(1);
		ms.setHtml(html.toString());
		activeChar.sendPacket(ms);

		if (Config.GMAUDIT)
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "getbuffs", target.getName() + " (" + Integer.toString(target.getObjectId()) + ")", "");
	}

	private void removeBuff(final L2PcInstance activeChar, final int objId, final int skillId)
	{
		L2Character target = null;
		try
		{
			target = (L2Character) L2World.getInstance().findObject(objId);
		}
		catch (final Exception e)
		{
		}

		if (target != null && skillId > 0)
		{
			final L2Effect[] effects = target.getAllEffects();

			for (final L2Effect e : effects)
				if (e != null && e.getSkill().getId() == skillId)
				{
					e.exit();
					activeChar.sendMessage("Removed " + e.getSkill().getName() + " level " + e.getSkill().getLevel() + " from " + target.getName() + " (" + objId + ")");
				}
			showBuffs(activeChar, target, 1);
			if (Config.GMAUDIT)
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopbuff", target.getName() + " (" + objId + ")", Integer.toString(skillId));
		}
	}

	private void removeAllBuffs(final L2PcInstance activeChar, final int objId)
	{
		L2Character target = null;
		try
		{
			target = (L2Character) L2World.getInstance().findObject(objId);
		}
		catch (final Exception e)
		{
		}

		if (target != null)
		{
			target.stopAllEffects();
			activeChar.sendMessage("Removed all effects from " + target.getName() + " (" + objId + ")");
			showBuffs(activeChar, target, 1);
			if (Config.GMAUDIT)
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopallbuffs", target.getName() + " (" + objId + ")", "");
		}
	}
}