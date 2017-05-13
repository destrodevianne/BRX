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
package quests.Q00610_MagicalPowerOfWaterPart2;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.util.Util;

/**
 * Magical Power of Water - Part 2 (610)
 * @author Joxit
 */
public class Q00610_MagicalPowerOfWaterPart2 extends Quest
{
	// NPCs
	private static final int ASEFA = 31372;
	private static final int VARKA_TOTEM = 31560;
	// Monster
	private static final int ASHUTAR = 25316;
	// Items
	private static final int GREEN_TOTEM = 7238;
	private static final int ASHUTAR_HEART = 7239;
	// Misc
	private static final int MIN_LEVEL = 75;

	private Q00610_MagicalPowerOfWaterPart2(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(ASEFA);
		addTalkId(ASEFA, VARKA_TOTEM);
		addKillId(ASHUTAR);
		registerQuestItems(GREEN_TOTEM, ASHUTAR_HEART);

		final String test = loadGlobalQuestVar("Q00610_respawn");
		final long remain = !test.isEmpty() ? Long.parseLong(test) - System.currentTimeMillis() : 0;
		if (remain > 0)
			startQuestTimer("spawn_npc", remain, null, null);
		else
			addSpawn(VARKA_TOTEM, 105452, -36775, -1050, 34000, false, 0, true);
	}

	@Override
	public void actionForEachPlayer(final L2PcInstance player, final L2Npc npc, final boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && Util.checkIfInRange(1500, npc, player, false))
			if (npc.getId() == ASHUTAR)
				switch (st.getCond())
				{
					case 1: // take the item and give the heart
						st.takeItems(GREEN_TOTEM, 1);
					case 2:
						if (!st.hasQuestItems(ASHUTAR_HEART))
							st.giveItems(ASHUTAR_HEART, 1);
						st.setCond(3, true);
						break;
				}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = null;
		if (player != null)
		{
			final QuestState st = player.getQuestState(getName());
			if (st == null)
				return null;

			switch (event)
			{
				case "31372-02.html":
					st.startQuest();
					htmltext = event;
					break;
				case "give_heart":
					if (st.hasQuestItems(ASHUTAR_HEART))
					{
						st.addExpAndSp(10000, 0);
						st.exitQuest(true, true);
						htmltext = "31372-06.html";
					}
					else
						htmltext = "31372-07.html";
					break;
				case "spawn_totem":
					htmltext = st.hasQuestItems(GREEN_TOTEM) ? spawnAshutar(npc, st) : "31560-04.html";
					break;
			}
		}
		else if (event.equals("despawn_ashutar"))
		{
			npc.broadcastNpcSay(Say2.NPC_ALL, "The power of constraint is getting weaker. Your ritual has failed!");
			npc.deleteMe();
			addSpawn(VARKA_TOTEM, 105452, -36775, -1050, 34000, false, 0, true);
		}
		else if (event.equals("spawn_npc"))
			addSpawn(VARKA_TOTEM, 105452, -36775, -1050, 34000, false, 0, true);
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int respawnMinDelay = (int) (43200000 * Config.RAID_MIN_RESPAWN_MULTIPLIER);
		final int respawnMaxDelay = (int) (129600000 * Config.RAID_MAX_RESPAWN_MULTIPLIER);
		final int respawnDelay = getRandom(respawnMinDelay, respawnMaxDelay);
		cancelQuestTimer("despawn_ashutar", npc, null);
		saveGlobalQuestVar("Q00610_respawn", String.valueOf(System.currentTimeMillis() + respawnDelay));
		startQuestTimer("spawn_npc", respawnDelay, null, null);
		executeForEachPlayer(killer, npc, isPet, true, false);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (npc.getId())
		{
			case ASEFA:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = player.getLevel() >= MIN_LEVEL ? st.hasQuestItems(GREEN_TOTEM) ? "31372-01.htm" : "31372-00a.html" : "31372-00b.html";
						break;
					case State.STARTED:
						htmltext = st.isCond(1) ? "31372-03.html" : st.hasQuestItems(ASHUTAR_HEART) ? "31372-04.html" : "31372-05.html";
						break;
				}
				break;
			case VARKA_TOTEM:
				if (st.isStarted())
					switch (st.getCond())
					{
						case 1:
							htmltext = "31560-01.html";
							break;
						case 2:
							htmltext = spawnAshutar(npc, st);
							break;
						case 3:
							htmltext = "31560-05.html";
							break;
					}
				break;
		}
		return htmltext;
	}

	private String spawnAshutar(final L2Npc npc, final QuestState st)
	{
		if (getQuestTimer("spawn_npc", null, null) != null)
			return "31560-03.html";
		if (st.isCond(1))
		{
			st.takeItems(GREEN_TOTEM, 1);
			st.setCond(2, true);
		}
		npc.deleteMe();
		final L2Npc ashutar = addSpawn(ASHUTAR, 104825, -36926, -1136, 0, false, 0);
		ashutar.broadcastNpcSay(Say2.NPC_ALL, "The magical power of water comes from the power of storm and hail! If you dare to confront it, only death will await you!");
		startQuestTimer("despawn_ashutar", 1200000, ashutar, null);
		return "31560-02.html";
	}

	public static void main(final String[] args)
	{
		new Q00610_MagicalPowerOfWaterPart2(610, Q00610_MagicalPowerOfWaterPart2.class.getSimpleName(), "Magical Power of Water - Part 2");
	}
}