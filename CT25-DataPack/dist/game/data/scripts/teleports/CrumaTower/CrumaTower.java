package teleports.CrumaTower;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class CrumaTower extends Quest
{
	private static final String qn = "CrumaTower";
	
	private final static int NPC = 30483;
	
	public CrumaTower(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		if (player.getLevel() > 55)
			htmltext = "30483.htm";
		else
			player.teleToLocation(17724, 114004, -11672);
		
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new CrumaTower(-1, qn, "teleports");
	}
}