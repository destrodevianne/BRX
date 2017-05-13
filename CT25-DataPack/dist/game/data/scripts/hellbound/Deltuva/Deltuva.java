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
package hellbound.Deltuva;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00132_MatrasCuriosity.Q00132_MatrasCuriosity;

public class Deltuva extends Quest
{
	// Npc
	private static final int DELTUVA = 32313;

	public Deltuva(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(DELTUVA);
		addTalkId(DELTUVA);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = null;
		if (event.equalsIgnoreCase("teleport"))
		{
			final QuestState hostQuest = player.getQuestState(Q00132_MatrasCuriosity.class.getSimpleName());
			
			if (hostQuest != null && hostQuest.getState() == State.COMPLETED)
				player.teleToLocation(17934, 283189, -9701);
			else
				htmltext = "32313-02.htm";
		}

		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new Deltuva(-1, Deltuva.class.getSimpleName(), "hellbound");
	}
}
