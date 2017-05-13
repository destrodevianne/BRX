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
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;

/**
 * Originally written by RosT in python
 * @author Evilus
 */
public class HatuOtis extends L2AttackableAIScript
{
	// Npc
	private static final int OTIS = 18558;
	
	// Constant skills
	boolean _isAlreadyUsedSkill = false;
	boolean _isAlreadyUsedSkill1 = false;
	
	public HatuOtis(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(OTIS);
		addKillId(OTIS);
		
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
			if (_isAlreadyUsedSkill == true)
			{
				npc.setTarget(npc);
				npc.doCast(SkillTable.getInstance().getInfo(4737, 3));
				_isAlreadyUsedSkill = false;
			}
		return null;
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int objId = npc.getObjectId();
		final int maxHp = npc.getMaxHp();
		final double nowHp = npc.getStatus().getCurrentHp();
		
		if (npcId == OTIS)
		{
			if (nowHp < maxHp * 0.3)
				if (_isAlreadyUsedSkill1 == false)
				{
					npc.broadcastPacket(new CreatureSay(objId, 0, npc.getName(), "I will be with you, and to take care of you !"));
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(4175, 3));
					_isAlreadyUsedSkill1 = true;
				}
			if (_isAlreadyUsedSkill == false)
			{
				startQuestTimer("time_to_skill", 30000, npc, player);
				_isAlreadyUsedSkill = true;
			}
		}
		return null;
		
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final int npcId = npc.getId();
		
		if (npcId == OTIS)
			cancelQuestTimer("time_to_skill", npc, player);
		return null;
	}
	
	public static void main(final String[] args)
	{
		new HatuOtis(-1, HatuOtis.class.getSimpleName(), "ai/individual/kamaloka");
	}
}
