/*
 * Copyright (C) 2004-2014 L2J DataPack
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
package ai.individual.npc.Ranga;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.holders.MinionHolder;

/**
 * Ragna Orc Seer AI.
 * @author Zealar
 */
public final class RagnaOrcSeer extends L2AttackableAIScript
{
	private static final int RAGNA_ORC_SEER = 22697;

	private RagnaOrcSeer(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addSpawnId(RAGNA_ORC_SEER);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{

		if (getRandom(100) < 50)
			spawnMinions(npc, "Privates1");
		else
			spawnMinions(npc, "Privates2");
		return super.onSpawn(npc);
	}

	private void spawnMinions(final L2Npc npc, final String spawnName)
	{
		for (final MinionHolder is : npc.getTemplate().getParameters().getMinionList(spawnName))
			addMinion((L2MonsterInstance) npc, is.getId());
	}

	public static void main(final String[] args)
	{
		new RagnaOrcSeer(-1, RagnaOrcSeer.class.getSimpleName(), "ai/individual");
	}
}