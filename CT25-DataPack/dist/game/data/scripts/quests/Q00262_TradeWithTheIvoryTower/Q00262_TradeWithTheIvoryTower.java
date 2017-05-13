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
package quests.Q00262_TradeWithTheIvoryTower;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Trade With The Ivory Tower (262)
 * @author ivantotov
 */
public final class Q00262_TradeWithTheIvoryTower extends Quest
{
	// NPCs
	private static final int VOLLODOS = 30137;
	// Items
	private static final int SPORE_SAC = 707;
	// Misc
	private static final int MIN_LEVEL = 8;
	private static final int REQUIRED_ITEM_COUNT = 10;
	// Monsters
	private static final Map<Integer, Integer> MOBS_SAC = new HashMap<>();

	static
	{
		MOBS_SAC.put(20007, 3); // Green Fungus
		MOBS_SAC.put(20400, 4); // Blood Fungus
	}

	private Q00262_TradeWithTheIvoryTower(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(VOLLODOS);
		addTalkId(VOLLODOS);
		for (final int id : MOBS_SAC.keySet())
			super.addKillId(id);
		registerQuestItems(SPORE_SAC);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equalsIgnoreCase("30137-03.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return super.onKill(npc, player, isPet);

		final QuestState st = partyMember.getQuestState(getName());
		final float chance = MOBS_SAC.get(npc.getId()) * Config.RATE_QUEST_DROP;
		if (getRandom(10) < chance)
		{
			st.rewardItems(SPORE_SAC, 1);
			if (st.getQuestItemsCount(SPORE_SAC) >= REQUIRED_ITEM_COUNT)
				st.setCond(2, true);
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, player, isPet);
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
				htmltext = player.getLevel() >= MIN_LEVEL ? "30137-02.htm" : "30137-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						if (st.getQuestItemsCount(SPORE_SAC) < REQUIRED_ITEM_COUNT)
							htmltext = "30137-04.html";
						break;
					}
					case 2:
					{
						if (st.getQuestItemsCount(SPORE_SAC) >= REQUIRED_ITEM_COUNT)
						{
							htmltext = "30137-05.html";
							st.giveAdena(3000, true);
							st.exitQuest(true, true);
						}
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
		new Q00262_TradeWithTheIvoryTower(262, Q00262_TradeWithTheIvoryTower.class.getSimpleName(), "Trade With The Ivory Tower");
	}
}