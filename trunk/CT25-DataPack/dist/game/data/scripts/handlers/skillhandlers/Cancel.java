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
package handlers.skillhandlers;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.ISkillHandler;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.skills.Formulas;
import ct25.xtreme.gameserver.templates.skills.L2SkillType;
import ct25.xtreme.util.Rnd;
import ct25.xtreme.util.StringUtil;

/**
 * @author DS
 */
public class Cancel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CANCEL,
	};

	/**
	 * @see ct25.xtreme.gameserver.handler.ISkillHandler#useSkill(ct25.xtreme.gameserver.model.actor.L2Character, ct25.xtreme.gameserver.model.L2Skill, ct25.xtreme.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (skill.isMagic())
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
		}
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;

			if (skill.isMagic())
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}
		else if (activeChar instanceof L2Npc)
		{
			((L2Npc) activeChar)._soulshotcharged = false;
			((L2Npc) activeChar)._spiritshotcharged = false;
		}

		L2Character target;
		L2Effect effect;
		final int cancelLvl, minRate, maxRate;

		cancelLvl = skill.getMagicLevel();
		minRate = 25;
		maxRate = 80;
		
		for (final L2Object obj : targets)
		{
			if (!(obj instanceof L2Character))
				continue;
			target = (L2Character) obj;

			if (target.isDead())
				continue;

			int lastCanceledSkillId = 0;
			int count = skill.getMaxNegatedEffects();
			double rate = skill.getPower();
			final double vulnModifier = Formulas.calcSkillTypeVulnerability(0, target, skill.getSkillType());
			final double profModifier = Formulas.calcSkillTypeProficiency(0, activeChar, target, skill.getSkillType());
			final double res = vulnModifier + profModifier;
			double resMod = 1;
			if (res != 0)
			{
				if (res < 0)
				{
					resMod = 1 - 0.075 * res;
					resMod = 1 / resMod;
				}
				else
					resMod = 1 + 0.02 * res;

				rate *= resMod;
			}

			if (activeChar.isDebug())
			{
				final StringBuilder stat = new StringBuilder(100);
				StringUtil.append(stat, skill.getName(), " power:", String.valueOf((int) skill.getPower()), " lvl:", String.valueOf(cancelLvl), " res:", String.format("%1.2f", resMod), "(", String.format("%1.2f", profModifier), "/", String.format("%1.2f", vulnModifier), ") total:", String.valueOf(rate));
				final String result = stat.toString();
				if (activeChar.isDebug())
					activeChar.sendDebugMessage(result);
				if (Config.DEVELOPER)
					_log.info(result);
			}
			
			final L2Effect[] effects = target.getAllEffects();

			if (skill.getNegateAbnormals() != null)
				for (final L2Effect eff : effects)
				{
					if (eff == null)
						continue;

					for (final String negateAbnormalType : skill.getNegateAbnormals().keySet())
						if (negateAbnormalType.equalsIgnoreCase(eff.getAbnormalType()) && skill.getNegateAbnormals().get(negateAbnormalType) >= eff.getAbnormalLvl())
							if (calcCancelSuccess(eff, cancelLvl, (int) rate, minRate, maxRate))
								eff.exit();
				}
			else
			{
				for (int i = effects.length; --i >= 0;)
				{
					effect = effects[i];
					if (effect == null)
						continue;

					if (!effect.canBeStolen())
					{
						effects[i] = null;
						continue;
					}

					// first pass - dances/songs only
					if (!effect.getSkill().isDance())
						continue;

					if (effect.getSkill().getId() == lastCanceledSkillId)
					{
						effect.exit(); // this skill already canceled
						continue;
					}

					if (!calcCancelSuccess(effect, cancelLvl, (int) rate, minRate, maxRate))
						continue;

					lastCanceledSkillId = effect.getSkill().getId();
					effect.exit();
					count--;

					if (count == 0)
						break;
				}

				if (count != 0)
				{
					lastCanceledSkillId = 0;
					for (int i = effects.length; --i >= 0;)
					{
						effect = effects[i];
						if (effect == null)
							continue;

						// second pass - all except dances/songs
						if (effect.getSkill().isDance())
							continue;

						if (effect.getSkill().getId() == lastCanceledSkillId)
						{
							effect.exit(); // this skill already canceled
							continue;
						}

						if (!calcCancelSuccess(effect, cancelLvl, (int) rate, minRate, maxRate))
							continue;

						lastCanceledSkillId = effect.getSkill().getId();
						effect.exit();
						count--;

						if (count == 0)
							break;
					}
				}
			}

			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}

		// Applying self-effects
		if (skill.hasSelfEffects())
		{
			effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
				// Replace old effect with new one.
				effect.exit();
			skill.getEffectsSelf(activeChar);
		}
	}

	private boolean calcCancelSuccess(final L2Effect effect, final int cancelLvl, final int baseRate, final int minRate, final int maxRate)
	{
		int rate = 2 * (cancelLvl - effect.getSkill().getMagicLevel());
		rate += effect.getAbnormalTime() / 120;
		rate += baseRate;

		if (rate < minRate)
			rate = minRate;
		else if (rate > maxRate)
			rate = maxRate;

		return Rnd.get(100) < rate;
	}

	/**
	 * @see ct25.xtreme.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}