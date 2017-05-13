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
package custom.BufferNPC;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.complement.NpcHtmlDefault;
import ct25.xtreme.gameserver.datatables.ItemTable;
import ct25.xtreme.gameserver.datatables.ModsBufferSchemeTable;
import ct25.xtreme.gameserver.datatables.ModsBufferSchemeTable.SchemeContent;
import ct25.xtreme.gameserver.datatables.ModsBufferSchemeTable.SchemePlayer;
import ct25.xtreme.gameserver.datatables.ModsBufferSkillTable;
import ct25.xtreme.gameserver.datatables.ModsBufferSkillTable.BufferSkill;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.clientpackets.Say2;

/**
 * @author L0ngh0rn
 */
public class BufferNPC extends Quest
{
	private final static int NPC = Config.BUFFER_NPC_ID;
	private final static String NAME_QUEST = BufferNPC.class.getSimpleName();
	private final static DecimalFormat f = new DecimalFormat(",##0,000");
	private final static NpcHtmlDefault nhd = new NpcHtmlDefault();
	private final static ModsBufferSchemeTable mbs = ModsBufferSchemeTable.getInstance();
	private final static ModsBufferSkillTable mbsi = ModsBufferSkillTable.getInstance();

	public BufferNPC(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(NPC);
		addStartNpc(NPC);
		addTalkId(NPC);
	}

	public static void main(final String[] args)
	{
		new BufferNPC(-1, NAME_QUEST, "custom");
	}

	private String title(final QuestState st)
	{
		String htmltext = title();
		htmltext += "Hello, " + st.getPlayer().getName() + "!<br1>";
		return htmltext;
	}

	public String title()
	{
		return nhd.title("Buffer Manager", "Buffer Information");
	}

	public String footer()
	{
		return nhd.footer("NPC Buffer", "v3.7");
	}

	public String button(final String value, final String event, final int w, final int h, final int type, final boolean revert)
	{
		return nhd.button(NAME_QUEST, value, event, w, h, type, revert);
	}

	public String link(final String value, final String event, final String color)
	{
		return nhd.link(NAME_QUEST, value, event, color);
	}

	public String topic(final String title)
	{
		return nhd.topic("[ " + title + " ]");
	}

	public String formatNumber(final Long number)
	{
		return number > 999 ? f.format(number) : String.valueOf(number);
	}

	private String getNameItem(final int itemId)
	{
		return ItemTable.getInstance().getTemplate(itemId).getName();
	}

