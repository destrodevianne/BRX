package teleports.GrandBossTeleporters;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2GrandBossInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.zone.type.L2BossZone;
import ct25.xtreme.util.Rnd;

public class GrandBossTeleporters extends Quest
{
	private static final String qn = "GrandBossTeleporters";
	
	private final static int[] NPCs =
	{
		13001,
		31859,
		31384,
		31385,
		31540,
		31686,
		31687,
		31759
	};
	
	private Quest antharasAI()
	{
		return QuestManager.getInstance().getQuest("antharas");
	}
	
	private Quest valakasAI()
	{
		return QuestManager.getInstance().getQuest("valakas");
	}
	
	private int count = 0;
	
	public GrandBossTeleporters(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int id : NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("31540"))
			if (st.hasQuestItems(7267))
			{
				st.takeItems(7267, 1);
				player.teleToLocation(183813, -115157, -3303);
				st.set("allowEnter", "1");
			}
			else
				htmltext = "31540-06.htm";
			
		return htmltext;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		
		final int npcId = npc.getId();
		if (npcId == 13001)
		{
			if (antharasAI() != null)
			{
				final int status = GrandBossManager.getInstance().getBossStatus(29019);
				final int statusW = GrandBossManager.getInstance().getBossStatus(29066);
				final int statusN = GrandBossManager.getInstance().getBossStatus(29067);
				final int statusS = GrandBossManager.getInstance().getBossStatus(29068);
				
				if (status == 2 || statusW == 2 || statusN == 2 || statusS == 2)
					htmltext = "13001-02.htm";
				else if (status == 3 || statusW == 3 || statusN == 3 || statusS == 3)
					htmltext = "13001-01.htm";
				else if (status == 0 || status == 1)
					if (st.hasQuestItems(3865))
					{
						st.takeItems(3865, 1);
						final L2BossZone zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
						if (zone != null)
							zone.allowPlayerEntry(player, 30);
						final int x = 174170 + Rnd.get(260);
						final int y = 113983 + Rnd.get(1500);
						player.teleToLocation(x, y, -7709);
						if (status == 0)
						{
							final L2GrandBossInstance antharas = GrandBossManager.getInstance().getBoss(29019);
							antharasAI().startQuestTimer("1001", 1000, antharas, null);
							GrandBossManager.getInstance().setBossStatus(29019, 1);
						}
					}
					else
						htmltext = "13001-03.htm";
			}
			else
				htmltext = "13001-01.htm";
		}
		else if (npcId == 31859)
		{
			final int x = 79800 + Rnd.get(600);
			final int y = 151200 + Rnd.get(1100);
			player.teleToLocation(x, y, -3534);
		}
		else if (npcId == 31385)
		{
			if (valakasAI() != null)
			{
				final int status = GrandBossManager.getInstance().getBossStatus(29028);
				if (status == 0 || status == 1)
				{
					if (count >= 200)
						htmltext = "31385-03.htm";
					else if (st.getInt("allowEnter") == 1)
					{
						st.unset("allowEnter");
						final L2BossZone zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
						if (zone != null)
							zone.allowPlayerEntry(player, 30);
						final int x = 204328 + Rnd.get(600);
						final int y = -111874 + Rnd.get(600);
						player.teleToLocation(x, y, 70);
						count++;
						if (status == 0)
						{
							final L2GrandBossInstance valakas = GrandBossManager.getInstance().getBoss(29028);
							valakasAI().startQuestTimer("1001", Config.Valakas_Wait_Time, valakas, null);
							GrandBossManager.getInstance().setBossStatus(29028, 1);
						}
					}
					else // player cheated, wasn't ported via npc Klein
						htmltext = "31385-04.htm";
				}
				else if (status == 2)
					htmltext = "31385-02.htm";
				else
					htmltext = "31385-01.htm";
			}
			else
				htmltext = "31385-01.htm";
		}
		else if (npcId == 31384)
			DoorTable.getInstance().getDoor(24210004).openMe();
		else if (npcId == 31686)
			DoorTable.getInstance().getDoor(24210006).openMe();
		else if (npcId == 31687)
			DoorTable.getInstance().getDoor(24210005).openMe();
		else if (npcId == 31540)
		{
			if (count < 50)
				htmltext = "31540-01.htm";
			else if (count < 100)
				htmltext = "31540-02.htm";
			else if (count < 150)
				htmltext = "31540-03.htm";
			else if (count < 200)
				htmltext = "31540-04.htm";
			else
				htmltext = "31540-05.htm";
		}
		else if (npcId == 31759)
		{
			final int x = 150037 + Rnd.get(500);
			final int y = -57720 + Rnd.get(500);
			player.teleToLocation(x, y, -2976);
		}
		
		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new GrandBossTeleporters(-1, qn, "teleports");
	}
}