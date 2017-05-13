package custom.MissQueen;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import javolution.util.FastList;

public class MissQueen extends Quest
{
	private static final String qn = "MissQueen";

	private static FastList<Integer> npcIds = new FastList<>();

	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;

	private static boolean QUEEN_ENABLED = false;

	private static final int NEWBIE_REWARD = 16;
	private static final int TRAVELER_REWARD = 32;

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (!QUEEN_ENABLED)
			return "";
		final QuestState st = player.getQuestState(qn);
		if (st == null)
			return "";
		final int newbie = player.getNewbie();
		final int level = player.getLevel();
		final int occupation_level = player.getClassId().level();
		final int pkkills = player.getPkKills();
		if (event.equalsIgnoreCase("newbie_give_coupon"))
		{
			if (6 <= level && level <= 25 && pkkills == 0 && occupation_level == 0)
			{
				if ((newbie | NEWBIE_REWARD) != newbie)
				{
					player.setNewbie(newbie | NEWBIE_REWARD);
					st.giveItems(COUPON_ONE, 1);
					return "31760-2.htm";
				}
				return "31760-1.htm";
			}
			return "31760-3.htm";
		}
		else if (event.equalsIgnoreCase("traveller_give_coupon"))
		{
			if (6 <= level && level <= 25 && pkkills == 0 && occupation_level == 1)
			{
				if ((newbie | TRAVELER_REWARD) != newbie)
				{
					player.setNewbie(newbie | TRAVELER_REWARD);
					st.giveItems(COUPON_TWO, 1);
					return "31760-5.htm";
				}
				return "31760-4.htm";
			}
			return "31760-6.htm";
		}
		return "";
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		return "31760.htm";
	}

	public MissQueen(final int id, final String name, final String desc)
	{
		super(id, name, desc);
		for (int i = 31760; i <= 31767; i++)
		{
			addStartNpc(i);
			addFirstTalkId(i);
			addTalkId(i);
			npcIds.add(i);
		}
	}

	public static void main(final String[] args)
	{
		new MissQueen(-1, qn, "custom");
	}
}