	private String generateButtonPage(final int page, final int select, final String group, final int schemeId)
	{
		String text = "";
		if (page == 1)
			return text;
		text += "<center><table><tr>";
		for (int i = 1; i <= page; i++)
		{
			text += "<td>" + button("P" + i, "viewSkillGroup " + group + " " + schemeId + " " + i, 35, 20, 5, i == select ? true : false) + "</td>";
			text += i % 8 == 0 ? "</tr><tr>" : "";
		}
		text += "</tr></table></center>";
		return text;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return page(st, "");
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		final String paramEvent[] = event.split(" ");
		String action = "page", param1 = "", param2 = "", param3 = "", param4 = "", param5 = "", param6 = "", param7 = "";
		final int size = paramEvent.length;

		if (size >= 1)
			action = paramEvent[0];
		if (size >= 2)
			param1 = paramEvent[1];
		if (size >= 3)
			param2 = paramEvent[2];
		if (size >= 4)
			param3 = paramEvent[3];
		if (size >= 5)
			param4 = paramEvent[4];
		if (size >= 6)
			param5 = paramEvent[5];
		if (size >= 7)
			param6 = paramEvent[6];
		if (size >= 8)
			param7 = paramEvent[7];

		String htmltext = "";
		if (action.equals("page"))
			htmltext = page(st, "");
		else if (action.equals("execBuff"))
			htmltext = execBuff(st, mbs.getSchemeContent(Integer.parseInt(param1)), Integer.parseInt(param2));
		else if (action.equals("execSelectBuff"))
			htmltext = execSelectBuff(st, param1, Integer.parseInt(param2), Integer.parseInt(param3), Integer.parseInt(param4), Integer.parseInt(param5), Integer.parseInt(param6), param7.equalsIgnoreCase("info") ? true : false);
		else if (action.equals("removeBuff"))
			htmltext = removeBuff(st, 1);
		else if (action.equals("removeBuffPet"))
			htmltext = removeBuff(st, 2);
		else if (action.equals("recover"))
			htmltext = recover(st, 1);
		else if (action.equals("recoverPet"))
			htmltext = recover(st, 2);
		else if (action.equals("newSchemePage"))
			htmltext = newSchemePage(st, "");
		else if (action.equals("addScheme"))
		{
			final int type = param1.equals("Pet") ? 1 : 0;
			if (param2.length() > 7 && param2.length() != 0)
				htmltext = newSchemePage(st, "Name invalid!");
			else
			{
				final String msg = mbs.addScheme(st.getPlayer(), type, param2);
				if (msg.equals(""))
					htmltext = viewGroup(st, mbs.getAddSchemeId(st.getPlayer().getObjectId(), param2, type));
				else
					htmltext = newSchemePage(st, msg);
			}
		}
		else if (action.equals("viewGroup"))
			htmltext = viewGroup(st, param1.equals("manual") ? -1 : Integer.parseInt(param1));
		else if (action.equals("delSchemePage"))
			htmltext = delSchemePage(st, "");
		else if (action.equals("editSchemePage"))
			htmltext = editSchemePage(st);
		else if (action.equals("delScheme"))
			htmltext = delSchemePage(st, mbs.delScheme(st.getPlayer(), Integer.parseInt(param1), Integer.parseInt(param3)));
		else if (action.equals("viewSkillGroup"))
			htmltext = viewSkillGroup(st, param1, Integer.parseInt(param2), Integer.parseInt(param3), "");
		else if (action.equals("viewSkillInfo"))
			htmltext = viewSkillInfo(st, param1, Integer.parseInt(param2), Integer.parseInt(param3), Integer.parseInt(param4), Integer.parseInt(param5));
		else if (action.equals("addSkillScheme") || action.equals("delSkillScheme"))
		{
			if (size < 6)
				return viewSkillGroup(st, param1, Integer.parseInt(param2), 1, "Waiting for more parameters!");

			final Integer schemeId = Integer.parseInt(param2);
			final Integer skillId = Integer.parseInt(param3);
			final Integer skillLevel = Integer.parseInt(param4);

			String msg = "";
			if (action.equals("addSkillScheme"))
				msg = mbs.addContent(schemeId, skillId, skillLevel);
			else if (action.equals("delSkillScheme"))
				msg = mbs.delContent(schemeId, skillId, skillLevel);
			
			if (param6.equals("info"))
				htmltext = viewSkillInfo(st, param1, schemeId, skillId, skillLevel, Integer.parseInt(param5));
			else
				htmltext = viewSkillGroup(st, param1, schemeId, Integer.parseInt(param5), msg);
		}
		return htmltext;
	}

	private String page(final QuestState st, final String msg)
	{
		String htmltext = "";
		htmltext += title(st);
		if (st.getPlayer().getLevel() < Config.BUFFER_NPC_MIN_LEVEL)
			htmltext += "Minimum Level is: " + String.valueOf(Config.BUFFER_NPC_MIN_LEVEL);
		else if (st.getPlayer().getKarma() > 0)
			htmltext += "You have too much karma!<br>Come back,<br>when you don't have any karma!";
		else if (st.getPlayer().getPvpFlag() > 0)
			htmltext += "You can't buff while you are flagged!<br>Wait some time and try again!";
		else if (st.getPlayer().isAttackingNow())
			htmltext += "You can't buff while you are attacking!<br>Stop your fight and try again!";
		else
		{
			htmltext += "<center>";
			if (msg != "")
				htmltext += "<font color=\"LEVEL\">" + msg + "</font>";
			htmltext += loadSchemePlayer(st.getPlayer());
			htmltext += loadComplement();
			htmltext += "</center>";
		}
		htmltext += footer();
		return htmltext;
	}

