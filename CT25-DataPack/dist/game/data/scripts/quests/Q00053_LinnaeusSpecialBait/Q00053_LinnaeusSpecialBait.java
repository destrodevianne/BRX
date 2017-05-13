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
package quests.Q00053_LinnaeusSpecialBait;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Linnaeus Special Bait (53)<br>
 * Original Jython script by Next and DooMita.
 * @author nonom
 */
public class Q00053_LinnaeusSpecialBait extends Quest
{
	// NPCs
	private static final int LINNAEUS = 31577;
	private static final int CRIMSON_DRAKE = 20670;
	// Items
	private static final int CRIMSON_DRAKE_HEART = 7624;
	private static final int FLAMING_FISHING_LURE = 7613;
	// Misc
	// Custom setting: whether or not to check for fishing skill level?
	// Default False to require fishing skill level, any other value to ignore fishing and evaluate char level only.
	private static final boolean ALT_IGNORE_FISHING = false;

	public Q00053_LinnaeusSpecialBait(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(LINNAEUS);
		addTalkId(LINNAEUS);
		addKillId(CRIMSON_DRAKE);
		registerQuestItems(CRIMSON_DRAKE_HEART);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return getNoQuestMsg(player);

		String htmltext = event;

		switch (event)
		{
			case "31577-1.htm":
				st.startQuest();
				break;
			case "31577-3.htm":
				if (st.isCond(2) && st.getQuestItemsCount(CRIMSON_DRAKE_HEART) >= 100)
				{
					st.giveItems(FLAMING_FISHING_LURE, 4);
					st.exitQuest(false, true);
				}
				else
					htmltext = "31577-5.html";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return null;

		final QuestState st = partyMember.getQuestState(getName());

		if (st.getQuestItemsCount(CRIMSON_DRAKE_HEART) < 100)
		{
			final float chance = 33 * Config.RATE_QUEST_DROP;
			if (getRandom(100) < chance)
			{
				st.rewardItems(CRIMSON_DRAKE_HEART, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}

		if (st.getQuestItemsCount(CRIMSON_DRAKE_HEART) >= 100)
			st.setCond(2, true);

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
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				htmltext = player.getLevel() > 59 && fishingLevel(player) > 19 ? "31577-0.htm" : "31577-0a.html";
				break;
			case State.STARTED:
				htmltext = st.isCond(1) ? "31577-4.html" : "31577-2.html";
				break;
		}
		return htmltext;
	}

	private static int fishingLevel(final L2PcInstance player)
	{
		int level = 20;
		if (!ALT_IGNORE_FISHING)
		{
			level = player.getSkillLevel(1315);
			final L2Effect info = player.getFirstEffect(2274);
			if (info != null)
				level = (int) info.getSkill().getPower();
		}
		return level;
	}

	public static void main(final String[] args)
	{
		new Q00053_LinnaeusSpecialBait(53, Q00053_LinnaeusSpecialBait.class.getSimpleName(), "Linnaeus Special Bait");
	}
}
