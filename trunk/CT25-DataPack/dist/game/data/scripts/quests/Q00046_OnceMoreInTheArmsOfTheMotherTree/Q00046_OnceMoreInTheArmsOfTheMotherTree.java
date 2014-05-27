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
package quests.Q00046_OnceMoreInTheArmsOfTheMotherTree;

import java.util.HashMap;
import java.util.Map;

import quests.Q00007_ATripBegins.Q00007_ATripBegins;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.ItemHolder;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Once More In The Arms Of The Mother Tree (46)
 * @author janiko
 */
public final class Q00046_OnceMoreInTheArmsOfTheMotherTree extends Quest
{
	// Npcs
	private static final int GALLADUCCI = 30097;
	private static final int GENTLER = 30094;
	private static final int SANDRA = 30090;
	private static final int DUSTIN = 30116;
	// Items
	private static final int MARK_OF_TRAVELER = 7570;
	private static final int GALLADUCCIS_ORDER_1 = 7563;
	private static final int GALLADUCCIS_ORDER_2 = 7564;
	private static final int GALLADUCCIS_ORDER_3 = 7565;
	private static final int PURIFIED_MAGIC_NECKLACE = 7566;
	private static final int GEMSTONE_POWDER = 7567;
	private static final int MAGIC_SWORD_HILT = 7568;
	// Misc
	private static final int MIN_LVL = 3;
	// Reward
	private static final int SCROLL_OF_ESCAPE_ELVEN_VILLAGE = 7555;
	// Get condition for each npc
	private static Map<Integer, ItemHolder> NPC_ITEMS = new HashMap<>();
	static
	{
		NPC_ITEMS.put(GENTLER, new ItemHolder(1, GALLADUCCIS_ORDER_1));
		NPC_ITEMS.put(SANDRA, new ItemHolder(3, GALLADUCCIS_ORDER_2));
		NPC_ITEMS.put(DUSTIN, new ItemHolder(5, GALLADUCCIS_ORDER_3));
	}
	
	private Q00046_OnceMoreInTheArmsOfTheMotherTree(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(GALLADUCCI);
		addTalkId(GALLADUCCI);
		for (int id : NPC_ITEMS.keySet()) super.addTalkId(id);
		registerQuestItems(GALLADUCCIS_ORDER_1, GALLADUCCIS_ORDER_2, GALLADUCCIS_ORDER_3, PURIFIED_MAGIC_NECKLACE, GEMSTONE_POWDER, MAGIC_SWORD_HILT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st == null)
		{
			return htmltext;
		}
		switch (event)
		{
			case "30097-04.htm":
			{
				if (st.isCreated())
				{
					st.startQuest();
					st.giveItems(GALLADUCCIS_ORDER_1, 1);
					htmltext = event;
				}
				break;
			}
			case "30094-02.html":
			{
				if (st.isCond(1) && st.hasQuestItems(GALLADUCCIS_ORDER_1))
				{
					st.takeItems(GALLADUCCIS_ORDER_1, 1);
					st.giveItems(MAGIC_SWORD_HILT, 1);
					st.setCond(2, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30094-03.html";
				}
				break;
			}
			case "30097-07.html":
			{
				if (st.isCond(2) && st.hasQuestItems(MAGIC_SWORD_HILT))
				{
					st.takeItems(MAGIC_SWORD_HILT, 1);
					st.giveItems(GALLADUCCIS_ORDER_2, 1);
					st.setCond(3, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30097-08.html";
				}
				break;
			}
			case "30090-02.html":
			{
				if (st.isCond(3) && st.hasQuestItems(GALLADUCCIS_ORDER_2))
				{
					st.takeItems(GALLADUCCIS_ORDER_2, 1);
					st.giveItems(GEMSTONE_POWDER, 1);
					st.setCond(4, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30090-03.html";
				}
				break;
			}
			case "30097-11.html":
			{
				if (st.isCond(4) && st.hasQuestItems(GEMSTONE_POWDER))
				{
					st.takeItems(GEMSTONE_POWDER, 1);
					st.giveItems(GALLADUCCIS_ORDER_3, 1);
					st.setCond(5, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30097-12.html";
				}
				break;
			}
			case "30116-02.html":
			{
				if (st.isCond(5) && st.hasQuestItems(GALLADUCCIS_ORDER_3))
				{
					st.takeItems(GALLADUCCIS_ORDER_3, 1);
					st.giveItems(PURIFIED_MAGIC_NECKLACE, 1);
					st.setCond(6, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30116-03.html";
				}
				break;
			}
			case "30097-15.html":
			{
				if (st.isCond(6) && st.hasQuestItems(PURIFIED_MAGIC_NECKLACE))
				{
					st.giveItems(SCROLL_OF_ESCAPE_ELVEN_VILLAGE, 1);
					st.exitQuest(false, true);
					htmltext = event;
				}
				else
				{
					htmltext = "30097-16.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		QuestState st = talker.getQuestState(getName());
		String htmltext = getNoQuestMsg(talker);
		if (st == null)
		{
			return htmltext;
		}
		switch (npc.getId())
		{
			case GALLADUCCI:
			{
				switch (st.getState())
				{
					case State.CREATED:
					{
						if (talker.getLevel() < MIN_LVL)
						{
							htmltext = "30097-03.html";
						}
						else
						{
							st = talker.getQuestState(Q00007_ATripBegins.class.getSimpleName());
							if ((st != null) && st.isCompleted() && st.hasQuestItems(MARK_OF_TRAVELER))
							{
								htmltext = "30097-01.htm";
							}
							else
							{
								htmltext = "30097-02.html";
							}
						}
						break;
					}
					case State.STARTED:
					{
						switch (st.getCond())
						{
							case 1:
							{
								htmltext = "30097-05.html";
								break;
							}
							case 2:
							{
								if (st.hasQuestItems(MAGIC_SWORD_HILT))
								{
									htmltext = "30097-06.html";
								}
								break;
							}
							case 3:
							{
								htmltext = "30097-09.html";
								break;
							}
							case 4:
							{
								if (st.hasQuestItems(GEMSTONE_POWDER))
								{
									htmltext = "30097-10.html";
								}
								break;
							}
							case 5:
							{
								htmltext = "30097-13.html";
								break;
							}
							case 6:
							{
								if (st.hasQuestItems(PURIFIED_MAGIC_NECKLACE))
								{
									htmltext = "30097-14.html";
								}
								break;
							}
						}
						break;
					}
					case State.COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg(talker);
						break;
					}
				}
				break;
			}
			case GENTLER:
			case SANDRA:
			case DUSTIN:
			{
				if (st.isStarted())
				{
					final ItemHolder i = NPC_ITEMS.get(npc.getId());
					final int cond = i.getId();
					if (st.isCond(cond))
					{
						final int itemId = (int) i.getCount();
						if (st.hasQuestItems(itemId))
						{
							htmltext = npc.getId() + "-01.html";
						}
					}
					else if (st.isCond(cond + 1))
					{
						htmltext = npc.getId() + "-04.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00046_OnceMoreInTheArmsOfTheMotherTree(46, Q00046_OnceMoreInTheArmsOfTheMotherTree.class.getSimpleName(), "Once More In the Arms of the Mother Tree");
	}
}