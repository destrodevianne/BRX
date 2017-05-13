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
package quests.Q00319_ScentOfDeath;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.ItemHolder;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;

/**
 * Scent of Death (319)
 * @author Zoey76
 */
public class Q00319_ScentOfDeath extends Quest
{
	// NPC
	private static final int MINALESS = 30138;
	// Monsters
	private static final int MARSH_ZOMBIE = 20015;
	private static final int MARSH_ZOMBIE_LORD = 20020;
	// Item
	private static final int ZOMBIES_SKIN = 1045;
	private static final ItemHolder LESSER_HEALING_POTION = new ItemHolder(1060, 1);
	// Misc
	private static final int MIN_LEVEL = 11;
	private static final int MIN_CHANCE = 7;
	private static final int REQUIRED_ITEM_COUNT = 5;

	private Q00319_ScentOfDeath(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(MINALESS);
		addTalkId(MINALESS);
		addKillId(MARSH_ZOMBIE, MARSH_ZOMBIE_LORD);
		registerQuestItems(ZOMBIES_SKIN);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		String htmltext = null;
		if (player.getLevel() >= MIN_LEVEL)
			switch (event)
			{
				case "30138-04.htm":
				{
					st.startQuest();
					htmltext = event;
					break;
				}
			}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && Util.checkIfInRange(1500, npc, killer, false) && st.getQuestItemsCount(ZOMBIES_SKIN) < REQUIRED_ITEM_COUNT)
			if (getRandom(10) > MIN_CHANCE)
			{
				st.giveItems(ZOMBIES_SKIN, 1);
				if (st.getQuestItemsCount(ZOMBIES_SKIN) < REQUIRED_ITEM_COUNT)
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				else
					st.setCond(2, true);
			}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return getNoQuestMsg(player);

		String htmltext = getNoQuestMsg(player);
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getLevel() >= MIN_LEVEL ? "30138-03.htm" : "30138-02.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						htmltext = "30138-05.html";
						break;
					}
					case 2:
					{
						st.giveAdena(3350, false);
						st.giveItems(LESSER_HEALING_POTION);
						st.takeItems(ZOMBIES_SKIN, -1);
						st.exitQuest(true, true);
						htmltext = "30138-06.html";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00319_ScentOfDeath(319, Q00319_ScentOfDeath.class.getSimpleName(), "Scent of Death");
	}
}
