package handlers.voicedcommandhandlers;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.cache.HtmCache;
import ct25.xtreme.gameserver.handler.IVoicedCommandHandler;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.event.DMEvent;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;

public class DMVoicedInfo implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"dminfo",
		"dmjoin",
		"dmleave"
	};

	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "data/html/mods/DMEvent/Status.htm");

	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (command.equalsIgnoreCase("dminfo"))
		{
			if (DMEvent.isStarting() || DMEvent.isStarted())
			{
				final String htmContent = USE_STATIC_HTML && !HTML.isEmpty() ? HTML : HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/DMEvent/Status.htm");

				try
				{
					final String[] firstPositions = DMEvent.getFirstPosition(Config.DM_REWARD_FIRST_PLAYERS);
					final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);

					String htmltext = "";
					Boolean c = true;
					final String c1 = "D9CC46";
					final String c2 = "FFFFFF";
					if (firstPositions != null)
						for (int i = 0; i < firstPositions.length; i++)
						{
							final String[] row = firstPositions[i].split("\\,");
							final String color = c ? c1 : c2;
							htmltext += "<tr>";
							htmltext += "<td width=\"35\" align=\"center\"><font color=\"" + color + "\">" + String.valueOf(i + 1) + "</font></td>";
							htmltext += "<td width=\"100\" align=\"left\"><font color=\"" + color + "\">" + row[0] + "</font></td>";
							htmltext += "<td width=\"125\" align=\"right\"><font color=\"" + color + "\">" + row[1] + "</font></td>";
							htmltext += "</tr>";
							c = !c;
						}

					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%toprank%", htmltext);
					activeChar.sendPacket(npcHtmlMessage);

				}
				catch (final Exception e)
				{
					_log.warning("wrong DM voiced: " + e);
				}

			}
			else
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (command.equalsIgnoreCase("dmjoin"))
			DMEvent.onBypass("dm_event_participation", activeChar);
		else if (command.equalsIgnoreCase("dmleave"))
			DMEvent.onBypass("dm_event_remove_participation", activeChar);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
