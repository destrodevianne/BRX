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
package ct25.xtreme.gameserver.skills.l2skills;

import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.idfactory.IdFactory;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Trap;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2TrapInstance;
import ct25.xtreme.gameserver.templates.StatsSet;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;

public class L2SkillTrap extends L2SkillSummon
{
	private int _triggerSkillId = 0;
	private int _triggerSkillLvl = 0;
	private int _trapNpcId = 0;
	protected L2Spawn _trapSpawn;
	
	/**
	 * 
	 * @param set
	 */
	public L2SkillTrap(StatsSet set)
	{
		super(set);
		_triggerSkillId = set.getInt("triggerSkillId");
		_triggerSkillLvl = set.getInt("triggerSkillLvl");
		_trapNpcId = set.getInt("trapNpcId");
	}
	
	public int getTriggerSkillId()
	{
		return _triggerSkillId;
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Skill#useSkill(ct25.xtreme.gameserver.model.actor.L2Character, ct25.xtreme.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
			return;
		
		if (_trapNpcId == 0)
			return;
		
		L2PcInstance activeChar = (L2PcInstance) caster;
		
		if (activeChar.inObserverMode())
			return;
		
		if (activeChar.isMounted())
			return;
		
		if (_triggerSkillId == 0 || _triggerSkillLvl == 0)
			return;
		
		L2Trap trap = activeChar.getTrap();
		if (trap != null)
			trap.unSummon();
		
		L2Skill skill = SkillTable.getInstance().getInfo(_triggerSkillId, _triggerSkillLvl);
		
		if (skill == null)
			return;
		
		L2NpcTemplate TrapTemplate = NpcTable.getInstance().getTemplate(_trapNpcId);
		trap = new L2TrapInstance(IdFactory.getInstance().getNextId(), TrapTemplate, activeChar, getTotalLifeTime(), skill);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(activeChar.getHeading());
		activeChar.setTrap(trap);
		//L2World.getInstance().storeObject(trap);
		trap.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
}
