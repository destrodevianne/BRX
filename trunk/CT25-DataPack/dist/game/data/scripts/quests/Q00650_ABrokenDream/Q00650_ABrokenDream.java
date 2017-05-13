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
package quests.Q00650_ABrokenDream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;
import quests.Q00117_TheOceanOfDistantStars.Q00117_TheOceanOfDistantStars;

/**
 * A Broken Dream (650)
 * @author netvirus
 */
public final class Q00650_ABrokenDream extends Quest
{
	// Npc
	private static final int GHOST_OF_A_RAILROAD_ENGINEER = 32054;
	// Item
	private static final int REMNANTS_OF_OLD_DWARVES_DREAMS = 8514;
	// Misc
	private static final int MIN_LVL = 39;
	// Monsters
	private static final Map<Integer, Integer> MONSTER_DROP_CHANCES = new HashMap<>();

	static
	{
		MONSTER_DROP_CHANCES.put(22027, 575); // Forgotten Crewman
		MONSTER_DROP_CHANCES.put(22028, 515); // Vagabond of the Ruins
	}

	private Q00650_ABrokenDream(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(GHOST_OF_A_RAILROAD_ENGINEER);
		addTalkId(GHOST_OF_A_RAILROAD_ENGINEER);
		for (final int id : MONSTER_DROP_CHANCES.keySet())
			super.addKillId(id);
		registerQuestItems(REMNANTS_OF_OLD_DWARVES_DREAMS);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st == null)
			return htmltext;

		switch (event)
		{
			case "32054-03.htm":
			{
				if (st.isCreated())
				{
					st.startQuest();
					htmltext = event;
				}
				break;
			}
			case "32054-07.html":
			case "32054-08.html":
			{
				if (st.isStarted())
					htmltext = event;
				break;
			}
			case "32054-09.html":
			{
				if (st.isStarted())
				{
					st.exitQuest(true, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() < MIN_LVL)
					htmltext = "32054-02.htm";
				else
				{
					final QuestState q117 = player.getQuestState(Q00117_TheOceanOfDistantStars.class.getName()); // TODO: Update.
					htmltext = q117 != null && q117.isCompleted() ? "32054-01.htm" : "32054-04.htm";
				}
				break;
			}
			case State.STARTED:
			{
				htmltext = st.hasQuestItems(REMNANTS_OF_OLD_DWARVES_DREAMS) ? "32054-05.html" : "32054-06.html";
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final List<L2PcInstance> randomList = new ArrayList<>();
		final QuestState st = killer.getQuestState(getName());
		if (st != null && st.isStarted())
		{
			randomList.add(killer);
			randomList.add(killer);
		}

		final int monsterChance = MONSTER_DROP_CHANCES.get(npc.getId());
		if (killer.isInParty())
			for (final L2PcInstance member : killer.getParty().getMembers())
			{
				final QuestState qs = member.getQuestState(getName());
				if (qs != null && qs.isStarted())
					randomList.add(member);
			}

		if (!randomList.isEmpty())
		{
			final L2PcInstance player = randomList.get(getRandom(randomList.size()));
			if (getRandom(1000) < monsterChance && Util.checkIfInRange(1500, npc, player, true))
			{
				giveItems(player, REMNANTS_OF_OLD_DWARVES_DREAMS, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	public static void main(final String[] args)
	{
		new Q00650_ABrokenDream(650, Q00650_ABrokenDream.class.getSimpleName(), "A Broken Dream");
	}
}
