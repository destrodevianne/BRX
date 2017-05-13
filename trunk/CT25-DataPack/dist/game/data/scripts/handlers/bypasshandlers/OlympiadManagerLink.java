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
package handlers.bypasshandlers;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.datatables.MultiSell;
import ct25.xtreme.gameserver.datatables.NpcBufferTable;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.handler.IBypassHandler;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2OlympiadManagerInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.olympiad.CompetitionType;
import ct25.xtreme.gameserver.model.olympiad.Olympiad;
import ct25.xtreme.gameserver.model.olympiad.OlympiadManager;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ExHeroList;
import ct25.xtreme.gameserver.network.serverpackets.InventoryUpdate;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * @author DS
 */
public class OlympiadManagerLink implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"olympiaddesc",
		"olympiadnoble",
		"olybuff",
		"olympiad"
	};
	
	private static final String FEWER_THAN = "Fewer than " + String.valueOf(Config.ALT_OLY_REG_DISPLAY);
	private static final String MORE_THAN = "More than " + String.valueOf(Config.ALT_OLY_REG_DISPLAY);
	private static final int GATE_PASS = Config.ALT_OLY_COMP_RITEM;

	@Override
	public final boolean useBypass(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		if (!(target instanceof L2OlympiadManagerInstance))
			return false;
		
		try
		{
			if (command.toLowerCase().startsWith(COMMANDS[0])) // desc
			{
				final int val = Integer.parseInt(command.substring(13, 14));
				final String suffix = command.substring(14);
				((L2OlympiadManagerInstance) target).showChatWindow(activeChar, val, suffix);
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1])) // noble
			{
				if (!activeChar.isNoble() || activeChar.getClassId().level() < 3)
					return false;

				int passes;
				final int val = Integer.parseInt(command.substring(14));
				final NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());

				switch (val)
				{
					case 1:
						OlympiadManager.getInstance().unRegisterNoble(activeChar);
						break;
					case 2:
						final int nonClassed = OlympiadManager.getInstance().getRegisteredNonClassBased().size();
						final int teams = OlympiadManager.getInstance().getRegisteredTeamsBased().size();
						final Collection<List<Integer>> allClassed = OlympiadManager.getInstance().getRegisteredClassBased().values();
						int classed = 0;
						if (!allClassed.isEmpty())
							for (final List<Integer> cls : allClassed)
								if (cls != null)
									classed += cls.size();
						html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_registered.htm");
						if (Config.ALT_OLY_REG_DISPLAY > 0)
						{
							html.replace("%listClassed%", classed < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
							html.replace("%listNonClassedTeam%", teams < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
							html.replace("%listNonClassed%", nonClassed < Config.ALT_OLY_REG_DISPLAY ? FEWER_THAN : MORE_THAN);
						}
						else
						{
							html.replace("%listClassed%", String.valueOf(classed));
							html.replace("%listNonClassedTeam%", String.valueOf(teams));
							html.replace("%listNonClassed%", String.valueOf(nonClassed));
						}
						html.replace("%objectId%", String.valueOf(target.getObjectId()));
						activeChar.sendPacket(html);
						break;
					case 3:
						final int points = Olympiad.getInstance().getNoblePoints(activeChar.getObjectId());
						html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_points1.htm");
						html.replace("%points%", String.valueOf(points));
						html.replace("%objectId%", String.valueOf(target.getObjectId()));
						activeChar.sendPacket(html);
						break;
					case 4:
						OlympiadManager.getInstance().registerNoble(activeChar, CompetitionType.NON_CLASSED);
						break;
					case 5:
						OlympiadManager.getInstance().registerNoble(activeChar, CompetitionType.CLASSED);
						break;
					case 6:
						passes = Olympiad.getInstance().getNoblessePasses(activeChar, false);
						if (passes > 0)
						{
							html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_settle.htm");
							html.replace("%objectId%", String.valueOf(target.getObjectId()));
							activeChar.sendPacket(html);
						}
						else
						{
							html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_nopoints2.htm");
							html.replace("%objectId%", String.valueOf(target.getObjectId()));
							activeChar.sendPacket(html);
						}
						break;
					case 7:
						MultiSell.getInstance().separateAndSend(102, activeChar, (L2Npc) target, false);
						break;
					case 8:
						MultiSell.getInstance().separateAndSend(103, activeChar, (L2Npc) target, false);
						break;
					case 9:
						final int point = Olympiad.getInstance().getLastNobleOlympiadPoints(activeChar.getObjectId());
						html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_points2.htm");
						html.replace("%points%", String.valueOf(point));
						html.replace("%objectId%", String.valueOf(target.getObjectId()));
						activeChar.sendPacket(html);
						break;
					case 10:
						passes = Olympiad.getInstance().getNoblessePasses(activeChar, true);
						if (passes > 0)
						{
							final L2ItemInstance item = activeChar.getInventory().addItem("Olympiad", GATE_PASS, passes, activeChar, target);

							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(item);
							activeChar.sendPacket(iu);

							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemNumber(passes);
							sm.addItemName(item);
							activeChar.sendPacket(sm);
						}
						break;
					case 11:
						OlympiadManager.getInstance().registerNoble(activeChar, CompetitionType.TEAMS);
						break;
					default:
						_log.warning("Olympiad System: Couldnt send packet for request " + val);
						break;
				}
			}
			else if (command.toLowerCase().startsWith(COMMANDS[2])) // buff
			{
				if (activeChar.olyBuff <= 0)
					return false;
				
				final NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
				final String[] params = command.split(" ");

				if (params[1] == null)
				{
					_log.warning("Olympiad Buffer Warning: npcId = " + ((L2Npc) target).getId() + " has no buffGroup set in the bypass for the buff selected.");
					return false;
				}
				final int buffGroup = Integer.parseInt(params[1]);

				final int[] npcBuffGroupInfo = NpcBufferTable.getInstance().getSkillInfo(((L2Npc) target).getId(), buffGroup);

				if (npcBuffGroupInfo == null)
				{
					_log.warning("Olympiad Buffer Warning: npcId = " + ((L2Npc) target).getId() + " Location: " + target.getX() + ", " + target.getY() + ", " + target.getZ() + " Player: " + activeChar.getName() + " has tried to use skill group (" + buffGroup + ") not assigned to the NPC Buffer!");
					return false;
				}

				final int skillId = npcBuffGroupInfo[0];
				final int skillLevel = npcBuffGroupInfo[1];

				final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				target.setTarget(activeChar);

				if (activeChar.olyBuff > 0)
					if (skill != null)
					{
						activeChar.olyBuff--;
						target.broadcastPacket(new MagicSkillUse(target, activeChar, skill.getId(), skill.getLevel(), 0, 0));
						skill.getEffects(activeChar, activeChar);
						final L2Summon summon = activeChar.getPet();
						if (summon != null)
						{
							target.broadcastPacket(new MagicSkillUse(target, summon, skill.getId(), skill.getLevel(), 0, 0));
							skill.getEffects(summon, summon);
						}
					}

				if (activeChar.olyBuff > 0)
				{
					html.setFile(activeChar.getHtmlPrefix(), activeChar.olyBuff == 5 ? Olympiad.OLYMPIAD_HTML_PATH + "olympiad_buffs.htm" : Olympiad.OLYMPIAD_HTML_PATH + "olympiad_5buffs.htm");
					html.replace("%objectId%", String.valueOf(target.getObjectId()));
					activeChar.sendPacket(html);
				}
				else
				{
					html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "olympiad_nobuffs.htm");
					html.replace("%objectId%", String.valueOf(target.getObjectId()));
					activeChar.sendPacket(html);
					target.deleteMe();
				}
			}
			else if (command.toLowerCase().startsWith(COMMANDS[3])) // olympiad
			{
				final int val = Integer.parseInt(command.substring(9, 10));

				final NpcHtmlMessage reply = new NpcHtmlMessage(target.getObjectId());

				switch (val)
				{
					case 2:
						// for example >> Olympiad 1_88
						final int classId = Integer.parseInt(command.substring(11));
						if (classId >= 88 && classId <= 118 || classId >= 131 && classId <= 134 || classId == 136)
						{
							final List<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
							reply.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "olympiad_ranking.htm");

							int index = 1;
							for (final String name : names)
							{
								reply.replace("%place" + index + "%", String.valueOf(index));
								reply.replace("%rank" + index + "%", name);
								index++;
								if (index > 10)
									break;
							}
							for (; index <= 10; index++)
							{
								reply.replace("%place" + index + "%", "");
								reply.replace("%rank" + index + "%", "");
							}

							reply.replace("%objectId%", String.valueOf(target.getObjectId()));
							activeChar.sendPacket(reply);
						}
						break;
					case 4:
						activeChar.sendPacket(new ExHeroList());
						break;
					default:
						_log.warning("Olympiad System: Couldnt send packet for request " + val);
						break;
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.INFO, "Exception in " + e.getMessage(), e);
		}
		
		return true;
	}

	@Override
	public final String[] getBypassList()
	{
		return COMMANDS;
	}
}
