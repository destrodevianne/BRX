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

/**
 * @author Forsaiken
 */
package ct25.xtreme.gameserver.skills.effects;

import javolution.util.FastList;

import ct25.xtreme.gameserver.ai.CtrlEvent;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.idfactory.IdFactory;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2EffectPointInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillLaunched;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.skills.Env;
import ct25.xtreme.gameserver.skills.Formulas;
import ct25.xtreme.gameserver.skills.l2skills.L2SkillSignetCasttime;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct25.xtreme.gameserver.templates.effects.EffectTemplate;
import ct25.xtreme.gameserver.templates.skills.L2EffectType;
import ct25.xtreme.util.Point3D;

public class EffectSignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;
	
	public EffectSignetMDam(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		L2NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
			template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		else
			return false;
		
		L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		//L2World.getInstance().storeObject(effectPoint);
		
		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();
		
		if (getEffector() instanceof L2PcInstance
				&& getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
		{
			Point3D wordPosition = ((L2PcInstance) getEffector()).getCurrentSkillWorldPosition();
			
			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);
		
		_actor = effectPoint;
		return true;
		
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getCount() >= getTotalCount() - 2)
			return true; // do nothing first 2 times
		int mpConsume = getSkill().getMpConsume();
		
		L2PcInstance caster = (L2PcInstance) getEffector();
		
		boolean ss = false;
		boolean bss = false;
		
		L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			switch (weaponInst.getChargedSpiritshot())
			{
				case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					bss = true;
					break;
				case L2ItemInstance.CHARGED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					ss = true;
					break;
			}
		}
		
		FastList<L2Character> targets = new FastList<L2Character>();
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null || cha == caster)
				continue;
			
			if (cha instanceof L2Attackable || cha instanceof L2Playable)
			{
				if (cha.isAlikeDead())
					continue;
				
				if (mpConsume > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					return false;
				}
				else
					caster.reduceCurrentMp(mpConsume);
				
				if (cha instanceof L2Playable)
				{
					if (caster.canAttackCharacter(cha))
					{
						targets.add(cha);
						caster.updatePvPStatus(cha);
					}
				}
				else
					targets.add(cha);
			}
		}
		
		if (!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
			for (L2Character target : targets)
			{
				boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				byte shld = Formulas.calcShldUse(caster, target, getSkill());
				int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), shld, ss, bss, mcrit);
				
				if (target instanceof L2Summon)
					target.broadcastStatusUpdate();
				
				if (mdam > 0)
				{
					if (!target.isRaid()
							&& Formulas.calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		if (_actor != null)
			_actor.deleteMe();
	}
}
