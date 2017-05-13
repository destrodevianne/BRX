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
package ai.individual.npc.Tunatun;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import quests.Q00020_BringUpWithLove.Q00020_BringUpWithLove;

/**
 * Beast Herder Tunatun AI.
 * @author Adry_85
 */
public final class Tunatun extends Quest
{
	// NPC
	private static final int TUNATUN = 31537;

	// Item
	private static final int BEAST_HANDLERS_WHIP = 15473;

	// Misc
	private static final int MIN_LEVEL = 82;

	private Tunatun(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(TUNATUN);
		addFirstTalkId(TUNATUN);
		addTalkId(TUNATUN);
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		switch (npc.getId())
		{
			case TUNATUN:
			{
				return "31537-00.html";
			}
		}
		return null;
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if ("Whip".equals(event))
		{
			if (hasQuestItems(player, BEAST_HANDLERS_WHIP))
				return "31537-01.html";

			final QuestState st = player.getQuestState(Q00020_BringUpWithLove.class.getSimpleName());
			if (st == null && player.getLevel() < MIN_LEVEL)
				return "31537-02.html";
			else if (st != null || player.getLevel() >= MIN_LEVEL)
			{
				giveItems(player, BEAST_HANDLERS_WHIP, 1);
				return "31537-03.html";
			}
		}
		return event;
	}

	public static void main(final String[] args)
	{
		new Tunatun(-1, Tunatun.class.getSimpleName(), "ai/individual/npc");
	}
}