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
package quests.Q00616_MagicalPowerOfFirePart2;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.util.Util;

/**
 * Magical Power of Fire - Part 2 (616)
 * @author Joxit
 */
public class Q00616_MagicalPowerOfFirePart2 extends Quest
{
	// NPCs
	private static final int UDAN = 31379;
	private static final int KETRA_TOTEM = 31558;
	// Monster
	private static final int NASTRON = 25306;
	// Items
	private static final int RED_TOTEM = 7243;
	private static final int NASTRON_HEART = 7244;
	// Misc
	private static final int MIN_LEVEL = 75;

	private Q00616_MagicalPowerOfFirePart2(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(UDAN);
		addTalkId(UDAN, KETRA_TOTEM);
		addKillId(NASTRON);
		registerQuestItems(RED_TOTEM, NASTRON_HEART);

		final String test = loadGlobalQuestVar("Q00616_respawn");
		final long remain = !test.isEmpty() ? Long.parseLong(test) - System.currentTimeMillis() : 0;
		if (remain > 0)
			startQuestTimer("spawn_npc", remain, null, null);
		else
			addSpawn(KETRA_TOTEM, 142368, -82512, -6487, 58000, false, 0, true);
	}

	@Override
	public void actionForEachPlayer(final L2PcInstance player, final L2Npc npc, final boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && Util.checkIfInRange(1500, npc, player, false))
			if (npc.getId() == NASTRON)
				switch (st.getCond())
				{
					case 1: // take the item and give the heart
						st.takeItems(RED_TOTEM, 1);
					case 2:
						if (!st.hasQuestItems(NASTRON_HEART))
							st.giveItems(NASTRON_HEART, 1);
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
				case "31379-02.html":
					st.startQuest();
					htmltext = event;
					break;
				case "give_heart":
					if (st.hasQuestItems(NASTRON_HEART))
					{
						st.addExpAndSp(10000, 0);
						st.exitQuest(true, true);
						htmltext = "31379-06.html";
					}
					else
						htmltext = "31379-07.html";
					break;
				case "spawn_totem":
					htmltext = st.hasQuestItems(RED_TOTEM) ? spawnNastron(npc, st) : "31558-04.html";
					break;
			}
		}
		else if (event.equals("despawn_nastron"))
		{
			npc.broadcastNpcSay(Say2.NPC_ALL, "The power of constraint is getting weaker. Your ritual has failed!");
			npc.deleteMe();
			addSpawn(KETRA_TOTEM, 142368, -82512, -6487, 58000, false, 0, true);
		}
		else if (event.equals("spawn_npc"))
			addSpawn(KETRA_TOTEM, 142368, -82512, -6487, 58000, false, 0, true);
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int respawnMinDelay = (int) (43200000 * Config.RAID_MIN_RESPAWN_MULTIPLIER);
		final int respawnMaxDelay = (int) (129600000 * Config.RAID_MAX_RESPAWN_MULTIPLIER);
		final int respawnDelay = getRandom(respawnMinDelay, respawnMaxDelay);
		cancelQuestTimer("despawn_nastron", npc, null);
		saveGlobalQuestVar("Q00616_respawn", String.valueOf(System.currentTimeMillis() + respawnDelay));
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
			case UDAN:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = player.getLevel() >= MIN_LEVEL ? st.hasQuestItems(RED_TOTEM) ? "31379-01.htm" : "31379-00a.html" : "31379-00b.html";
						break;
					case State.STARTED:
						htmltext = st.isCond(1) ? "31379-03.html" : st.hasQuestItems(NASTRON_HEART) ? "31379-04.html" : "31379-05.html";
						break;
				}
				break;
			case KETRA_TOTEM:
				if (st.isStarted())
					switch (st.getCond())
					{
						case 1:
							htmltext = "31558-01.html";
							break;
						case 2:
							htmltext = spawnNastron(npc, st);
							break;
						case 3:
							htmltext = "31558-05.html";
							break;
					}
				break;
		}
		return htmltext;
	}

	private String spawnNastron(final L2Npc npc, final QuestState st)
	{
		if (getQuestTimer("spawn_npc", null, null) != null)
			return "31558-03.html";
		if (st.isCond(1))
		{
			st.takeItems(RED_TOTEM, 1);
			st.setCond(2, true);
		}
		npc.deleteMe();
		final L2Npc nastron = addSpawn(NASTRON, 142528, -82528, -6496, 0, false, 0);
		nastron.broadcastNpcSay(Say2.NPC_ALL, "The magical power of fire is also the power of flames and lava! If you dare to confront it, only death will await you!");
		startQuestTimer("despawn_nastron", 1200000, nastron, null);
		return "31558-02.html";
	}

	public static void main(final String[] args)
	{
		new Q00616_MagicalPowerOfFirePart2(616, Q00616_MagicalPowerOfFirePart2.class.getSimpleName(), "Magical Power of Fire - Part 2");
	}
}