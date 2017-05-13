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
package quests.Q00320_BonesTellTheFuture;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Bones Tell The Future (320)
 * @author ivantotov
 */
public final class Q00320_BonesTellTheFuture extends Quest
{
	// NPC
	private static final int TETRACH_KAITAR = 30359;
	// Item
	private static final int BONE_FRAGMENT = 809;
	// Misc
	private static final int MIN_LEVEL = 10;
	private static final int REQUIRED_BONE_COUNT = 10;
	private static final double DROP_CHANCE = 0.18;
	// Monsters
	private static final int[] MONSTERS =
	{
		20517, // Skeleton Hunter
		20518, // Skeleton Hunter Archer
	};

	private Q00320_BonesTellTheFuture(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(TETRACH_KAITAR);
		addTalkId(TETRACH_KAITAR);
		addKillId(MONSTERS);
		registerQuestItems(BONE_FRAGMENT);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equals("30359-04.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState qs = getRandomPartyMemberState(killer, 1, 3, npc);
		if (qs != null && qs.giveItemRandomly(npc, BONE_FRAGMENT, 1, REQUIRED_BONE_COUNT, DROP_CHANCE, true))
			qs.setCond(2);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getRace() == Race.DarkElf ? player.getLevel() >= MIN_LEVEL ? "30359-03.htm" : "30359-02.htm" : "30359-00.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.getQuestItemsCount(BONE_FRAGMENT) >= REQUIRED_BONE_COUNT)
				{
					htmltext = "30359-06.html";
					st.giveAdena(8470, true);
					st.exitQuest(true, true);
				}
				else
					htmltext = "30359-05.html";
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00320_BonesTellTheFuture(320, Q00320_BonesTellTheFuture.class.getSimpleName(), "Bones Tell The Future");
	}
}