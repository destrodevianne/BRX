package village_master.Biotin;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public final class Biotin extends Quest
{
	private static final String qn = "Biotin_occupation_change";
	
	// Quest NPCs
	private static final int BIOTIN = 30031;
	
	public Biotin(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(BIOTIN);
		addTalkId(BIOTIN);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.contains("-01") || event.contains("-02") || event.contains("-03") || event.contains("-04") || event.contains("-05"))
			return event;
		
		return null;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance talker)
	{
		switch (talker.getClassId())
		{
			case wizard:
			case cleric:
				return "30031-06.htm";
			case sorceror:
			case necromancer:
			case warlock:
			case bishop:
			case prophet:
				return "30031-07.htm";
			default:
				return "30031-08.htm";
		}
	}
	
	public static void main(final String[] args)
	{
		new Biotin(-1, qn, "village_master");
	}
}