	private String loadSchemePlayer(final L2PcInstance activeChar)
	{
		String htmltext = "";
		if (Config.BUFFER_NPC_ENABLE_SCHEME)
		{
			htmltext += mbs.hasScheme(activeChar) ? loadScheme(activeChar, "My Scheme", ActionType.VIEW) : "";
			htmltext += loadManagerScheme(activeChar);
		}
		if (Config.BUFFER_NPC_ENABLE_SELECT)
			htmltext += loadManualBuff();
		if (Config.BUFFER_NPC_ENABLE_READY)
			htmltext += loadScheme(null, "Scheme Ready", ActionType.VIEW);
		if (!Config.BUFFER_NPC_ENABLE_SCHEME && !Config.BUFFER_NPC_ENABLE_SELECT && !Config.BUFFER_NPC_ENABLE_READY)
			htmltext += "<html><body>No option active!</body></html>";
		return htmltext;
	}

	private String loadManualBuff()
	{
		String htmltext = "";
		htmltext += topic("Manual");
		htmltext += "<center>";
		htmltext += button("Select Buff", "viewGroup manual", 100, 20, 6, false);
		htmltext += "</center><br1>";
		return htmltext;
	}

	private String loadManagerScheme(final L2PcInstance activeChar)
	{
		String htmltext = "";
		htmltext += topic("Manager Scheme");
		htmltext += "<table width=\"258\" align=\"center\">";
		htmltext += "<tr>";
		htmltext += "<td width=\"86\" align=\"center\">" + button("New", "newSchemePage", 70, 20, 3, false) + "</td>";
		if (mbs.hasScheme(activeChar))
		{
			htmltext += "<td width=\"86\" align=\"center\">" + button("Edit", "editSchemePage", 70, 20, 1, false) + "</td>";
			htmltext += "<td width=\"86\" align=\"center\">" + button("Del", "delSchemePage", 70, 20, 4, false) + "</td>";
		}
		htmltext += "</tr>";
		htmltext += "</table><br1>";
		return htmltext;
	}

	private String loadScheme(final L2PcInstance activeChar, final String title, final ActionType action)
	{
		String htmltext = "";
		htmltext += topic(title);
		htmltext += "<table width=\"261\" align=\"center\">";
		htmltext += "<tr>";
		int i = 0;
		if (mbs.hasScheme(activeChar))
			for (final SchemePlayer sp : mbs.getSchemePlayer(activeChar))
			{
				String event;
				final int type = sp.getType() == 0 ? 2 : 3;
				if (type == 3 && !Config.BUFFER_NPC_ENABLE_PET)
					continue;

				switch (action)
				{
					case DEL:
						event = "delScheme";
						break;
					case EDIT:
						event = "viewGroup";
						break;
					case VIEW:
					case EXEC:
					default:
						if (!mbs.hasSkillScheme(sp.getId()) && sp.getPlayerId() != 0)
							event = "viewGroup";
						else
							event = "execBuff";
						break;
				}
				htmltext += "<td width=\"86\" align=\"center\">";
				htmltext += button(sp.getName(), event + " " + sp.getId() + " " + sp.getType() + " " + i, 78, 20, type, false);
				htmltext += "</td>";
				i++;
				htmltext += i % 3 == 0 ? "</tr><tr>" : "";
			}
		htmltext += "</tr>";
		htmltext += "</table><br1>";
		return htmltext;
	}

