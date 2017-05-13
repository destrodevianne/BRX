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
package hellbound.Desert;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.instancemanager.RaidBossSpawnManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2RaidBossInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;

public class Typhoon extends L2AttackableAIScript
{
	// Npc
	private static final int TYPHOON = 25539;

	// Skill
	private static SkillHolder STORM = new SkillHolder(5434, 1);
	
	public Typhoon(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		addAggroRangeEnterId(TYPHOON);
		addSpawnId(TYPHOON);

		final L2RaidBossInstance boss = RaidBossSpawnManager.getInstance().getBosses().get(TYPHOON);
		
		if (HellboundManager.getInstance().getLevel() > 3 && boss != null)
			onSpawn(boss);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && npc != null && !npc.isDead())
		{
			npc.doSimultaneousCast(STORM.getSkill());
			startQuestTimer("cast", 10000, npc, null);
		}
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		npc.doSimultaneousCast(STORM.getSkill());
		
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (!npc.isTeleporting())
			startQuestTimer("cast", 5000, npc, null);
		
		return super.onSpawn(npc);
	}
	
	public static void main(final String[] args)
	{
		new Typhoon(-1, Typhoon.class.getSimpleName(), "hellbound");
	}
}
