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
package quests.Q00457_LostAndFound;

import java.util.Set;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.QuestState.QuestType;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;

/**
 * Lost and Found (457)
 * @author nonom
 */
public final class Q00457_LostAndFound extends Quest
{
	// NPCs
	private static final int GUMIEL = 32759;
	private static final int ESCORT_CHECKER = 32764;
	private static final int[] SOLINA_CLAN =
	{
		22789, // Guide Solina
		22790, // Seeker Solina
		22791, // Savior Solina
		22793, // Ascetic Solina
	};
	// Misc
	private static final int PACKAGED_BOOK = 15716;
	private static final int CHANCE_SPAWN = 1; // 1%
	private static final int MIN_LV = 82;
	private static Set<L2Spawn> _escortCheckers;

	private Q00457_LostAndFound(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		addStartNpc(GUMIEL);
		addFirstTalkId(GUMIEL);
		addTalkId(GUMIEL);
		addKillId(SOLINA_CLAN);

		_escortCheckers = SpawnTable.getInstance().getSpawns(ESCORT_CHECKER);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return getNoQuestMsg(player);

		String htmltext = null;
		switch (event)
		{
			case "32759-06.html":
			{
				npc.setScriptValue(0);
				st.startQuest();
				npc.setTarget(player);
				npc.setWalking();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
				startQuestTimer("CHECK", 1000, npc, player, true);
				startQuestTimer("TIME_LIMIT", 600000, npc, player);
				startQuestTimer("TALK_TIME", 120000, npc, player);
				startQuestTimer("TALK_TIME2", 30000, npc, player);
				break;
			}
			case "TALK_TIME":
			{
				broadcastNpcSay(npc, player, "Ah... I think I remember this place.", false);
				break;
			}
			case "TALK_TIME2":
			{
				broadcastNpcSay(npc, player, "What were you doing here?", false);
				startQuestTimer("TALK_TIME3", 10 * 1000, npc, player);
				break;
			}
			case "TALK_TIME3":
			{
				broadcastNpcSay(npc, player, "I guess you're the silent type. Then are you looking for treasure like me?", false);
				break;
			}
			case "TIME_LIMIT":
			{
				startQuestTimer("STOP", 2000, npc, player);
				st.exitQuest(QuestType.DAILY);
				break;
			}
			case "CHECK":
			{
				final double distance = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
				if (distance > 1000)
					if (distance > 5000)
					{
						startQuestTimer("STOP", 2000, npc, player);
						st.exitQuest(QuestType.DAILY);
					}
					else if (npc.isScriptValue(0))
					{
						broadcastNpcSay(npc, player, "Hey, don't go so fast.", true);
						npc.setScriptValue(1);
					}
					else if (npc.isScriptValue(1))
					{
						broadcastNpcSay(npc, player, "It's hard to follow.", true);
						npc.setScriptValue(2);
					}
					else if (npc.isScriptValue(2))
					{
						startQuestTimer("STOP", 2000, npc, player);
						st.exitQuest(QuestType.DAILY);
					}
				for (final L2Spawn escortSpawn : _escortCheckers)
				{
					final L2Npc escort = escortSpawn.getLastSpawn();
					if (escort != null && npc.isInsideRadius(escort, 1000, false, false))
					{
						startQuestTimer("STOP", 1000, npc, player);
						startQuestTimer("BYE", 3000, npc, player);
						cancelQuestTimer("CHECK", npc, player);
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), "Ah! Fresh air!"));
						broadcastNpcSay(npc, player, "Ah! Fresh air!", false);
						st.giveItems(PACKAGED_BOOK, 1);
						st.exitQuest(QuestType.DAILY, true);
						break;
					}
				}
				break;
			}
			case "STOP":
			{
				npc.setTarget(null);
				npc.getAI().stopFollow();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				cancelQuestTimer("CHECK", npc, player);
				cancelQuestTimer("TIME_LIMIT", npc, player);
				cancelQuestTimer("TALK_TIME", npc, player);
				cancelQuestTimer("TALK_TIME2", npc, player);
				break;
			}
			case "BYE":
			{
				npc.deleteMe();
				break;
			}
			default:
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (npc.getTarget() != null)
			return npc.getTarget().equals(player) ? "32759-08.html" : "32759-01a.html";
		return "32759.html";
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final QuestState st = getQuestState(player, true);

		if (getRandom(100) < CHANCE_SPAWN && st.isNowAvailable() && player.getLevel() >= MIN_LV)
			addSpawn(GUMIEL, npc);
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getLevel() >= MIN_LV ? "32759-01.htm" : "32759-03.html";
				break;
			}
			case State.COMPLETED:
			{
				if (st.isNowAvailable())
				{
					st.setState(State.CREATED);
					htmltext = player.getLevel() >= MIN_LV ? "32759-01.htm" : "32759-03.html";
				}
				else
					htmltext = "32759-02.html";
				break;
			}
		}
		return htmltext;
	}

	public void broadcastNpcSay(final L2Npc npc, final L2PcInstance player, final String stringId, final boolean whisper)
	{
		(whisper ? player : npc).sendPacket(new NpcSay(npc.getObjectId(), whisper ? Say2.TELL : Say2.ALL, npc.getId(), stringId));
	}

	public static void main(final String[] args)
	{
		new Q00457_LostAndFound(457, Q00457_LostAndFound.class.getSimpleName(), "Lost and Found");
	}
}