	private String loadComplement()
	{
		String htmltext = "";
		htmltext += topic("Complement");
		htmltext += "<table width=\"260\" align=\"center\">";
		htmltext += "<tr>";
		if (Config.BUFFER_NPC_ENABLE_RECOVER)
			htmltext += "<td width=\"130\" align=\"center\">" + button("Recover HP/MP/CP", "recover", 125, 20, 2, false) + "</td>";
		if (Config.BUFFER_NPC_ENABLE_REMOVE)
			htmltext += "<td width=\"130\" align=\"center\">" + button("Remove Buffs", "removeBuff", 125, 20, 2, false) + "</td>";
		htmltext += "</tr>";
		if (Config.BUFFER_NPC_ENABLE_PET)
		{
			htmltext += "<tr>";
			if (Config.BUFFER_NPC_ENABLE_RECOVER)
				htmltext += "<td width=\"130\" align=\"center\">" + button("Recover HP/MP Pet", "recoverPet", 125, 20, 3, false) + "</td>";
			if (Config.BUFFER_NPC_ENABLE_REMOVE)
				htmltext += "<td width=\"130\" align=\"center\">" + button("Remove Buffs Pet", "removeBuffPet", 125, 20, 3, false) + "</td>";
			htmltext += "</tr>";
		}
		htmltext += "</table><br1>";
		return htmltext;
	}

	private String execBuff(final QuestState st, final List<SchemeContent> list, final Integer type)
	{
		final L2Character object = type == 0 || type == 2 ? st.getPlayer() : st.getPlayer().getPet();
		if (object == null)
			return page(st, "Try again!");
		for (final SchemeContent sc : list)
		{
			final BufferSkill bs = mbsi.getSkillInfo(sc.getSkillId() + "-" + sc.getSkillLevel());
			final String skillInfo = "(" + bs.getName() + " <font color=\"FFE545\">" + bs.getComp() + "</font> Amount: " + formatNumber(bs.getFee_amount()) + " " + getNameItem(bs.getFee_id()) + ")";
			if (!st.getPlayer().isGM() && (bs.getPlayer() == 2 || bs.getPlayer() == 1 && !st.getPlayer().isPlayer()))
			{
				mbs.delContent(sc.getId(), sc.getSkillId(), sc.getSkillLevel());
				continue;
			}
			if (!mbs.payBuffFee(st.getPlayer(), bs.getFee_id(), bs.getFee_amount()))
			{
				st.getPlayer().sendChatMessage(st.getPlayer().getObjectId(), Say2.TELL, "Buffer Scheme", "You need money to use the buff: " + skillInfo);
				continue;
			}
			SkillTable.getInstance().getInfo(sc.getSkillId(), sc.getSkillLevel()).getEffects(object, object);
		}
		return page(st, "Apply Buff!");
	}

	private String execSelectBuff(final QuestState st, final String group, final Integer schemeId, final Integer skillId, final Integer skillLvl, final Integer page, final Integer type, final boolean info)
	{
		final List<SchemeContent> list = new ArrayList<>();
		list.add(new SchemeContent(-1, skillId, skillLvl));
		execBuff(st, list, type);
		if (info)
			return viewSkillInfo(st, group, schemeId, skillId, skillLvl, page);
		return viewSkillGroup(st, group, schemeId, page, "");
	}

