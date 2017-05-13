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
package hellbound.Jude;

import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public class Jude extends Quest
{
	// Npc
	private static final int JUDE = 32356;

	// Item
	private static final int TREASURE = 9684;
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if ("TreasureSacks".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() == 3)
				if (player.getInventory().getInventoryItemCount(TREASURE, -1, false) >= 40)
					if (player.destroyItemByItemId("Quest", TREASURE, 40, npc, true))
					{
						player.addItem("Quest", 9677, 1, npc, true);
						return "32356-02.htm";
					}
			return "32356-02a.htm";
		}
		
		return event;
	}
	
	@Override
	public final String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		switch (HellboundManager.getInstance().getLevel())
		{
			case 0:
			case 1:
			case 2:
				return "32356-01.htm";
			case 3:
			case 4:
				return "32356-01c.htm";
			case 5:
				return "32356-01a.htm";
			default:
				return "32356-01b.htm";
		}
	}
	
	public Jude(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(JUDE);
		addStartNpc(JUDE);
		addTalkId(JUDE);
	}
	
	public static void main(final String[] args)
	{
		new Jude(-1, Jude.class.getSimpleName(), "hellbound");
	}
}
