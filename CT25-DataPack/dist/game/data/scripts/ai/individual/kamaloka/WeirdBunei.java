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
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * Originally written by RosT in python
 * @author Evilus
 */
public class WeirdBunei extends L2AttackableAIScript
{
	// Npc
	private static final int WEIRD = 18564;
	
	// Constant
	private boolean _isAlreadyStarted = false;
	
	public WeirdBunei(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(WEIRD);
		addKillId(WEIRD);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
			if (_isAlreadyStarted == true)
			{
				_isAlreadyStarted = false;
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(5625, 1));
			}
			else
				return null;
			
		return null;
	}
	
	@Override
	public final String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet, final L2Skill skill)
	{
		final int npcId = npc.getId();
		if (npcId == WEIRD)
			if (_isAlreadyStarted == false)
			{
				startQuestTimer("time_to_skill", 30000, npc, player);
				_isAlreadyStarted = true;
			}
			else if (_isAlreadyStarted == true)
				return null;
		return null;
	}
	
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (npcId == WEIRD)
			cancelQuestTimer("time_to_skill", npc, player);
		return null;
	}
	
	public static void main(final String[] args)
	{
		new WeirdBunei(-1, WeirdBunei.class.getSimpleName(), "ai/individual/kamaloka");
	}
}
