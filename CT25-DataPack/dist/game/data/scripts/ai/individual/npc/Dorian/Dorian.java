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
package ai.individual.npc.Dorian;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import quests.Q00024_InhabitantsOfTheForestOfTheDead.Q00024_InhabitantsOfTheForestOfTheDead;

/**
 * Dorian (Raid Fighter) - Quest AI
 * @author malyelfik
 */
public final class Dorian extends L2AttackableAIScript
{
	// NPC
	private static final int DORIAN = 25332;

	// Items
	private static final int SILVER_CROSS = 7153;
	private static final int BROKEN_SILVER_CROSS = 7154;

	private Dorian()
	{
		super(-1, Dorian.class.getSimpleName(), "ai/individual/npc");
		addSpawnId(DORIAN);

		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(DORIAN))
			startQuestTimer("checkArea", 3000, spawn.getLastSpawn(), null, true);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equals("checkArea"))
			if (npc.isDecayed())
				cancelQuestTimers("checkArea");
			else
				for (final L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(300))
				{
					final QuestState qs = pl.getQuestState(Q00024_InhabitantsOfTheForestOfTheDead.class.getSimpleName());
					if (qs != null && qs.isCond(3))
					{
						qs.takeItems(SILVER_CROSS, -1);
						qs.giveItems(BROKEN_SILVER_CROSS, 1);
						qs.setCond(4, true);
						broadcastNpcSay(npc, Say2.ALL, 2450); // That Sign!
					}
				}
		return null;
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		startQuestTimer("checkArea", 3000, npc, null, true);
		return null;
	}

	public static void main(final String[] args)
	{
		new Dorian();
	}
}