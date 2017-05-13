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
package quests.Q00186_ContractExecution;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;
import quests.Q00184_ArtOfPersuasion.Q00184_ArtOfPersuasion;

/**
 * Contract Execution (186)
 * @author ivantotov
 */
public final class Q00186_ContractExecution extends Quest
{
	// NPCs
	private static final int RESEARCHER_LORAIN = 30673;
	private static final int MAESTRO_NIKOLA = 30621;
	private static final int BLUEPRINT_SELLER_LUKA = 31437;
	// Items
	private static final int LORAINES_CERTIFICATE = 10362;
	private static final int METALLOGRAPH_RESEARCH_REPORT = 10366;
	private static final int LETO_LIZARDMAN_ACCESSORY = 10367;
	// Misc
	private static final int MIN_LEVEL = 41;
	private static final int MAX_LEVEL_FOR_EXP_SP = 47;
	// Monsters
	private static final int[] MONSTERS = new int[]
	{
		20577, // Leto Lizardman
		20578, // Leto Lizardman Archer
		20579, // Leto Lizardman Soldier
		20580, // Leto Lizardman Warrior
		20581, // Leto Lizardman Shaman
		20582, // Leto Lizardman Overlord
	};

	private Q00186_ContractExecution(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(RESEARCHER_LORAIN);
		addTalkId(RESEARCHER_LORAIN, BLUEPRINT_SELLER_LUKA, MAESTRO_NIKOLA);
		addKillId(MONSTERS);
		registerQuestItems(METALLOGRAPH_RESEARCH_REPORT, LETO_LIZARDMAN_ACCESSORY);
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
			case "30621-02.html":
			{
				htmltext = event;
				break;
			}
			case "30673-03.htm":
			{
				if (player.getLevel() >= MIN_LEVEL && st.hasQuestItems(LORAINES_CERTIFICATE))
				{
					st.startQuest();
					st.giveItems(METALLOGRAPH_RESEARCH_REPORT, 1);
					st.takeItems(LORAINES_CERTIFICATE, -1);
					htmltext = event;
				}
				break;
			}
			case "30621-03.html":
			{
				if (st.isCond(1))
				{
					st.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "31437-03.html":
			{
				if (st.isCond(2) && st.hasQuestItems(LETO_LIZARDMAN_ACCESSORY))
					htmltext = event;
				break;
			}
			case "31437-04.html":
			{
				if (st.isCond(2) && st.hasQuestItems(LETO_LIZARDMAN_ACCESSORY))
				{
					st.setCond(3);
					htmltext = event;
				}
				break;
			}
			case "31437-06.html":
			{
				if (st.isCond(3))
				{
					st.giveAdena(105083, true);
					if (player.getLevel() < MAX_LEVEL_FOR_EXP_SP)
						st.addExpAndSp(285935, 18711);
					st.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && st.isCond(2) && Util.checkIfInRange(1500, npc, killer, false))
			if (!st.hasQuestItems(LETO_LIZARDMAN_ACCESSORY))
			{
				st.giveItems(LETO_LIZARDMAN_ACCESSORY, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (npc.getId())
		{
			case RESEARCHER_LORAIN:
			{
				switch (st.getState())
				{
					case State.CREATED:
					{
						final QuestState qs = player.getQuestState(Q00184_ArtOfPersuasion.class.getSimpleName());
						if (qs != null && qs.isCompleted() && st.hasQuestItems(LORAINES_CERTIFICATE))
							htmltext = player.getLevel() >= MIN_LEVEL ? "30673-01.htm" : "30673-02.htm";
						break;
					}
					case State.STARTED:
					{
						if (st.getCond() >= 1)
							htmltext = "30673-04.html";
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(player);
						break;
					}
				}
				break;
			}
			case MAESTRO_NIKOLA:
			{
				if (st.isStarted())
					htmltext = st.isCond(1) ? "30621-01.html" : "30621-04.html";
				break;
			}
			case BLUEPRINT_SELLER_LUKA:
			{
				switch (st.getCond())
				{
					case 2:
					{
						htmltext = st.hasQuestItems(LETO_LIZARDMAN_ACCESSORY) ? "31437-02.html" : "31437-01.html";
						break;
					}
					case 3:
					{
						htmltext = "31437-05.html";
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
		new Q00186_ContractExecution(186, Q00186_ContractExecution.class.getSimpleName(), "Contract Execution");
	}
}