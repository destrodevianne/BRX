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
package hellbound.Village;

import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;

public class Village extends Quest
{
	// Npcs
	private static final int NATIVE = 32362;
	private static final int INSURGENT = 32363;
	private static final int TRAITOR = 32364;
	private static final int INCASTLE = 32357;

	// Items
	private static final int MARK_OF_BETRAYAL = 9676;
	private static final int BADGES = 9674;

	// Items
	private static final int[] FSTRING_ID =
	{
		1800073, // Hun.. hungry
		1800111 // Alright, now Leodas is yours!
	};

	// Door Ids
	private static final int[] doors =
	{
		19250003,
		19250004
	};

	@Override
	public final String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int hellboundLevel = HellboundManager.getInstance().getLevel();
		final int npcId = npc.getId();

		if (npcId == NATIVE)
			return hellboundLevel > 5 ? "32362-01.htm" : "32362.htm";
		
		else if (npcId == INSURGENT)
			return hellboundLevel > 5 ? "32363-01.htm" : "32363.htm";

		else if (npcId == INCASTLE)
			if (hellboundLevel < 9)
				return "32357-01a.htm";
			
			else if (hellboundLevel == 9)
				return "32357-01.htm";
			
			else
				return "32357-01b.htm";
			
		return null;
	}

	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = null;
		if (npc.getId() == TRAITOR)
		{
			if (event.equalsIgnoreCase("open_door"))
			{
				if (player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) >= 10)
				{
					if (player.destroyItemByItemId("Quest", MARK_OF_BETRAYAL, 10, npc, true))
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), FSTRING_ID[1]));
						HellboundManager.getInstance().updateTrust(-50, true);
						
						for (final int doorId : doors)
						{
							final L2DoorInstance door = DoorTable.getInstance().getDoor(doorId);
							if (door != null)
								door.openMe();
						}
						
						cancelQuestTimers("close_doors");
						startQuestTimer("close_doors", 1800000, npc, player); // 30 min
					}
				}
				
				else if (player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) > 0 && player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) < 10)
					htmltext = "32364-01.htm";
				
				else
					htmltext = "32364-02.htm";
			}

			else if (event.equalsIgnoreCase("close_doors"))
				for (final int doorId : doors)
				{
					final L2DoorInstance door = DoorTable.getInstance().getDoor(doorId);
					if (door != null)
						door.closeMe();
				}
		}

		else if (npc.getId() == NATIVE && event.equalsIgnoreCase("hungry_death"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), FSTRING_ID[0]));
			npc.doDie(null);
		}

		else if (npc.getId() == INCASTLE)
			if (event.equalsIgnoreCase("FreeSlaves"))
			{
				if (player.getInventory().getInventoryItemCount(BADGES, -1, false) >= 5)
				{
					if (player.destroyItemByItemId("Quest", BADGES, 5, npc, true))
					{
						HellboundManager.getInstance().updateTrust(100, true);
						htmltext = "32357-02.htm";
						startQuestTimer("delete_me", 3000, npc, null);
					}
				}
				else
					htmltext = "32357-02a.htm";
			}
			
			else if (event.equalsIgnoreCase("delete_me"))
			{
				npc.deleteMe();
				npc.getSpawn().decreaseCount(npc);
			}

		return htmltext;
	}

	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (npc.getId() == NATIVE && HellboundManager.getInstance().getLevel() < 6)
			startQuestTimer("hungry_death", 600000, npc, null);

		return super.onSpawn(npc);
	}
	
	public Village(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(NATIVE, INSURGENT, INCASTLE);
		addStartNpc(TRAITOR, INCASTLE);
		addTalkId(TRAITOR, INCASTLE);
		addSpawnId(NATIVE);
	}
	
	public static void main(final String[] args)
	{
		new Village(-1, Village.class.getSimpleName(), "hellbound");
	}
}
