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
package quests.Q00998_FallenAngelSelect;

import quests.Q00141_ShadowFoxPart3.Q00141_ShadowFoxPart3;
import quests.Q00142_FallenAngelRequestOfDawn.Q00142_FallenAngelRequestOfDawn;
import quests.Q00143_FallenAngelRequestOfDusk.Q00143_FallenAngelRequestOfDusk;

import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Fallen Angel Select (998 - Custom)<br>
 * NOTE: This quest is used for start quest 142 or 143
 * @author Nono
 */
public class Q00998_FallenAngelSelect extends Quest
{
	// NPCs
	private static final int NATOOLS = 30894;
	// Misc
	private static final int MIN_LEVEL = 38;
	
	private Q00998_FallenAngelSelect(int questId, String name, String descr)
	{
		super(questId, name, descr);
		setIsCustom(true);
		addStartNpc(NATOOLS);
		addTalkId(NATOOLS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		switch (event)
		{
			case "30894-01.html":
			case "30894-02.html":
			case "30894-03.html":
				return event;
			case "dawn":
				startQuest(Q00142_FallenAngelRequestOfDawn.class.getSimpleName(), player);
				break;
			case "dusk":
				startQuest(Q00143_FallenAngelRequestOfDusk.class.getSimpleName(), player);
				break;
		}
		return null;
	}
	
	private void startQuest(String name, L2PcInstance player)
	{
		final Quest q = QuestManager.getInstance().getQuest(name);
		if (q != null)
		{
			q.newQuestState(player);
			q.notifyEvent("30894-01.html", null, player);
			player.getQuestState(getName()).setState(State.COMPLETED);
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		final QuestState qs = player.getQuestState(Q00141_ShadowFoxPart3.class.getSimpleName());
		if ((st == null) || !st.isStarted())
		{
			return getNoQuestMsg(player);
		}
		return ((player.getLevel() >= MIN_LEVEL) && (qs != null) && qs.isCompleted()) ? "30894-01.html" : "30894-00.html";
	}
	
	public static void main(String[] args)
	{
		new Q00998_FallenAngelSelect(998, Q00998_FallenAngelSelect.class.getSimpleName(), "Fallen Angel - Select");
	}
}