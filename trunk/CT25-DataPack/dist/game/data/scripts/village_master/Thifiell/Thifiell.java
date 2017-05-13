package village_master.Thifiell;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public final class Thifiell extends Quest
{
	private static final String qn = "Thifiell_occupation_change";
	
	// Quest NPCs
	private static final int THIFIELL = 30358;
	
	public Thifiell(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(THIFIELL);
		addTalkId(THIFIELL);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.contains("-01") || event.contains("-02") || event.contains("-03") || event.contains("-04") || event.contains("-05") || event.contains("-06") || event.contains("-07") || event.contains("-08") || event.contains("-09") || event.contains("-10"))
			return event;
		
		return null;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance talker)
	{
		switch (talker.getClassId())
		{
			case darkFighter:
				return "30358-01.htm";
			case darkMage:
				return "30358-02.htm";
			case darkWizard:
			case shillienOracle:
			case palusKnight:
			case assassin:
				return "30358-12.htm";
			default:
				return "30358-11.htm";
		}
	}
	
	public static void main(final String[] args)
	{
		new Thifiell(-1, qn, "village_master");
	}
}