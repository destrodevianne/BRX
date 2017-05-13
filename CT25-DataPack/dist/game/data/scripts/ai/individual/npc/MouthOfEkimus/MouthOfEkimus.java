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
package ai.individual.npc.MouthOfEkimus;

import ct25.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

/**
 * @author Browser
 */
public class MouthOfEkimus extends Quest
{
	// NPC
	private static final int MOUTHOFEKIMUS = 32537;

	// Misc
	private static final int MIN_LV = 75;

	public MouthOfEkimus(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(MOUTHOFEKIMUS);
		addFirstTalkId(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		switch (event)
		{
			case "hallofsuffering":
			{
				if (GraciaSeedsManager.getInstance().getSoIState() == 1)
					htmltext = "32537-1.htm";
				else if (GraciaSeedsManager.getInstance().getSoIState() == 4)
					htmltext = "32537-2.htm";
				else
					htmltext = "32537-6.htm";
				break;
			}

			case "halloferosion":
			{
				if (GraciaSeedsManager.getInstance().getSoIState() == 1)
					htmltext = "32537-3.htm";
				else if (GraciaSeedsManager.getInstance().getSoIState() == 4)
					htmltext = "32537-4.htm";
				else
					htmltext = "32537-6.htm";
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (player.getLevel() >= MIN_LV)
			return "32537-0.htm";
		return "32537-5.htm";
	}

	public static void main(final String[] args)
	{
		new MouthOfEkimus(-1, MouthOfEkimus.class.getSimpleName(), "ai/individual/npc");
	}
}