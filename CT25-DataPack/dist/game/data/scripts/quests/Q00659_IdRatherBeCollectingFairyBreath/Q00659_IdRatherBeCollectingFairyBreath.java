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
package quests.Q00659_IdRatherBeCollectingFairyBreath;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

/**
 * I'd Rather Be Collecting Fairy Breath (659)
 * @author Adry_85
 */
public final class Q00659_IdRatherBeCollectingFairyBreath extends Quest
{
	// NPC
	private static final int GALATEA = 30634;
	// Item
	private static final int FAIRY_BREATH = 8286;
	// Misc
	private static final int MIN_LEVEL = 26;
	// Mobs
	private static final Map<Integer, Double> MOBS = new HashMap<>();
	static
	{
		MOBS.put(20078, 0.98); // whispering_wind
		MOBS.put(21023, 0.82); // sobing_wind
		MOBS.put(21024, 0.86); // babbleing_wind
		MOBS.put(21025, 0.90); // giggleing_wind
		MOBS.put(21026, 0.96); // singing_wind
	}

	private Q00659_IdRatherBeCollectingFairyBreath()
	{
		super(659, Q00659_IdRatherBeCollectingFairyBreath.class.getSimpleName(), "I'd Rather Be Collecting Fairy Breath");
		addStartNpc(GALATEA);
		addTalkId(GALATEA);
		for (final int id : MOBS.keySet())
			super.addKillId(id);
		registerQuestItems(FAIRY_BREATH);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		String htmltext = null;
		switch (event)
		{
			case "30634-02.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "REWARD":
			{
				if (hasQuestItems(player, FAIRY_BREATH))
				{
					final long count = getQuestItemsCount(player, FAIRY_BREATH);
					final long bonus = count >= 10 ? 5365 : 0;
					st.takeItems(FAIRY_BREATH, -1);
					st.giveAdena(count * 50 + bonus, true);
					htmltext = "30634-05.html";
				}
				else
					htmltext = "30634-06.html";
				break;
			}
			case "30634-07.html":
			{
				htmltext = event;
				break;
			}
			case "30634-08.html":
			{
				st.exitQuest(true, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final QuestState st = getRandomPartyMemberState(player, -1, 3, npc);
		if (st != null)
			st.giveItemRandomly(npc, FAIRY_BREATH, 1, 0, MOBS.get(npc.getId()), true);
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;

		if (st.isCreated())
			htmltext = player.getLevel() >= MIN_LEVEL ? "30634-01.htm" : "30634-03.html";
		else if (st.isStarted())
			htmltext = hasQuestItems(player, FAIRY_BREATH) ? "30634-04.html" : "30634-09.html";
		return htmltext;
	}

	public static void main(final String args[])
	{
		new Q00659_IdRatherBeCollectingFairyBreath();
	}
}
