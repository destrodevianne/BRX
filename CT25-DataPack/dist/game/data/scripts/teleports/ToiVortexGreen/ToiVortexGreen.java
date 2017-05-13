package teleports.ToiVortexGreen;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class ToiVortexGreen extends Quest
{
	private static final String qn = "ToiVortexGreen";
	
	private final static int DIMENSION_VORTEX_2 = 30953;
	private final static int DIMENSION_VORTEX_3 = 30954;
	
	private final static int GREEN_DIMENSION_STONE = 4401;
	
	public ToiVortexGreen(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(DIMENSION_VORTEX_2, DIMENSION_VORTEX_3);
		addTalkId(DIMENSION_VORTEX_2, DIMENSION_VORTEX_3);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		final int npcId = npc.getId();
		if (npcId == DIMENSION_VORTEX_2 || npcId == DIMENSION_VORTEX_3)
			if (st.getQuestItemsCount(GREEN_DIMENSION_STONE) >= 1)
			{
				st.takeItems(GREEN_DIMENSION_STONE, 1);
				player.teleToLocation(110930, 15963, -4378);
			}
			else
				htmltext = "1.htm";
			
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new ToiVortexGreen(-1, qn, "teleports");
	}
}