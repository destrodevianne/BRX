package teleports.StrongholdsTeleports;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public class StrongholdsTeleports extends Quest
{
	private static final String qn = "StrongholdsTeleports";
	
	private final static int[] NPCs =
	{
		32163,
		32181,
		32184,
		32186
	};
	
	public StrongholdsTeleports(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int id : NPCs)
		{
			addStartNpc(id);
			addFirstTalkId(id);
			addTalkId(id);
		}
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		if (player.getLevel() < 20 && npc.getId() == 32163)
			htmltext = "32163-4.htm";
		else
			htmltext = "32163-5.htm";
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		if (player.getLevel() < 20)
			htmltext = npc.getId() + ".htm";
		else
			htmltext = npc.getId() + "-no.htm";
		
		npc.showChatWindow(player);
		
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new StrongholdsTeleports(-1, qn, "teleports");
	}
}