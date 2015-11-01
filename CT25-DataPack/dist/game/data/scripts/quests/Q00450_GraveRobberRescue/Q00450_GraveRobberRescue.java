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
package quests.Q00450_GraveRobberRescue;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.enums.QuestType;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;

/**
 * Grave Robber Rescue (450)
 * @author malyelfik
 */
public class Q00450_GraveRobberRescue extends Quest
{
	// NPCs
	private static final int KANEMIKA = 32650;
	private static final int WARRIOR = 32651;
	// Monster
	private static final int WARRIOR_MON = 22741;
	// Item
	private static final int EVIDENCE_OF_MIGRATION = 14876;
	// Misc
	private static final int MIN_LEVEL = 80;
	
	public Q00450_GraveRobberRescue(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(KANEMIKA);
		addTalkId(KANEMIKA, WARRIOR);
		registerQuestItems(EVIDENCE_OF_MIGRATION);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "32650-04.htm":
			case "32650-05.htm":
			case "32650-06.html":
				break;
			case "32650-07.htm":
				st.startQuest();
				break;
			case "despawn":
				npc.setBusy(false);
				npc.deleteMe();
				htmltext = null;
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == KANEMIKA)
		{
			switch (st.getState())
			{
				case State.COMPLETED:
					if (!st.isNowAvailable())
					{
						htmltext = "32650-03.html";
						break;
					}
					st.setState(State.CREATED);
				case State.CREATED:
					htmltext = (player.getLevel() >= MIN_LEVEL) ? "32650-01.htm" : "32650-02.htm";
					break;
				case State.STARTED:
					if (st.isCond(1))
					{
						htmltext = (!st.hasQuestItems(EVIDENCE_OF_MIGRATION)) ? "32650-08.html" : "32650-09.html";
					}
					else
					{
						st.giveAdena(65000, true); // Glory days reward: 6 886 980 exp, 8 116 410 sp, 371 400 Adena
						st.exitQuest(QuestType.DAILY, true);
						htmltext = "32650-10.html";
					}
					break;
			}
		}
		else if (st.isCond(1))
		{
			if (npc.isBusy())
			{
				return null;
			}
			
			if (getRandom(100) < 66)
			{
				st.giveItems(EVIDENCE_OF_MIGRATION, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0));
				npc.setBusy(true);
				
				startQuestTimer("despawn", 3000, npc, player);
				
				if (st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) == 10)
				{
					st.setCond(2, true);
				}
				htmltext = "32651-01.html";
			}
			else
			{
				if (getRandom(100) < 50)
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), "...Grunt... Oh..."));
				}
				else
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), "Grunt... What's... wrong with me..."));
				}
				npc.deleteMe();
				htmltext = null;
				
				final L2Attackable monster = (L2Attackable) addSpawn(WARRIOR_MON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 600000);
				monster.setRunning();
				monster.addDamageHate(player, 0, 999);
				monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				showOnScreenMsg(player, "The grave robber warrior has been filled with dark energy and is attacking you!", 3000);
			}
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00450_GraveRobberRescue(450, Q00450_GraveRobberRescue.class.getSimpleName(), "Grave Robber Rescue");
	}
}