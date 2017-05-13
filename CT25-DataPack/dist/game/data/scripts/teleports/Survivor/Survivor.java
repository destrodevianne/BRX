package teleports.Survivor;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class Survivor extends Quest
{
	private static final String qn = "Survivor";
	
	private final static int survivor = 32632;
	
	public Survivor(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(survivor);
		addTalkId(survivor);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		if (player.getLevel() >= 75)
		{
			if (st.getQuestItemsCount(57) >= 150000)
			{
				player.teleToLocation(-149406, 255247, -80);
				st.takeItems(57, 150000);
			}
			else
				htmltext = "32632-2.htm";
		}
		else
			htmltext = "32632-3.htm";
		
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new Survivor(-1, qn, "teleports");
	}
}