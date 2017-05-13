package teleports.PaganTeleporters;

import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class PaganTeleporters extends Quest
{
	private static final String qn = "PaganTeleporters";
	
	public PaganTeleporters(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(32039, 32040);
		addStartNpc(32034, 32035, 32036, 32037, 32039, 32040);
		addTalkId(32034, 32035, 32036, 32037, 32039, 32040);

	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Close_Door1"))
			DoorTable.getInstance().getDoor(19160001).closeMe();
		else if (event.equalsIgnoreCase("Close_Door2"))
		{
			DoorTable.getInstance().getDoor(19160010).closeMe();
			DoorTable.getInstance().getDoor(19160011).closeMe();
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();
		if (npcId == 32039)
			player.teleToLocation(-12766, -35840, -10856);
		else if (npcId == 32040)
			player.teleToLocation(34962, -49758, -763);
		
		return null;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		
		final int npcId = npc.getId();
		if (npcId == 32034)
		{
			if (st.getQuestItemsCount(8064) == 0 && st.getQuestItemsCount(8065) == 0 && st.getQuestItemsCount(8067) == 0)
				htmltext = "32034-1.htm";
			else if (st.hasQuestItems(8064))
			{
				st.takeItems(8064, 1); // This part must happen when u walk through doors >.<
				st.giveItems(8065, 1);
			}
			
			DoorTable.getInstance().getDoor(19160001).openMe();
			startQuestTimer("Close_Door1", 10000, npc, player);
			htmltext = "FadedMark.htm";
		}
		else if (npcId == 32035)
		{
			DoorTable.getInstance().getDoor(19160001).openMe();
			startQuestTimer("Close_Door1", 10000, npc, player);
			htmltext = "FadedMark.htm";
		}
		else if (npcId == 32036)
		{
			if (!st.hasQuestItems(8067))
				htmltext = "32036-1.htm";
			else
			{
				DoorTable.getInstance().getDoor(19160010).openMe();
				DoorTable.getInstance().getDoor(19160011).openMe();
				startQuestTimer("Close_Door2", 10000, npc, player);
				htmltext = "32036-2.htm";
			}
		}
		else if (npcId == 32037)
		{
			DoorTable.getInstance().getDoor(19160010).openMe();
			DoorTable.getInstance().getDoor(19160011).openMe();
			startQuestTimer("Close_Door2", 10000, npc, player);
			htmltext = "FadedMark.htm";
		}
		
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new PaganTeleporters(-1, qn, "teleports");
	}
}