package village_master.Reed;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public final class Reed extends Quest
{
	private static final String qn = "Reed_occupation_change";
	
	// Quest NPCs
	private static final int REED = 30520;
	
	public Reed(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(REED);
		addTalkId(REED);
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
				return "30520-01.htm";
			case scavenger:
			case artisan:
				return "30520-05.htm";
			case bountyHunter:
			case warsmith:
				return "30520-06.htm";
			default:
				return "30520-07.htm";
		}
	}
	
	public static void main(final String[] args)
	{
		new Reed(-1, qn, "village_master");
	}
}