package teleports.ToiVortexRed;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class ToiVortexRed extends Quest
{
	private static final String qn = "ToiVortexRed";
	
	private final static int DIMENSION_VORTEX_1 = 30952;
	private final static int DIMENSION_VORTEX_2 = 30953;
	
	private final static int RED_DIMENSION_STONE = 4403;
	
	public ToiVortexRed(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(DIMENSION_VORTEX_1, DIMENSION_VORTEX_2);
		addTalkId(DIMENSION_VORTEX_2, DIMENSION_VORTEX_2);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		final int npcId = npc.getId();
		if (npcId == DIMENSION_VORTEX_1 || npcId == DIMENSION_VORTEX_2)
			if (st.getQuestItemsCount(RED_DIMENSION_STONE) >= 1)
			{
				st.takeItems(RED_DIMENSION_STONE, 1);
				player.teleToLocation(118558, 16659, 5987);
			}
			else
				htmltext = "1.htm";
			
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new ToiVortexRed(-1, qn, "teleports");
	}
}