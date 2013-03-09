package hellbound.Kanaf;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.util.Rnd;

public class Kanaf extends Quest
{
	private static final int KANAF = 32346;

	public Kanaf(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(KANAF);
		addTalkId(KANAF);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("info"))
			return "32346-0" + (Rnd.get(3) + 1) + ".htm";
		
		return null; 
	}
	

	public static void main(String[] args)
	{
		new Kanaf(-1, Kanaf.class.getSimpleName(), "hellbound");
	}
}