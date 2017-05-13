package village_master.Clan;

import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

public class Clan extends Quest
{
	
	private static final String qn = "Clan";
	private final static int[] _questNpc =
	{
		// clan master id
		30026,
		30031,
		30037,
		30066,
		30070,
		30109,
		30115,
		30120,
		30154,
		30174,
		30175,
		30176,
		30187,
		30191,
		30195,
		30288,
		30289,
		30290,
		30297,
		30358,
		30373,
		30462,
		30474,
		30498,
		30499,
		30500,
		30503,
		30504,
		30505,
		30508,
		30511,
		30512,
		30513,
		30520,
		30525,
		30565,
		30594,
		30595,
		30676,
		30677,
		30681,
		30685,
		30687,
		30689,
		30694,
		30699,
		30704,
		30845,
		30847,
		30849,
		30854,
		30857,
		30862,
		30865,
		30894,
		30897,
		30900,
		30905,
		30910,
		30913,
		31269,
		31272,
		31276,
		31279,
		31285,
		31288,
		31314,
		31317,
		31321,
		31324,
		31326,
		31328,
		31331,
		31334,
		31755,
		31958,
		31961,
		31965,
		31968,
		31974,
		31977,
		31996,
		32092,
		32093,
		32094,
		32095,
		32096,
		32097,
		32098,
		32145,
		32146,
		32147,
		32150,
		32153,
		32154,
		32157,
		32158,
		32160,
		32171,
		32193,
		32199,
		32202,
		32213,
		32214,
		32221,
		32222,
		32229,
		32230,
		32233,
		32234
	};

	public Clan(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int id : _questNpc)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		// If player at clan and/or clan leader, show htmltext and event
		final boolean cond = player.getClan() != null && player.isClanLeader();
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		htmltext = event;
		if (event.equalsIgnoreCase("9000-03.htm") && !cond)
			htmltext = "9000-03-no.htm";
		else if (event.equalsIgnoreCase("9000-04.htm") && !cond)
			htmltext = "9000-04-no.htm";
		else if (event.equalsIgnoreCase("9000-05.htm") && !cond)
			htmltext = "9000-05-no.htm";
		else if (event.equalsIgnoreCase("9000-07.htm") && !cond)
			htmltext = "9000-07-no.htm";
		else if (event.equalsIgnoreCase("9000-06a.htm") && !cond)
			htmltext = "9000-07-no.htm";
		else if (event.equalsIgnoreCase("9000-12a.htm") && !cond)
			htmltext = "9000-07-no.htm";
		else if (event.equalsIgnoreCase("9000-13a.htm") && !cond)
			htmltext = "9000-07-no.htm";
		else if (event.equalsIgnoreCase("9000-13b.htm") && !cond)
			htmltext = "9000-07-no.htm";
		else if (event.equalsIgnoreCase("9000-14a.htm") && !cond)
			htmltext = "9000-07-no.htm";
		else if (event.equalsIgnoreCase("9000-15.htm") && !cond)
			htmltext = "9000-07-no.htm";
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(qn);
			st = q.newQuestState(player);
		}
		st.set("cond", "0");
		st.setState(State.STARTED);
		return "9000-01.htm";

	}
	
	public static void main(final String[] args)
	{
		new Clan(-1, qn, "village_master");
	}
}