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
package ai.individual.kamaloka;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author InsOmnia
 */
public class FollowerOfAllosce extends L2AttackableAIScript
{
	// Npcs
	private static final int FOFALLOSCE = 18578;
	
	public FollowerOfAllosce(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAggroRangeEnterId(FOFALLOSCE);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5624, 1));
			this.startQuestTimer("time_to_skill", 30000, npc, player);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (npc.getId() == FOFALLOSCE)
		{
			npc.setIsInvul(true);
			this.startQuestTimer("time_to_skill", 30000, npc, player);
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5624, 1));
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public static void main(final String[] args)
	{
		new FollowerOfAllosce(-1, FollowerOfAllosce.class.getSimpleName(), "ai/individual/kamaloka");
	}
}