package teleports.ToiVortexExit;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

public class ToiVortexExit extends Quest
{
	private static final String qn = "ToiVortexExit";
	
	private final static int NPC = 29055;
	
	public ToiVortexExit(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		final int chance = st.getRandom(3);
		if (chance == 0)
		{
			final int x = 108784 + st.getRandom(100);
			final int y = 16000 + st.getRandom(100);
			final int z = -4928;
			player.teleToLocation(x, y, z);
		}
		else if (chance == 1)
		{
			final int x = 113824 + st.getRandom(100);
			final int y = 10448 + st.getRandom(100);
			final int z = -5164;
			player.teleToLocation(x, y, z);
		}
		else
		{
			final int x = 115488 + st.getRandom(100);
			final int y = 22096 + st.getRandom(100);
			final int z = -5168;
			player.teleToLocation(x, y, z);
		}
		
		st.exitQuest(true);
		return null;
	}
	
	public static void main(final String[] args)
	{
		new ToiVortexExit(-1, qn, "teleports");
	}
}