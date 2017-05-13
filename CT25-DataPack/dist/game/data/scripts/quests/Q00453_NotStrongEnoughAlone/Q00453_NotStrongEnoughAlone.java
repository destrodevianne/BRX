/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00453_NotStrongEnoughAlone;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.QuestState.QuestType;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;
import quests.Q10282_ToTheSeedOfAnnihilation.Q10282_ToTheSeedOfAnnihilation;

/**
 * Not Strong Enough Alone (453)
 * @author malyelfik
 */
public class Q00453_NotStrongEnoughAlone extends Quest
{
	// NPC
	private static final int KLEMIS = 32734;
	private static final int[] MONSTER1 =
	{
		22746,
		22747,
		22748,
		22749,
		22750,
		22751,
		22752,
		22753
	};
	private static final int[] MONSTER2 =
	{
		22754,
		22755,
		22756,
		22757,
		22758,
		22759
	};
	private static final int[] MONSTER3 =
	{
		22760,
		22761,
		22762,
		22763,
		22764,
		22765
	};
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32734-06.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32734-07.html"))
		{
			st.set("cond", "2");
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32734-08.html"))
		{
			st.set("cond", "3");
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32734-09.html"))
		{
			st.set("cond", "4");
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		final QuestState prev = player.getQuestState(Q10282_ToTheSeedOfAnnihilation.class.getSimpleName());
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 84 && prev != null && prev.getState() == State.COMPLETED)
					htmltext = "32734-01.htm";
				else
					htmltext = "32734-03.html";
				break;
			case State.STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "32734-10.html";
				else if (st.getInt("cond") == 2)
					htmltext = "32734-11.html";
				else if (st.getInt("cond") == 3)
					htmltext = "32734-12.html";
				else if (st.getInt("cond") == 4)
					htmltext = "32734-13.html";
				else if (st.getInt("cond") == 5)
				{
					final boolean i1 = Rnd.nextBoolean();
					final int i0 = Rnd.get(100);
					if (i1)
					{
						if (i0 < 9)
							st.giveItems(15815, 1);
						else if (i0 < 18)
							st.giveItems(15816, 1);
						else if (i0 < 27)
							st.giveItems(15817, 1);
						else if (i0 < 36)
							st.giveItems(15818, 1);
						else if (i0 < 47)
							st.giveItems(15819, 1);
						else if (i0 < 56)
							st.giveItems(15820, 1);
						else if (i0 < 65)
							st.giveItems(15821, 1);
						else if (i0 < 74)
							st.giveItems(15822, 1);
						else if (i0 < 83)
							st.giveItems(15823, 1);
						else if (i0 < 92)
							st.giveItems(15824, 1);
						else
							st.giveItems(15825, 1);
					}
					else if (i0 < 9)
						st.giveItems(15634, 1);
					else if (i0 < 18)
						st.giveItems(15635, 1);
					else if (i0 < 27)
						st.giveItems(15636, 1);
					else if (i0 < 36)
						st.giveItems(15637, 1);
					else if (i0 < 47)
						st.giveItems(15638, 1);
					else if (i0 < 56)
						st.giveItems(15639, 1);
					else if (i0 < 65)
						st.giveItems(15640, 1);
					else if (i0 < 74)
						st.giveItems(15641, 1);
					else if (i0 < 83)
						st.giveItems(15642, 1);
					else if (i0 < 92)
						st.giveItems(15643, 1);
					else
						st.giveItems(15644, 1);
					st.exitQuest(QuestType.DAILY, true);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					htmltext = "32734-14.html";
				}
				break;
			case State.COMPLETED:
				if (!st.isNowAvailable())
					htmltext = "32734-02.htm";
				else
				{
					st.setState(State.CREATED);
					if (player.getLevel() >= 84 && prev != null && prev.getState() == State.COMPLETED)
						htmltext = "32734-01.htm";
					else
						htmltext = "32734-03.html";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (player.getParty() != null)
			for (final L2PcInstance member : player.getParty().getPartyMembers())
				increaseNpcKill(member, npc);
		else
			increaseNpcKill(player, npc);
		return null;
	}

	private void increaseNpcKill(final L2PcInstance player, final L2Npc npc)
	{
		final QuestState st = player.getQuestState(getName());

		if (st == null)
			return;

		if (Util.contains(MONSTER1, npc.getId()) && st.getInt("cond") == 2)
		{
			int val = 0;

			if (npc.getId() == MONSTER1[0] || npc.getId() == MONSTER1[4])
				val = MONSTER1[0];
			else if (npc.getId() == MONSTER1[1] || npc.getId() == MONSTER1[5])
				val = MONSTER1[1];
			else if (npc.getId() == MONSTER1[2] || npc.getId() == MONSTER1[6])
				val = MONSTER1[2];
			else if (npc.getId() == MONSTER1[3] || npc.getId() == MONSTER1[7])
				val = MONSTER1[3];

			final int i = st.getInt(String.valueOf(val));
			if (i < 15)
				st.set(String.valueOf(val), String.valueOf(i + 1));

			if (st.getInt(String.valueOf(MONSTER1[0])) >= 15 && st.getInt(String.valueOf(MONSTER1[1])) >= 15 && st.getInt(String.valueOf(MONSTER1[2])) >= 15 && st.getInt(String.valueOf(MONSTER1[3])) >= 15)
			{
				st.set("cond", "5");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		else if (Util.contains(MONSTER2, npc.getId()) && st.getInt("cond") == 3)
		{
			int val = 0;

			if (npc.getId() == MONSTER2[0] || npc.getId() == MONSTER2[3])
				val = MONSTER2[0];
			else if (npc.getId() == MONSTER2[1] || npc.getId() == MONSTER2[4])
				val = MONSTER2[1];
			else if (npc.getId() == MONSTER2[2] || npc.getId() == MONSTER2[5])
				val = MONSTER2[2];

			final int i = st.getInt(String.valueOf(val));
			if (i < 20)
				st.set(String.valueOf(val), String.valueOf(i + 1));

			if (st.getInt(String.valueOf(MONSTER2[0])) >= 20 && st.getInt(String.valueOf(MONSTER2[1])) >= 20 && st.getInt(String.valueOf(MONSTER2[2])) >= 20)
			{
				st.set("cond", "5");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		else if (Util.contains(MONSTER3, npc.getId()) && st.getInt("cond") == 4)
		{
			int val = 0;

			if (npc.getId() == MONSTER3[0] || npc.getId() == MONSTER3[3])
				val = MONSTER3[0];
			else if (npc.getId() == MONSTER3[1] || npc.getId() == MONSTER3[4])
				val = MONSTER3[1];
			else if (npc.getId() == MONSTER3[2] || npc.getId() == MONSTER3[5])
				val = MONSTER3[2];

			final int i = st.getInt(String.valueOf(val));
			if (i < 20)
				st.set(String.valueOf(val), String.valueOf(i + 1));

			if (st.getInt(String.valueOf(MONSTER3[0])) >= 20 && st.getInt(String.valueOf(MONSTER3[1])) >= 20 && st.getInt(String.valueOf(MONSTER3[2])) >= 20)
			{
				st.set("cond", "5");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
	}

	public Q00453_NotStrongEnoughAlone(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(KLEMIS);
		addTalkId(KLEMIS);

		for (final int i : MONSTER1)
			addKillId(i);
		for (final int i : MONSTER2)
			addKillId(i);
		for (final int i : MONSTER3)
			addKillId(i);
	}

	public static void main(final String[] args)
	{
		new Q00453_NotStrongEnoughAlone(453, Q00453_NotStrongEnoughAlone.class.getSimpleName(), "Not Strong Enought Alone");
	}
}
