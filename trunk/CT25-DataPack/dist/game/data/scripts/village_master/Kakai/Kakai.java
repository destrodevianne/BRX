package village_master.Kakai;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public final class Kakai extends Quest
{
	private static final String qn = "Kakai_occupation_change";
	
	// Quest NPCs
	private static final int KAKAI = 30565;
	
	public Kakai(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(KAKAI);
		addTalkId(KAKAI);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.contains("-01") || event.contains("-02") || event.contains("-03") || event.contains("-04") || event.contains("-05") || event.contains("-06") || event.contains("-07") || event.contains("-08"))
			return event;
		
		return null;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance talker)
	{
		switch (talker.getClassId())
		{
			case orcFighter:
				return "30565-01.htm";
			case orcRaider:
			case orcMonk:
			case orcShaman:
				return "30565-09.htm";
			case destroyer:
			case tyrant:
			case overlord:
			case warcryer:
				return "30565-10.htm";
			case orcMage:
				return "30565-06.htm";
			default:
				return "30565-11.htm";
		}
	}
	
	public static void main(final String[] args)
	{
		new Kakai(-1, qn, "village_master");
	}
}