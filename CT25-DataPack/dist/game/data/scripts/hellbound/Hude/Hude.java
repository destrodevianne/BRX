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
package hellbound.Hude;

import ct25.xtreme.gameserver.datatables.MultiSell;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public class Hude extends Quest
{
	// Npc
	private static final int HUDE = 32298;

	// Items
	private static final int BASIC_CERT = 9850;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;
	private static final int MARK_OF_BETRAYAL = 9676;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;
	private static final int MAP = 9994;
	private static final int STINGER = 10012;

	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if ("scertif".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() > 3)
				if (player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) >= 30 && player.getInventory().getInventoryItemCount(STINGER, -1, false) >= 60 && player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) > 0)
					if (player.destroyItemByItemId("Quest", MARK_OF_BETRAYAL, 30, npc, true) && player.destroyItemByItemId("Quest", STINGER, 60, npc, true) && player.destroyItemByItemId("Quest", BASIC_CERT, 1, npc, true))
					{
						player.addItem("Quest", STANDART_CERT, 1, npc, true);
						return "32298-04a.htm";
					}
			return "32298-04b.htm";
		}
		else if ("pcertif".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getLevel() > 6)
				if (player.getInventory().getInventoryItemCount(LIFE_FORCE, -1, false) >= 56 && player.getInventory().getInventoryItemCount(CONTAINED_LIFE_FORCE, -1, false) >= 14 && player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
					if (player.destroyItemByItemId("Quest", LIFE_FORCE, 56, npc, true) && player.destroyItemByItemId("Quest", CONTAINED_LIFE_FORCE, 14, npc, true) && player.destroyItemByItemId("Quest", STANDART_CERT, 1, npc, true))
					{
						player.addItem("Quest", PREMIUM_CERT, 1, npc, true);
						player.addItem("Quest", MAP, 1, npc, true);
						return "32298-06a.htm";
					}
			return "32298-06b.htm";
		}
		else if ("multisell1".equalsIgnoreCase(event))
		{
			if (player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0 || player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
				MultiSell.getInstance().separateAndSend(322980001, player, npc, false);
		}
		
		else if ("multisell2".equalsIgnoreCase(event))
			if (player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
				MultiSell.getInstance().separateAndSend(322980002, player, npc, false);
			
		return null;
	}
	
	@Override
	public final String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		if (player.getTransformationId() != 101)
			return "32298-01.htm";

		if (player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) < 1 && player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) < 1 && player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) < 1)
			return "32298-01.htm";

		else if (player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) > 0 && player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) < 1 && player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) < 1)
			return "32298-03.htm";
		
		else if (player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0 && player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) < 1)
			return "32298-05.htm";
		
		else if (player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
			return "32298-07.htm";
		
		return null;
	}
	
	public Hude(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(HUDE);
		addStartNpc(HUDE);
		addTalkId(HUDE);
	}
	
	public static void main(final String[] args)
	{
		new Hude(-1, Hude.class.getSimpleName(), "hellbound");
	}
}