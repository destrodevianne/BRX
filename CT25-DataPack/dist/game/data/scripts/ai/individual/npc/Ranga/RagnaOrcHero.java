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
 * Ragna Orc Hero AI.
 * @author Zealar
 */
public final class RagnaOrcHero extends L2AttackableAIScript
{
	private static final int RAGNA_ORC_HERO = 22693;

	private RagnaOrcHero(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addSpawnId(RAGNA_ORC_HERO);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		if (getRandom(100) < 70)
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
		new RagnaOrcHero(-1, RagnaOrcHero.class.getSimpleName(), "ai/individual");
	}
}