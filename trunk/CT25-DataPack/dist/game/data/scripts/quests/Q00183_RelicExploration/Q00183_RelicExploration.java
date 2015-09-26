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
package quests.Q00183_RelicExploration;

import quests.Q00184_ArtOfPersuasion.Q00184_ArtOfPersuasion;
import quests.Q00185_NikolasCooperation.Q00185_NikolasCooperation;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Relic Exploration (183)
 * @author IvanTotov
 */
public final class Q00183_RelicExploration extends Quest
{
	// NPCs
	private static final int HEAD_BLACKSMITH_KUSTO = 30512;
	private static final int MAESTRO_NIKOLA = 30621;
	private static final int RESEARCHER_LORAIN = 30673;
	
	private Q00183_RelicExploration(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(HEAD_BLACKSMITH_KUSTO);
		addTalkId(HEAD_BLACKSMITH_KUSTO, RESEARCHER_LORAIN, MAESTRO_NIKOLA);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return null;
		}
		String htmltext = null;
		switch (event)
		{
			case "30512-02.htm":
			case "30673-02.html":
			case "30673-03.html":
			{
				htmltext = event;
				break;
			}
			case "30512-03.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "30673-04.html":
			{
				st.setCond(2, true);
				htmltext = event;
				break;
			}
			case "30621-02.html":
			{
				if (player.getLevel() < 43)
				{
					st.addExpAndSp(60000, 3000);
				}
				st.giveAdena(18100, true);
				st.exitQuest(false, true);
				htmltext = event;
				break;
			}
			case "Contract":
			{
				final Quest quest = QuestManager.getInstance().getQuest(Q00184_ArtOfPersuasion.class.getSimpleName());
				st = player.getQuestState(Q00184_ArtOfPersuasion.class.getSimpleName());
				if ((quest != null) && (st == null))
				{
					st = quest.newQuestState(player);
					st.setState(State.STARTED);
					quest.notifyEvent("30621-01.htm", npc, player);
				}
				break;
			}
			case "Consideration":
			{
				final Quest quest = QuestManager.getInstance().getQuest(Q00185_NikolasCooperation.class.getSimpleName());
				st = player.getQuestState(Q00185_NikolasCooperation.class.getSimpleName());
				if ((quest != null) && (st == null))
				{
					st = quest.newQuestState(player);
					st.setState(State.STARTED);
					quest.notifyEvent("30621-01.htm", npc, player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getId())
		{
			case HEAD_BLACKSMITH_KUSTO:
			{
				switch (st.getState())
				{
					case State.CREATED:
					{
						htmltext = (player.getLevel() < 40) ? "30512-00.htm" : "30512-01.htm";
						break;
					}
					case State.STARTED:
					{
						if (st.isCond(1))
						{
							htmltext = "30512-04.html";
						}
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
			case RESEARCHER_LORAIN:
			{
				if (st.isStarted())
				{
					htmltext = st.isCond(1) ? "30673-01.html" : "30673-05.html";
				}
				break;
			}
			case MAESTRO_NIKOLA:
			{
				if (st.isCond(2))
				{
					htmltext = "30621-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00183_RelicExploration(183, Q00183_RelicExploration.class.getSimpleName(), "Relic Exploration");
	}
}
