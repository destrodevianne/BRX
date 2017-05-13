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
package ai.individual.monster;

import java.util.Collection;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.GeoData;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2DecoyInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.QuestTimer;
import ct25.xtreme.gameserver.util.Util;
import javolution.util.FastList;

/**
 * @author Kazumi
 */

public class MutationDrake extends L2AttackableAIScript
{
	// Npc
	private static final int MUTATION_DRAKE = 22552;
	
	public MutationDrake(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		final int[] mob =
		{
			MUTATION_DRAKE
		};
		this.registerMobs(mob, QuestEventType.ON_ATTACK);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public final String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.getId() == MUTATION_DRAKE)
			if (npc.getCurrentHp() > npc.getMaxHp() * 3 / 4)
			{
				if (getRandom(100) < 75)
					getRandomTarget(npc);
			}
			else if (npc.getCurrentHp() > npc.getMaxHp() * 2 / 4)
			{
				if (getRandom(100) < 50)
					getRandomTarget(npc);
			}
			else if (npc.getCurrentHp() > npc.getMaxHp() * 1 / 4)
				if (getRandom(100) < 25)
					getRandomTarget(npc);
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public L2Character getRandomTarget(final L2Npc npc)
	{
		final FastList<L2Character> result = new FastList<>();
		final Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		{
			for (final L2Object obj : objs)
			{
				if (obj instanceof L2Character)
					if (((L2Character) obj).getZ() < npc.getZ() - 100 && ((L2Character) obj).getZ() > npc.getZ() + 100 || !GeoData.getInstance().canSeeTarget(((L2Character) obj).getX(), ((L2Character) obj).getY(), ((L2Character) obj).getZ(), npc.getX(), npc.getY(), npc.getZ())
						|| ((L2Character) obj).isGM())
						continue;
				if (obj instanceof L2PcInstance || obj instanceof L2Summon || obj instanceof L2DecoyInstance)
					if (Util.checkIfInRange(1000, npc, obj, true) && !((L2Character) obj).isDead())
						result.add((L2Character) obj);
			}
		}
		if (!result.isEmpty())
		{
			final QuestTimer timer = getQuestTimer("new_aggro", npc, null);
			if (timer != null)
				timer.cancel();
			startQuestTimer("new_aggro", 10000, npc, null);
			return result.get(getRandom(result.size()));
		}
		return null;
	}
	
	public static void main(final String[] args)
	{
		new MutationDrake(-1, MutationDrake.class.getSimpleName(), "ai/individual/monster");
	}
}