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
package quests.Q00158_SeedOfEvil;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.clientpackets.Say2;

/**
 * Seed of Evil (158)
 * @author malyelfik
 */
public class Q00158_SeedOfEvil extends Quest
{
	// NPC
	private static final int BIOTIN = 30031;
	// Monster
	private static final int NERKAS = 27016;
	// Items
	private static final int ENCHANT_ARMOR_D = 956;
	private static final int CLAY_TABLET = 1025;
	// Misc
	private static final int MIN_LEVEL = 21;

	private Q00158_SeedOfEvil(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(BIOTIN);
		addTalkId(BIOTIN);
		addAttackId(NERKAS);
		addKillId(NERKAS);
		registerQuestItems(CLAY_TABLET);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equalsIgnoreCase("30031-03.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.isScriptValue(0))
		{
			npc.broadcastNpcSay(Say2.NPC_ALL, "... How dare you challenge me!");
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && !st.hasQuestItems(CLAY_TABLET))
		{
			st.giveItems(CLAY_TABLET, 1);
			st.setCond(2, true);
		}
		npc.broadcastNpcSay(Say2.NPC_ALL, "The power of Lord Beleth rules the whole world...!");
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
				htmltext = player.getLevel() >= MIN_LEVEL ? "30031-02.htm" : "30031-01.html";
				break;
			case State.STARTED:
				if (st.isCond(1))
					htmltext = "30031-04.html";
				else if (st.isCond(2) && st.hasQuestItems(CLAY_TABLET))
				{
					st.giveItems(ENCHANT_ARMOR_D, 1);
					st.addExpAndSp(17818, 927);
					st.giveAdena(1495, true);
					st.exitQuest(false, true);
					htmltext = "30031-05.html";
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00158_SeedOfEvil(158, Q00158_SeedOfEvil.class.getSimpleName(), "Seed of Evil");
	}
}