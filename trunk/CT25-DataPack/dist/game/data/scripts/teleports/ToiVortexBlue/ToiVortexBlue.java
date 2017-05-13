package teleports.ToiVortexBlue;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class ToiVortexBlue extends Quest
{
	private static final String qn = "ToiVortexBlue";
	
	private final static int DIMENSION_VORTEX_1 = 30952;
	private final static int DIMENSION_VORTEX_3 = 30954;
	
	private final static int BLUE_DIMENSION_STONE = 4402;
	
	public ToiVortexBlue(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(DIMENSION_VORTEX_1, DIMENSION_VORTEX_3);
		addTalkId(DIMENSION_VORTEX_1, DIMENSION_VORTEX_3);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		final int npcId = npc.getId();
		if (npcId == DIMENSION_VORTEX_1 || npcId == DIMENSION_VORTEX_3)
			if (st.getQuestItemsCount(BLUE_DIMENSION_STONE) >= 1)
			{
				st.takeItems(BLUE_DIMENSION_STONE, 1);
				player.teleToLocation(114097, 19935, 935);
			}
			else
				htmltext = "1.htm";
			
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new ToiVortexBlue(-1, qn, "teleports");
	}
}