/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q457_LostAndFound;

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
import ct25.xtreme.util.Rnd;

/**
 * Lost and Found (457)
 * @author nonom
 */
public final class Q457_LostAndFound extends Quest
{
	private static final int GUMIEL = 32759;
	private static final int ESCORT_CHECKER = 32764;
	private static final int[] SOLINA_CLAN =
	{
		22789, // Guide Solina
		22790, // Seeker Solina
		22791, // Savior Solina
		22793, // Ascetic Solina
	};
	
	private static final int PACKAGED_BOOK = 15716;
	
	private static final int CHANCE_SPAWN = 1; // 1%
	
	private static int _count = 0;
	private static Set<L2Spawn> _escortCheckers;
	private static L2Npc _gumiel = null;
	
	private Q457_LostAndFound(int id, String name, String descr)
	{
		super(id, name, descr);
		addStartNpc(GUMIEL);
		addFirstTalkId(GUMIEL);
		addTalkId(GUMIEL);
		addSpawnId(GUMIEL);
		for (int i : SOLINA_CLAN)
		{
			addKillId(i);
		}		
		
		_escortCheckers = SpawnTable.getInstance().getSpawns(ESCORT_CHECKER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32759-06.html":
				_count = 0;
				st.startQuest();
				npc.setTarget(player);
				npc.setWalking();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
				startQuestTimer("check", 1000, npc, player, true);
				startQuestTimer("time_limit", 600000, npc, player);
				startQuestTimer("talk_time", 120000, npc, player);
				startQuestTimer("talk_time2", 30000, npc, player);
				break;
			case "talk_time":
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), "Ah... I think I remember this place."));
				break;
			case "talk_time2":
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), "What were you doing here?"));
				startQuestTimer("talk_time3", 10 * 1000, npc, player);
				break;
			case "talk_time3":
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), "I guess you're the silent type. Then are you looking for treasure like me?"));
				break;
			case "time_limit":
				startQuestTimer("stop", 2000, npc, player);
				st.exitQuest(QuestType.DAILY);
				break;
			case "check":
				final double distance = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
				if (distance > 1000)
				{
					if (distance > 5000)
					{
						startQuestTimer("stop", 2000, npc, player);
						st.exitQuest(QuestType.DAILY);
					}
					else if (_count == 0)
					{
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), "Hey, don't go so fast."));
						_count = 1;
					}
					else if (_count == 1)
					{
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), "It's hard to follow."));
						_count = 2;
					}
					else if (_count == 2)
					{
						startQuestTimer("stop", 2000, npc, player);
						st.exitQuest(QuestType.DAILY);
					}
				}
				for (L2Spawn escortSpawn : _escortCheckers)
				{
					final L2Npc escort = escortSpawn.getLastSpawn();
					if ((escort != null) && npc.isInsideRadius(escort, 1000, false, false))
					{
						startQuestTimer("stop", 1000, npc, player);
						startQuestTimer("bye", 3000, npc, player);
						cancelQuestTimer("check", npc, player);
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.NPC_ALL, npc.getName(), "Ah! Fresh air!"));
						st.giveItems(PACKAGED_BOOK, 1);
						st.exitQuest(QuestType.DAILY, true);
						break;
					}
				}
				break;
			case "stop":
				npc.setTarget(null);
				npc.getAI().stopFollow();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				cancelQuestTimer("check", npc, player);
				cancelQuestTimer("time_limit", npc, player);
				cancelQuestTimer("talk_time", npc, player);
				cancelQuestTimer("talk_time2", npc, player);
				_gumiel = null;
				break;
			case "bye":
				npc.deleteMe();
				break;
			default:
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getTarget() != null)
		{
			if (npc.getTarget().equals(player))
			{
				return "32759-08.html";
			}
			else if (_gumiel != null)
			{
				return "32759-01a.html";
			}
		}
		return "32759.html";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if ((_gumiel == null) && (Rnd.get(100) < CHANCE_SPAWN))
		{
			addSpawn(GUMIEL, npc);
		}
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		_gumiel = npc;
		_gumiel.getSpawn().stopRespawn();
		return super.onSpawn(npc);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() > 81) ? "32759-01.htm" : "32759-03.html";
				break;
			case State.COMPLETED:
				if (st.isNowAvailable())
				{
					st.setState(State.CREATED);
					htmltext = (player.getLevel() > 81) ? "32759-01.htm" : "32759-03.html";
				}
				else
				{
					htmltext = "32759-02.html";
				}
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q457_LostAndFound(457, Q457_LostAndFound.class.getSimpleName(), "Lost and Found");
	}
}
