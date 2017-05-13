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

import java.io.File;

import javax.script.ScriptException;

import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.scripting.L2ScriptEngineManager;

public class AdminQuest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_quest_reload",
		"admin_script_load"
	};

	/**
	 * @see ct25.xtreme.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, ct25.xtreme.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;
			
		// syntax will either be:
		// //quest_reload <id>
		// //quest_reload <questName>
		// The questName MUST start with a non-numeric character for this to work,
		// regardless which of the two formats is used.
		// Example: //quest_reload orc_occupation_change_1
		// Example: //quest_reload chests
		// Example: //quest_reload SagasSuperclass
		// Example: //quest_reload 12
		// syntax will either be:
		// //quest_reload <id>
		// //quest_reload <questName>
		// The questName MUST start with a non-numeric character for this to work,
		// regardless which of the two formats is used.
		// Example: //quest_reload orc_occupation_change_1
		// Example: //quest_reload chests
		// Example: //quest_reload SagasSuperclass
		// Example: //quest_reload 12
		if (command.startsWith("admin_quest_reload"))
		{
			final String[] parts = command.split(" ");
			if (parts.length < 2)
				activeChar.sendMessage("Usage: //quest_reload <questFolder>.<questSubFolders...>.questName> or //quest_reload <id>");
			else
				// try the first param as id
				try
				{
					final int questId = Integer.parseInt(parts[1]);
					if (QuestManager.getInstance().reload(questId))
						activeChar.sendMessage("Quest Reloaded Successfully.");
					else
						activeChar.sendMessage("Quest Reloaded Failed");
				}
				catch (final NumberFormatException e)
				{
					if (QuestManager.getInstance().reload(parts[1]))
						activeChar.sendMessage("Quest Reloaded Successfully.");
					else
						activeChar.sendMessage("Quest Reloaded Failed");
				}
		}
		// script load should NOT be used in place of reload. If a script is already loaded
		// successfully, quest_reload ought to be used. The script_load command should only
		// be used for scripts that failed to load altogether (eg. due to errors) or that
		// did not at all exist during server boot. Using script_load to re-load a previously
		// loaded script may cause unpredictable script flow, minor loss of data, and more.
		// This provides a way to load new scripts without having to reboot the server.
		else if (command.startsWith("admin_script_load"))
		{
			final String[] parts = command.split(" ");
			if (parts.length < 2)
				// activeChar.sendMessage("Example: //script_load <questFolder>/<questSubFolders...>/<filename>.<ext> ");
				activeChar.sendMessage("Example: //script_load quests/SagasSuperclass/__init__.py");
			else
			{
				File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, parts[1]);
				// Trying to reload by script name.
				if (!file.exists())
				{
					final Quest quest = QuestManager.getInstance().getQuest(parts[1]);
					if (quest != null)
						file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, quest.getClass().getName().replaceAll("\\.", "/") + ".java");
				}

				// Reloading by full path
				if (file.isFile())
					try
					{
						L2ScriptEngineManager.getInstance().executeScript(file);

						// This part should be called only when the script is successfuly loaded.
						activeChar.sendMessage("Script Successfully Loaded.");
					}
					catch (final ScriptException e)
					{
						activeChar.sendMessage("Failed loading: " + parts[1]);
						L2ScriptEngineManager.getInstance().reportScriptFileError(file, e);
					}
					catch (final Exception e)
					{
						activeChar.sendMessage("Failed loading: " + parts[1]);
					}
				else
					activeChar.sendMessage("File Not Found: " + parts[1]);
			}

		}
		return true;
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
