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
package ct25.xtreme.gameserver.model.actor.instance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.datatables.MapRegionTable;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.entity.event.Hitman;
import ct25.xtreme.gameserver.model.entity.event.PlayerToAssasinate;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;

public class L2HitmanInstance extends L2Npc
{
	private static final Integer maxPerPage = Config.HITMAN_MAX_PER_PAGE;
	private DecimalFormat f = new DecimalFormat(",##0,000");
	
	public L2HitmanInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String currentcommand = st.nextToken();
		
		try
		{
			if (currentcommand.startsWith("showList"))
			{
				int p = Integer.parseInt(st.nextToken());
				parseWindow(player, showListWindow(p));
			}
			else if (currentcommand.startsWith("showInfo"))
			{
				int playerId = Integer.parseInt(st.nextToken());
				int p = Integer.parseInt(st.nextToken());
				parseWindow(player, showInfoWindow(playerId, p));
			}
			else if (currentcommand.startsWith("showAddList"))
			{
				parseWindow(player, showAddList(generateListCurrency()));
			}
			else if (currentcommand.startsWith("addList"))
			{
				String name = st.nextToken();
				Long bounty = Long.parseLong(st.nextToken());
				Integer itemId = Hitman.getInstance().getCurrencyId(st.nextToken());
				if (bounty <= 0) bounty = 1L;
				Hitman.getInstance().putHitOn(player, name, bounty, itemId);
			}
			else if (currentcommand.startsWith("removeList"))
			{
				String name = st.nextToken();
				Hitman.getInstance().cancelAssasination(name, player);
				showChatWindow(player, 0);
			}
			else
				super.onBypassFeedback(player, command);
		}
		catch (Exception e)
		{
			player.sendChatMessage(player.getObjectId(), Say2.TELL, getName(), "Make sure you filled the fields correctly.");
		}
	}
	
	public void parseWindow(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npc_name%", getName());
		html.replace("%player_name%", player.getName());
		player.sendPacket(html);
	}
	
	private String generateListCurrency()
	{
		String text = "";
		for (String itemName : Hitman.getInstance().getCurrencys().keySet())
			text += itemName.trim() + ";";
		return text.substring(0, text.length() - 1).trim();
	}
	
	private String generateButtonPage(int page, int select)
	{
		String text = "";
		
		if (page == 1) return text;
		
		text += "<table><tr>";
		for (int i = 1; i <= page; i++)
		{
			String v = (i == select ? String.valueOf(i) + "*" : String.valueOf(i));
			text += "<td><button value=\"P" + v + "\"" +
				"action=\"bypass -h npc_%objectId%_showList " + i + "\" back=\"L2UI_ct1.button_df\"" +
				"fore=\"L2UI_ct1.button_df\" width=35 height=21></td>";
			text += (i % 8 == 0 ? "</tr><tr>" : "");
		}
		text += "</tr></table>";
		return text;
	}
	
	public NpcHtmlMessage showAddList(String list)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder content = new StringBuilder("");		
		content.append("<html><body>");
		content.append("<center><img src=L2Font-e.mini_logo-e width=245 height=80>");
		content.append("<img src=L2UI_CH3.herotower_deco width=256 height=32> <br>");
		content.append("You can put a hit on someone here:");
		content.append("<table width=256>");
		content.append("<tr><td width=256 align=\"center\">Target Name<br1><edit var=\"name\" width=150 height=15><br></td></tr>");
		content.append("<tr><td width=256 align=\"center\">Currency<br1><combobox width=180 var=\"currency\" list=" + list + "><br></td></tr>");
		content.append("<tr><td width=256 align=\"center\">Bounty<br1><edit var=\"bounty\" width=150 height=15><br></td></tr>");
		content.append("</table>");
		content.append("<br>");
		content.append("<button value=\"Add\" " +
				"action=\"bypass -h npc_%objectId%_addList $name $bounty $currency\" " +
				"back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\" width=95 height=21>");
		content.append("<br>If you wish to cancel a hit:<br1>");
		content.append("<table width=240>");
		content.append("<tr>");
		content.append("<td width=60>Name:</td>");
		content.append("<td><edit var=\"remname\" width=110 height=15></td>");
		content.append("<td><button value=\"Remove\" action=\"bypass -h npc_%objectId%_removeList $remname\" " +
				"back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\" width=60 height=21></td>");
		content.append("</tr>");
		content.append("</table>");
		content.append("<br><br>");
		content.append("<button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat 0\" " +
				"back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\" width=80 height=21>");
		content.append("<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br1>" +
				"<img src=l2ui.bbs_lineage2 height=16 width=80>");
		content.append("</center>");
		content.append("</body>");
		content.append("</html>");	
		html.setHtml(content.toString());
		return html;
	}
	
	public NpcHtmlMessage showListWindow(int p)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder content = new StringBuilder("<html><body><center>");
	
		content.append("<img src=L2Font-e.mini_logo-e width=245 height=80>");
		content.append("<img src=L2UI_CH3.herotower_deco width=256 height=32>");
		content.append("<br>");
		
		content.append("<table>" +
				"<tr><td align=\"center\"><font color=AAAAAA>Agency - Jobs</font></td></tr>" +
				"<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>" +
				"<tr><td align=\"center\">");
		ArrayList<PlayerToAssasinate> list = new ArrayList<>();
		list.addAll(Hitman.getInstance().getTargetsOnline().values());
		
		if (list.size() > 0)
		{
			int countPag = (int)Math.ceil((double)list.size() / (double)maxPerPage);
			int startRow = (maxPerPage * (p - 1));
			int stopRow = startRow + maxPerPage;
			int countReg = 0;
			String pages = generateButtonPage(countPag, p);
			
			content.append(pages);
			content.append("<table bgcolor=\"000000\">" +
					"<tr>" +
					"<td width=\"60\" align=\"center\">Target</td>" +
					"<td width=\"125\" align=\"center\"><font color=\"F2FEBF\">Bounty</font></td>" +
					"<td width=\"115\" align=\"center\"><font color=\"00CC00\">Currency</font></td>" +
					"</tr>");
			
			for (PlayerToAssasinate pta : list)
			{			
				if (pta == null) break;
				
				if (countReg >= stopRow) break;
				
				if (countReg >= startRow && countReg < stopRow)
					content.append("<tr>" +
						"<td align=\"center\"><button value=\"Info\" action=\"bypass -h npc_%objectId%_showInfo " + pta.getObjectId() + " " + p + "\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\" width=45 height=18></td>" +
						"<td align=\"center\">" + (pta.getBounty() > 999 ? f.format(pta.getBounty()) : pta.getBounty()) + "</td>" +
						"<td align=\"center\">"+ pta.getItemName() + "</td>" +
						"</tr>");
				
				countReg++;
			}
			
			content.append("<tr>" +
					"<td height=\"3\"> </td>" +
					"<td height=\"3\"> </td>" +
					"<td height=\"3\"> </td>" +
					"</tr></table>");
			
			content.append(pages);
		}
		else
			content.append("No target is currently online.");
		
		content.append("</td></tr>" +
				"<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>" +
				"</table>");
		content.append("<button value=\"Back\"" +
				"action=\"bypass -h npc_%objectId%_Chat 0\" back=\"L2UI_ct1.button_df\"" +
				"fore=\"L2UI_ct1.button_df\" width=55 height=21>");
		content.append("<br><font color=\"cc9900\"><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></font><br1>");
		content.append("<img src=l2ui.bbs_lineage2 height=16 width=80>");
		content.append("</center></body></html>");
		html.setHtml(content.toString());
		
		return html;
	}
	
	public NpcHtmlMessage showInfoWindow(int objectId, int p)
	{	
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		PlayerToAssasinate pta = Hitman.getInstance().getTargets().get(objectId);
		L2PcInstance target = L2World.getInstance().getPlayer(pta.getName());
		MapRegionTable map = MapRegionTable.getInstance();
		StringBuilder content = new StringBuilder("<html><body><center>");
		
		content.append("<img src=L2Font-e.mini_logo-e width=245 height=80>");
		content.append("<img src=L2UI_CH3.herotower_deco width=256 height=32>");
		content.append("<table>" +
				"<tr><td align=\"center\"><font color=AAAAAA>Target: " + pta.getName() + "</font></td></tr>" +
				"<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>" +
				"<tr><td align=\"center\">");
		
		if (target != null)
		{
			content.append("<table bgcolor=\"000000\"><tr><td>");			
			content.append("We are sorry %player_name% if we didn't manage to get more precise information.<br1> But you will have to do with this.<br>");
			content.append("<br><br>");
			content.append("Target: <font color=\"D74B18\">" + pta.getName() + "</font><br1>");
			content.append("Bounty: <font color=\"D74B18\">" + (pta.getBounty() > 999 ? f.format(pta.getBounty()) : pta.getBounty()) + " " + pta.getItemName() + "</font><br1>");
			content.append("Last Town: <font color=\"D74B18\">" + target.getLastTownName() + "</font><br1>");
			content.append("Current Known Location: <font color=\"D74B18\">" + map.getClosestTownName(target) + " Teritory</font>");
			content.append("</td></tr></table>");
		}
		else
			content.append("Player went offline.");
		
		content.append("</td></tr>" +
				"<tr><td align=\"center\"><img src=\"L2UI.SquareWhite\" width=\"261\" height=\"1\"></td></tr>" +
				"</table>");
		content.append("<button value=\"Back\" action=\"bypass -h npc_%objectId%_showList " + p + "\" back=\"L2UI_ct1.button_df\" " +
				"fore=\"L2UI_ct1.button_df\" width=100 height=21>");
		content.append("<br><font color=\"cc9900\"><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></font><br1>");
		content.append("<img src=l2ui.bbs_lineage2 height=16 width=80>");
		content.append("</center></body></html>");
		html.setHtml(content.toString());
		
		return html;
	}
}
