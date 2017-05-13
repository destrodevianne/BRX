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
package hellbound.Megaliths;

import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.model.quest.Quest;

public class HellboundCore extends Quest
{
	// Npcs
	private static final int NAIA = 18484;
	private static final int HELLBOUND_CORE = 32331;

	// Skill
	private static SkillHolder BEAM = new SkillHolder(5493, 1);
	
	public HellboundCore(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		addSpawnId(HELLBOUND_CORE);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && HellboundManager.getInstance().getLevel() <= 6)
		{
			for (final L2Character naia : npc.getKnownList().getKnownCharactersInRadius(900))
				if (naia != null && naia instanceof L2MonsterInstance && ((L2MonsterInstance) naia).getId() == NAIA && !naia.isDead())
				{
					naia.setTarget(npc);
					naia.doSimultaneousCast(BEAM.getSkill());
				}

			startQuestTimer("cast", 10000, npc, null);
		}
		
		return null;
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		startQuestTimer("cast", 10000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(final String[] args)
	{
		new HellboundCore(-1, HellboundCore.class.getSimpleName(), "hellbound");
	}
}
