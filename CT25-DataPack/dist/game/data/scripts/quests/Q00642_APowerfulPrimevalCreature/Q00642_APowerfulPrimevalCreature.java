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
package quests.Q00642_APowerfulPrimevalCreature;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * A Powerful Primeval Creature (642)
 * @author Adry_85
 */
public class Q00642_APowerfulPrimevalCreature extends Quest
{
	// NPC
	private static final int DINN = 32105;
	// Items
	private static final int DINOSAUR_TISSUE = 8774;
	private static final int DINOSAUR_EGG = 8775;

	private static final Map<Integer, Integer> MOBS_TISSUE = new HashMap<>();

	static
	{
		MOBS_TISSUE.put(22196, 309); // Velociraptor
		MOBS_TISSUE.put(22197, 309); // Velociraptor
		MOBS_TISSUE.put(22198, 309); // Velociraptor
		MOBS_TISSUE.put(22199, 309); // Pterosaur
		MOBS_TISSUE.put(22215, 988); // Tyrannosaurus
		MOBS_TISSUE.put(22216, 988); // Tyrannosaurus
		MOBS_TISSUE.put(22217, 988); // Tyrannosaurus
		MOBS_TISSUE.put(22218, 309); // Velociraptor
		MOBS_TISSUE.put(22223, 309); // Velociraptor
	}

	private static final int ANCIENT_EGG = 18344;

	public Q00642_APowerfulPrimevalCreature(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(DINN);
		addTalkId(DINN);
		addKillId(ANCIENT_EGG);
		for (final int id : MOBS_TISSUE.keySet())
			super.addKillId(id);
		registerQuestItems(DINOSAUR_TISSUE, DINOSAUR_EGG);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return getNoQuestMsg(player);

		String htmltext = event;
		switch (event)
		{
			case "32105-05.html":
				st.startQuest();
				break;
			case "32105-06.htm":
				st.exitQuest(true);
				break;
			case "32105-09.html":
				if (st.hasQuestItems(DINOSAUR_TISSUE))
				{
					st.giveAdena(5000 * st.getQuestItemsCount(DINOSAUR_TISSUE), true);
					st.takeItems(DINOSAUR_TISSUE, -1);
				}
				break;
			case "exit":
				if (st.hasQuestItems(DINOSAUR_TISSUE))
				{
					st.giveAdena(5000 * st.getQuestItemsCount(DINOSAUR_TISSUE), true);
					st.exitQuest(true, true);
					htmltext = "32105-12.html";
				}
				else
				{
					st.exitQuest(true, true);
					htmltext = "32105-13.html";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return super.onKill(npc, player, isPet);

		final QuestState st = partyMember.getQuestState(getName());
		final int npcId = npc.getId();
		if (MOBS_TISSUE.containsKey(npcId))
		{
			final float chance = MOBS_TISSUE.get(npcId) * Config.RATE_QUEST_DROP;
			if (getRandom(1000) < chance)
			{
				st.rewardItems(DINOSAUR_TISSUE, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		else if (npcId == ANCIENT_EGG)
		{
			st.rewardItems(DINOSAUR_EGG, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
				htmltext = player.getLevel() < 75 ? "32105-01.htm" : "32105-02.htm";
				break;
			case State.STARTED:
				htmltext = !st.hasQuestItems(DINOSAUR_TISSUE) && !st.hasQuestItems(DINOSAUR_EGG) ? "32105-07.html" : "32105-08.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00642_APowerfulPrimevalCreature(642, Q00642_APowerfulPrimevalCreature.class.getSimpleName(), "A Powerful Primeval Creature");
	}
}
