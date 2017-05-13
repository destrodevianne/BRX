package village_master.Asterios;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public final class Asterios extends Quest
{
	private static final String qn = "Asterios_occupation_change";
	
	// Quest NPCs
	private static final int ASTERIOS = 30154;
	
	public Asterios(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(ASTERIOS);
		addTalkId(ASTERIOS);
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
			case elvenFighter:
				return "30154-01.htm";
			case elvenMage:
				return "30154-02.htm";
			case elvenWizard:
			case oracle:
			case elvenKnight:
			case elvenScout:
				return "30154-12.htm";
			default:
				return "30154-11.htm";
		}
	}
	
	public static void main(final String[] args)
	{
		new Asterios(-1, qn, "village_master");
	}
}