	private String viewGroup(final QuestState st, final int schemeId)
	{
		String htmltext = "";
		htmltext += title(st);
		htmltext += "<center>";
		if (schemeId != -1)
			htmltext += "Scheme Name: <font color=\"LEVEL\">" + mbs.getSchemeNamePlayer(st.getPlayer(), schemeId) + "</font><br>";
		htmltext += "<table width=\"260\" align=\"center\">";
		htmltext += "<tr>";
		int i = 0;
		for (final String g : mbsi.getGroups(st.getPlayer()))
		{
			htmltext += "<td width=\"130\" align=\"center\" height=\"30\">";
			htmltext += button(g, "viewSkillGroup " + g + " " + schemeId + " 1", 115, 20, 6, false);
			htmltext += "</td>";
			htmltext += ++i % 2 == 0 ? "</tr><tr>" : "";
		}
		htmltext += "</tr>";
		htmltext += "</table><br><br1>";
		htmltext += button("Back", "page", 80, 20, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}

	private String viewSkillGroup(final QuestState st, final String group, final int schemeId, final int page, final String msg)
	{
		final L2PcInstance player = st.getPlayer();
		int countDance = -1;
		int countOther = -1;
		String htmltext = "";
		htmltext += title(st);
		if (schemeId != -1)
		{
			final int[] buffs = mbs.getSchemeCountGroupPlayer(st.getPlayer(), schemeId);
			countDance = Config.DANCES_MAX_AMOUNT - buffs[0];
			countOther = Config.BUFFS_MAX_AMOUNT - buffs[1];
			htmltext += "<center>Scheme Information</center><br1>";
			htmltext += "Scheme Name: <font color=\"LEVEL\">" + mbs.getSchemeNamePlayer(player, schemeId) + "</font><br1>";
			htmltext += "Dance/Song: " + Config.DANCES_MAX_AMOUNT + " - " + buffs[0] + " = " + countDance + "<br1>";
			htmltext += "Others: " + Config.BUFFS_MAX_AMOUNT + " - " + buffs[1] + " = " + countOther + "<br>";
		}
		htmltext += "<center>";
		if (msg != "")
			htmltext += "<font color=\"LEVEL\">" + msg + "</font><br1>";

		final int maxPerPage = 6;
		final int countPag = (int) Math.ceil((double) mbsi.getCountSkill(group, st.getPlayer()) / (double) maxPerPage);
		final int startRow = maxPerPage * (page - 1);
		final int stopRow = startRow + maxPerPage;
		int countReg = 0;
		final String pages = generateButtonPage(countPag, page, group, schemeId);
		htmltext += pages;
		htmltext += "<table width=\"260\" align=\"center\">";
		Boolean c = true;
		final String c1 = "000000";
		final String c2 = "292929";
		for (final BufferSkill bs : mbsi.getSkillGroup(group, st.getPlayer()))
		{
			if (bs == null)
				break;
			if (countReg >= stopRow)
				break;
			if (countReg >= startRow && countReg < stopRow)
			{
				final String color = c ? c1 : c2;
				htmltext += "<tr><td width=\"260\">";
				htmltext += "<table width=\"260\" align=\"center\" bgcolor=\"" + color + "\">";
				htmltext += "<tr><td width=\"260\" align=\"center\">" + bs.getName() + " <font color=\"FFE545\">" + bs.getComp() + "</font></td></tr>";
				htmltext += "<tr><td>";
				final String action = group + " " + schemeId + " " + bs.getId() + " " + bs.getLevel() + " " + page;
				htmltext += "<table width=\"260\">";
				htmltext += "<tr>";
				htmltext += "<td width=\"34\" align=\"center\"><img src=\"" + bs.getIcon() + "\" width=\"32\" height=\"32\"></td>";
				htmltext += "<td align=\"center\">" + button("INFO", "viewSkillInfo " + action, 50, 20, 6, false) + "</td>";
				htmltext += "<td align=\"center\">" + button("CHAR", "execSelectBuff " + action + " 2 group", 50, 20, 2, false) + "</td>";
				if (Config.BUFFER_NPC_ENABLE_PET)
					htmltext += "<td align=\"center\">" + button("PET", "execSelectBuff " + action + " 3 group", 50, 20, 3, false) + "</td>";
				if (schemeId != -1 && Config.BUFFER_NPC_ENABLE_SCHEME)
					if (!mbs.verifySkillInScheme(st.getPlayer(), schemeId, bs.getId(), bs.getLevel()))
					{
						final boolean buffDanceSong = bs.getGroup().equalsIgnoreCase("Dance") || bs.getGroup().equalsIgnoreCase("Song");
						if (buffDanceSong && countDance == 0 || !buffDanceSong && countOther == 0)
							htmltext += "<td align=\"center\">" + "" + "</td>";
						else
							htmltext += "<td align=\"center\">" + button("ADD", "addSkillScheme " + action, 50, 20, 3, false) + "</td>";
					}
					else
						htmltext += "<td align=\"center\">" + button("DEL", "delSkillScheme " + action, 50, 20, 4, false) + "</td>";
				htmltext += "</tr>";
				htmltext += "</table>";
				htmltext += "</td></tr>";
				if (!Config.BUFFER_NPC_REMOVE_AMOUNT)
					htmltext += "<tr><td align=\"center\"><font color=\"LEVEL\">" + formatNumber(bs.getFee_amount()) + " " + getNameItem(bs.getFee_id()) + "</font></td></tr>";
				htmltext += "</table>";
				htmltext += "</td></tr>";
				c = !c;
			}
			countReg++;
		}
		htmltext += "</table>";
		htmltext += pages;
		htmltext += "<br1>" + button("Back Group", "viewGroup " + schemeId, 100, 20, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}

	private String viewSkillInfo(final QuestState st, final String group, final int schemeId, final int skillId, final int skillLvl, final int page)
	{
		final BufferSkill bs = mbsi.getSkillInfo(String.valueOf(skillId + "-" + skillLvl));
		String htmltext = "";
		htmltext += title(st);
		htmltext += "<center><img src=\"" + bs.getIcon() + "\" width=\"32\" height=\"32\"></center><br>";
		htmltext += "Skill Name: <font color=\"LEVEL\">" + bs.getName() + " " + bs.getComp() + "</font><br1>";
		htmltext += "Fee: <font color=\"LEVEL\">" + formatNumber(bs.getFee_amount()) + " " + getNameItem(bs.getFee_id()) + "</font><br1>";
		htmltext += "Description:<br1>" + bs.getDesc() + "<br><br>";
		int countDance = -1;
		int countOther = -1;
		if (schemeId != -1)
		{
			final int[] buffs = mbs.getSchemeCountGroupPlayer(st.getPlayer(), schemeId);
			countDance = Config.DANCES_MAX_AMOUNT - buffs[0];
			countOther = Config.BUFFS_MAX_AMOUNT - buffs[1];
		}
		final String action = group + " " + schemeId + " " + bs.getId() + " " + bs.getLevel() + " " + page;
		htmltext += "<center>";
		htmltext += "<table width=\"260\">";
		htmltext += "<tr>";
		htmltext += "<td align=\"center\">" + button("CHAR", "execSelectBuff " + action + " 2 info", 50, 20, 2, false) + "</td>";
		if (Config.BUFFER_NPC_ENABLE_PET)
			htmltext += "<td align=\"center\">" + button("PET", "execSelectBuff " + action + " 3 info", 50, 20, 3, false) + "</td>";
		if (schemeId != -1 && Config.BUFFER_NPC_ENABLE_SCHEME)
			if (!mbs.verifySkillInScheme(st.getPlayer(), schemeId, bs.getId(), bs.getLevel()))
			{
				final boolean buffDanceSong = bs.getGroup().equalsIgnoreCase("Dance") || bs.getGroup().equalsIgnoreCase("Song");
				if (buffDanceSong && countDance == 0 || !buffDanceSong && countOther == 0)
					htmltext += "<td align=\"center\">" + "" + "</td>";
				else
					htmltext += "<td align=\"center\">" + button("ADD", "addSkillScheme " + action + " info", 50, 20, 3, false) + "</td>";
			}
			else
				htmltext += "<td align=\"center\">" + button("DEL", "delSkillScheme " + action + " info", 50, 20, 4, false) + "</td>";
		htmltext += "</tr>";
		htmltext += "</table>";
		htmltext += "<br><br1>" + button("Back", "viewSkillGroup " + group + " " + schemeId + " " + page, 100, 20, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}

	private String newSchemePage(final QuestState st, final String msg)
	{
		String htmltext = "";
		htmltext += title(st);
		if (Config.BUFFER_NPC_FEE_SCHEME[0] != 0)
			htmltext += "Value of Scheme: " + formatNumber((long) Config.BUFFER_NPC_FEE_SCHEME[1]) + " " + getNameItem(Config.BUFFER_NPC_FEE_SCHEME[0]) + "<br>";
		htmltext += "Type a name for your scheme buffs.<br1>";
		htmltext += "Examples: XP, PvP, Raid, etc ...<br1>";
		htmltext += "More than one word separated by period (.).<br1>";
		htmltext += "Maximum 07 characters.<br>";
		if (msg != "")
			htmltext += "<font color=\"LEVEL\">" + msg + "</font><br>";
		htmltext += "<table width=\"260\" align=\"center\">";
		htmltext += "<tr><td width=\"100\">Name:</td><td width=\"160\"><edit var=\"name\" width=\"120\"></td></tr>";
		htmltext += "<tr><td width=\"100\">Type:</td><td width=\"160\"><combobox width=\"120\" var=\"type\" list=\"Char" + (Config.BUFFER_NPC_ENABLE_PET ? ";Pet" : "") + "\"></td></tr>";
		htmltext += "<tr><td width=\"100\" align=\"left\">" + button("Back", "page", 80, 20, 4, false) + "</td>";
		htmltext += "<td width=\"160\" align=\"right\">" + button("Add", "addScheme $type $name", 80, 20, 1, false) + "</td></tr>";
		htmltext += "</table>";
		htmltext += footer();
		return htmltext;
	}

	private String editSchemePage(final QuestState st)
	{
		String htmltext = "";
		htmltext += title(st);
		htmltext += "<center>";
		htmltext += loadScheme(st.getPlayer(), "Edit My Scheme", ActionType.EDIT);
		htmltext += button("Back", "page", 80, 20, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}

	private String delSchemePage(final QuestState st, final String msg)
	{
		String htmltext = "";
		htmltext += title(st);
		htmltext += "<center>";
		htmltext += loadScheme(st.getPlayer(), "Delete My Scheme", ActionType.DEL);
		htmltext += button("Back", "page", 80, 20, 4, false);
		htmltext += "</center>";
		htmltext += footer();
		return htmltext;
	}

	private String removeBuff(final QuestState st, final int type)
	{
		if (!mbs.payBuffFee(st.getPlayer(), Config.BUFFER_NPC_FEE_REMOVE[0], (long) Config.BUFFER_NPC_FEE_REMOVE[1]))
			return page(st, "You need money to use this option!");
		switch (type)
		{
			case 1:
			default:
				if (st.getPlayer() != null)
					st.getPlayer().stopAllEffects();
				break;
			case 2:
				if (st.getPlayer().getPet() != null)
					st.getPlayer().getPet().stopAllEffects();
				break;
		}
		return page(st, "Buff successfully removed!");
	}
	
	private String recover(final QuestState st, final int type)
	{
		if (!Config.BUFFER_NPC_ENABLE_RECOVER_EVENT && st.getPlayer().onEvents())
			return page(st, "Can not use recovery Events.");
		if (!mbs.payBuffFee(st.getPlayer(), Config.BUFFER_NPC_FEE_RECOVER[0], (long) Config.BUFFER_NPC_FEE_RECOVER[1]))
			return page(st, "You need money to use this option!");
		switch (type)
		{
			case 1:
			default:
				if (st.getPlayer() != null)
				{
					st.getPlayer().getStatus().setCurrentHp(st.getPlayer().getStat().getMaxHp());
					st.getPlayer().getStatus().setCurrentMp(st.getPlayer().getStat().getMaxMp());
					st.getPlayer().getStatus().setCurrentCp(st.getPlayer().getStat().getMaxCp());
				}
				break;
			case 2:
				if (st.getPlayer().getPet() != null)
				{
					st.getPlayer().getPet().getStatus().setCurrentHp(st.getPlayer().getPet().getStat().getMaxHp());
					st.getPlayer().getPet().getStatus().setCurrentMp(st.getPlayer().getPet().getStat().getMaxMp());
				}
				break;
		}
		return page(st, "Successfully recover!");
	}

	public enum ActionType
	{
		VIEW,
		DEL,
		EDIT,
		EXEC;
		public ActionType getType(final String name)
		{
			try
			{
				return ActionType.valueOf(name.toUpperCase());
			}
			catch (final Exception e)
			{
				return null;
			}
		}
	}
}
