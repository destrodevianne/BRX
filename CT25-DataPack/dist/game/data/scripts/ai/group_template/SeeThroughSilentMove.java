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
package ai.group_template;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;

public class SeeThroughSilentMove extends L2AttackableAIScript
{
	private static final int[] MOBIDS =
	{
		18001,
		18002,
		22199,
		22215,
		22216,
		22217,
		22327,
		22746,
		22747,
		22748,
		22749,
		22750,
		22751,
		22752,
		22753,
		22754,
		22755,
		22756,
		22757,
		22758,
		22759,
		22760,
		22761,
		22762,
		22763,
		22764,
		22765,
		22794,
		22795,
		22796,
		22797,
		22798,
		22799,
		22800,
		29009,
		29010,
		29011,
		29012,
		29013
	};

	public SeeThroughSilentMove(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int npcId : MOBIDS)
			for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(npcId))
			{
				final L2Npc npc = spawn.getLastSpawn();
				if (npc != null && npc.isL2Attackable())
					((L2Attackable) npc).setSeeThroughSilentMove(true);
			}
		registerMobs(MOBIDS, QuestEventType.ON_SPAWN);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		if (npc instanceof L2Attackable)
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		return super.onSpawn(npc);
	}

	public static void main(final String[] args)
	{
		new SeeThroughSilentMove(-1, SeeThroughSilentMove.class.getSimpleName(), "ai/group_template");
	}
}