package teleports.GraciaHeart;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class GraciaHeart extends Quest
{
	private static final String qn = "GraciaHeart";

	private final static int EmergyCompressor = 36570;

	public GraciaHeart(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(EmergyCompressor);
		addTalkId(EmergyCompressor);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(qn);
		final int npcId = npc.getId();
		if (npcId == EmergyCompressor)
			if (player.getLevel() >= 75)
				player.teleToLocation(-204288, 242026, 1744);
			else
				htmltext = "36570-00.htm";
			
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new GraciaHeart(-1, qn, "teleports");
	}
}
