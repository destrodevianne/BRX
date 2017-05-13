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
package teleports.NoblesseTeleport;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class NoblesseTeleport extends Quest
{
	private static final String qn = "NoblesseTeleport";
	
	private final static int[] NPCs =
	{
		30006,
		30059,
		30080,
		30134,
		30146,
		30177,
		30233,
		30256,
		30320,
		30540,
		30576,
		30836,
		30848,
		30878,
		30899,
		31275,
		31320,
		31964,
		32163
	};
	
	private final static String html = "<html><body><br>" + "Ah, you\'re a Noblesse! I can offer you a special service then.<br><br>" + "You may use this Olympiad Token.<br>" + "<a action=\"bypass -h %bypass%\">Teleport to Hunting Grounds</a><br><br>" + "Don\'t use Olympiad Token.<br>"
		+ "<a action=\"bypass -h npc_%objectId%_Chat 2\">Teleport to Hunting Grounds</a><br>" + "<a action=\"bypass -h npc_%objectId%_Chat 0\">Back</a>" + "</body></html>";
	
	public NoblesseTeleport(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int id : NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		return event;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		
		if (player.isNoble())
		{
			String bypass = "Quest NoblesseTeleport noble-nopass.htm";
			if (st.getQuestItemsCount(13722) > 0)
				bypass = "npc_%objectId%_Chat 3";
			htmltext = html.replace("%bypass%", bypass).replace("%objectId%", String.valueOf(npc.getObjectId()));
		}
		else
			htmltext = "nobleteleporter-no.htm";
		
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new NoblesseTeleport(-1, qn, "teleports");
	}
}