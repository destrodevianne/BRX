package village_master.Bronk;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public final class Bronk extends Quest
{
	private static final String qn = "Bronk_occupation_change";
	
	// Quest NPCs
	private static final int BRONK = 30525;
	
	public Bronk(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(BRONK);
		addTalkId(BRONK);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.contains("-01") || event.contains("-02") || event.contains("-03") || event.contains("-04"))
			return event;
		
		return null;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance talker)
	{
		switch (talker.getClassId())
		{
			case dwarvenFighter:
				return "30525-01.htm";
			case artisan:
				return "30525-05.htm";
			case warsmith:
				return "30525-06.htm";
			case scavenger:
			case bountyHunter:
				return "30525-07.htm";
			default:
				return "30525-07.htm";
		}
	}
	
	public static void main(final String[] args)
	{
		new Bronk(-1, qn, "village_master");
	}
}