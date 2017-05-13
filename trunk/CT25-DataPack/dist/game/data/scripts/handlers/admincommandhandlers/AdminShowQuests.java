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
import java.sql.ResultSet;

import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ExShowQuestMark;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.QuestList;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import javolution.text.TextBuilder;

/**
 * @author Korvin con.close() change by Zoey76 24/02/2011
 */
public class AdminShowQuests implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_charquestmenu",
		"admin_setcharquest"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		final String[] cmdParams = command.split(" ");
		L2PcInstance target = null;
		L2Object targetObject = null;
		final String[] val = new String[4];
		val[0] = null;

		if (cmdParams.length > 1)
		{
			target = L2World.getInstance().getPlayer(cmdParams[1]);
			if (cmdParams.length > 2)
			{
				if (cmdParams[2].equals("0"))
				{
					val[0] = "var";
					val[1] = "Start";
				}
				if (cmdParams[2].equals("1"))
				{
					val[0] = "var";
					val[1] = "Started";
				}
				if (cmdParams[2].equals("2"))
				{
					val[0] = "var";
					val[1] = "Completed";
				}
				if (cmdParams[2].equals("3"))
					val[0] = "full";
				if (cmdParams[2].indexOf("_") != -1)
				{
					val[0] = "name";
					val[1] = cmdParams[2];
				}
				if (cmdParams.length > 3)
					if (cmdParams[3].equals("custom"))
					{
						val[0] = "custom";
						val[1] = cmdParams[2];
					}
			}
		}
		else
		{
			targetObject = activeChar.getTarget();

			if (targetObject != null && targetObject instanceof L2PcInstance)
				target = (L2PcInstance) targetObject;
		}

		if (target == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return false;
		}

		if (command.startsWith("admin_charquestmenu"))
		{
			if (val[0] != null)
				try
				{
					showquestmenu(target, activeChar, val);
				}
				catch (final Exception e)
				{
				}
			else
				showfirstquestmenu(target, activeChar);
		}
		else if (command.startsWith("admin_setcharquest"))
			if (cmdParams.length >= 5)
			{
				val[0] = cmdParams[2];
				val[1] = cmdParams[3];
				val[2] = cmdParams[4];
				if (cmdParams.length == 6)
					val[3] = cmdParams[5];
				try
				{
					setquestvar(target, activeChar, val);
				}
				catch (final Exception e)
				{
				}
			}
			else
				return false;
		return true;
	}

	private void showfirstquestmenu(final L2PcInstance target, final L2PcInstance actor)
	{
		final TextBuilder replyMSG = new TextBuilder("<html><body><table width=270>" + "<tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td width=180><center>Player: " + target.getName()
			+ "</center></td>" + "<td width=45><button value=\"Back\" action=\"bypass -h admin_admin6\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final int ID = target.getObjectId();

		replyMSG.append("Quest Menu for <font color=\"LEVEL\">" + target.getName() + "</font> (ID:" + ID + ")<br><center>");
		replyMSG.append("<table width=250><tr><td><button value=\"CREATED\" action=\"bypass -h admin_charquestmenu " + target.getName() + " 0\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"STARTED\" action=\"bypass -h admin_charquestmenu " + target.getName() + " 1\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"COMPLETED\" action=\"bypass -h admin_charquestmenu " + target.getName() + " 2\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><br><button value=\"All\" action=\"bypass -h admin_charquestmenu " + target.getName() + " 3\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><br><br>Manual Edit by Quest number:<br></td></tr>");
		replyMSG.append("<tr><td><edit var=\"qn\" width=50 height=15><br><button value=\"Edit\" action=\"bypass -h admin_charquestmenu " + target.getName() + " $qn custom\" width=50 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("</table></center></body></html>");
		adminReply.setHtml(replyMSG.toString());
		actor.sendPacket(adminReply);
	}

	@SuppressWarnings("resource")
	private void showquestmenu(final L2PcInstance target, final L2PcInstance actor, final String[] val)
	{
		Connection con = null;
		try
		{
			ResultSet rs;
			PreparedStatement req;
			final int ID = target.getObjectId();

			final TextBuilder replyMSG = new TextBuilder("<html><body>");
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			con = L2DatabaseFactory.getInstance().getConnection();

			if (val[0].equals("full"))
			{
				replyMSG.append("<table width=250><tr><td>Full Quest List for <font color=\"LEVEL\">" + target.getName() + "</font> (ID:" + ID + ")</td></tr>");
				req = con.prepareStatement("SELECT DISTINCT name FROM character_quests WHERE charId='" + ID + "' ORDER by name");
				req.execute();
				rs = req.getResultSet();
				while (rs.next())
					replyMSG.append("<tr><td><a action=\"bypass -h admin_charquestmenu " + target.getName() + " " + rs.getString(1) + "\">" + rs.getString(1) + "</a></td></tr>");
				replyMSG.append("</table></body></html>");
				L2DatabaseFactory.close(con);
			}
			else if (val[0].equals("name"))
			{
				final String[] states =
				{
					"CREATED",
					"STARTED",
					"COMPLETED"
				};
				final String state = states[target.getQuestState(val[1]).getState()];
				replyMSG.append("Character: <font color=\"LEVEL\">" + target.getName() + "</font><br>Quest: <font color=\"LEVEL\">" + val[1] + "</font><br>State: <font color=\"LEVEL\">" + state + "</font><br><br>");
				replyMSG.append("<center><table width=250><tr><td>Var</td><td>Value</td><td>New Value</td><td>&nbsp;</td></tr>");
				req = con.prepareStatement("SELECT var,value FROM character_quests WHERE charId='" + ID + "' and name='" + val[1] + "'");
				req.execute();
				rs = req.getResultSet();
				while (rs.next())
				{
					final String var_name = rs.getString(1);
					if (var_name.equals("<state>"))
						continue;
					replyMSG.append("<tr><td>" + var_name + "</td><td>" + rs.getString(2) + "</td><td><edit var=\"var" + var_name + "\" width=80 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + val[1] + " " + var_name + " $var"
						+ var_name + "\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td><button value=\"Del\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + val[1] + " " + var_name
						+ " delete\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
				}
				replyMSG.append("</table><br><br><table width=250><tr><td>Repeatable quest:</td><td>Unrepeatable quest:</td></tr>");
				replyMSG.append("<tr><td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + val[1] + " state COMLETED 1\" width=120 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				replyMSG.append("<td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + val[1] + " state COMLETED 0\" width=120 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
				replyMSG.append("</table><br><br><font color=\"ff0000\">Delete Quest from DB:</font><br><button value=\"Quest Delete\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + val[1]
					+ " state DELETE\" width=120 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				replyMSG.append("</center></body></html>");
				L2DatabaseFactory.close(con);
			}
			else if (val[0].equals("var"))
			{
				replyMSG.append("Character: <font color=\"LEVEL\">" + target.getName() + "</font><br>Quests with state: <font color=\"LEVEL\">" + val[1] + "</font><br>");
				replyMSG.append("<table width=250>");
				req = con.prepareStatement("SELECT DISTINCT name FROM character_quests WHERE charId='" + ID + "' and var='<state>' and value='" + val[1] + "'");
				req.execute();
				rs = req.getResultSet();
				while (rs.next())
					replyMSG.append("<tr><td><a action=\"bypass -h admin_charquestmenu " + target.getName() + " " + rs.getString(1) + "\">" + rs.getString(1) + "</a></td></tr>");
				replyMSG.append("</table></body></html>");
				L2DatabaseFactory.close(con);
			}
			else if (val[0].equals("custom"))
			{
				boolean exqdb = true;
				boolean exqch = true;
				final int qnumber = Integer.parseInt(val[1]);
				String state = null;
				String qname = null;
				QuestState qs = null;
				final String[] states =
				{
					"CREATED",
					"STARTED",
					"COMPLETED"
				};

				final Quest quest = QuestManager.getInstance().getQuest(qnumber);

				if (quest != null)
				{
					qname = quest.getName();
					qs = target.getQuestState(qname);
				}
				else
					exqdb = false;

				if (qs != null)
					state = states[qs.getState()];
				else
				{
					exqch = false;
					state = "N/A";
				}

				if (exqdb)
				{
					if (exqch)
					{
						replyMSG.append("Character: <font color=\"LEVEL\">" + target.getName() + "</font><br>Quest: <font color=\"LEVEL\">" + qname + "</font><br>State: <font color=\"LEVEL\">" + state + "</font><br><br>");
						replyMSG.append("<center><table width=250><tr><td>Var</td><td>Value</td><td>New Value</td><td>&nbsp;</td></tr>");
						req = con.prepareStatement("SELECT var,value FROM character_quests WHERE charId='" + ID + "' and name='" + qname + "'");
						req.execute();
						rs = req.getResultSet();
						while (rs.next())
						{
							final String var_name = rs.getString(1);
							if (var_name.equals("<state>"))
								continue;
							replyMSG.append("<tr><td>" + var_name + "</td><td>" + rs.getString(2) + "</td><td><edit var=\"var" + var_name + "\" width=80 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + qname + " " + var_name + " $var"
								+ var_name + "\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td><button value=\"Del\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + qname + " " + var_name
								+ " delete\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
						}
						replyMSG.append("</table><br><br><table width=250><tr><td>Repeatable quest:</td><td>Unrepeatable quest:</td></tr>");
						replyMSG.append("<tr><td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + qname + " state COMLETED 1\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
						replyMSG.append("<td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + qname + " state COMLETED 0\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
						replyMSG.append("</table><br><br><font color=\"ff0000\">Delete Quest from DB:</font><br><button value=\"Quest Delete\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + qname
							+ " state DELETE\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
						replyMSG.append("</center></body></html>");
						L2DatabaseFactory.close(con);
					}
					else
					{
						replyMSG.append("Character: <font color=\"LEVEL\">" + target.getName() + "</font><br>Quest: <font color=\"LEVEL\">" + qname + "</font><br>State: <font color=\"LEVEL\">" + state + "</font><br><br>");
						replyMSG.append("<center>Start this Quest for player:<br>");
						replyMSG.append("<button value=\"Create Quest\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + qnumber + " state CREATE\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><br>");
						replyMSG.append("<font color=\"ee0000\">Only for Unrepeateble quests:</font><br>");
						replyMSG.append("<button value=\"Create & Complete\" action=\"bypass -h admin_setcharquest " + target.getName() + " " + qnumber + " state CC\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><br>");
						replyMSG.append("</center></body></html>");
					}
				}
				else
					replyMSG.append("<center><font color=\"ee0000\">Quest with number </font><font color=\"LEVEL\">" + qnumber + "</font><font color=\"ee0000\"> doesn't exist!</font></center></body></html>");
			}
			adminReply.setHtml(replyMSG.toString());
			actor.sendPacket(adminReply);
		}
		catch (final Exception e)
		{
			actor.sendMessage("Error!");
			// e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void setquestvar(final L2PcInstance target, final L2PcInstance actor, final String[] val)
	{
		QuestState qs = target.getQuestState(val[0]);
		final String[] outval = new String[3];

		if (val[1].equals("state"))
		{
			if (val[2].equals("COMLETED"))
				qs.exitQuest(val[3].equals("1") ? true : false);
			else if (val[2].equals("DELETE"))
			{
				qs.getQuest();
				Quest.deleteQuestInDb(qs);
				target.sendPacket(new QuestList());
				target.sendPacket(new ExShowQuestMark(qs.getQuest().getId()));
			}
			else if (val[2].equals("CREATE"))
			{
				qs = QuestManager.getInstance().getQuest(Integer.parseInt(val[0])).newQuestState(target);
				qs.setState(State.STARTED);
				qs.set("cond", "1");
				target.sendPacket(new QuestList());
				target.sendPacket(new ExShowQuestMark(qs.getQuest().getId()));
				val[0] = qs.getQuest().getName();
			}
			else if (val[2].equals("CC"))
			{
				qs = QuestManager.getInstance().getQuest(Integer.parseInt(val[0])).newQuestState(target);
				qs.exitQuest(false);
				target.sendPacket(new QuestList());
				target.sendPacket(new ExShowQuestMark(qs.getQuest().getId()));
				val[0] = qs.getQuest().getName();
			}
		}
		else
		{
			if (val[2].equals("delete"))
				qs.unset(val[1]);
			else
				qs.set(val[1], val[2]);
			target.sendPacket(new QuestList());
			target.sendPacket(new ExShowQuestMark(qs.getQuest().getId()));
		}
		actor.sendMessage("");
		outval[0] = "name";
		outval[1] = val[0];
		try
		{
			showquestmenu(target, actor, outval);
		}
		catch (final Exception e)
		{
		} 
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}