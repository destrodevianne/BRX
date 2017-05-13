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

import ct25.xtreme.gameserver.handler.ISkillHandler;
import ct25.xtreme.gameserver.instancemanager.CastleManager;
import ct25.xtreme.gameserver.instancemanager.FortManager;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Castle;
import ct25.xtreme.gameserver.model.entity.Fort;
import ct25.xtreme.gameserver.skills.Formulas;
import ct25.xtreme.gameserver.templates.item.L2WeaponType;
import ct25.xtreme.gameserver.templates.skills.L2SkillType;

/**
 * @author _tomciaaa_
 */
public class StrSiegeAssault implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STRSIEGEASSAULT
	};

	/**
	 * @see ct25.xtreme.gameserver.handler.ISkillHandler#useSkill(ct25.xtreme.gameserver.model.actor.L2Character, ct25.xtreme.gameserver.model.L2Skill, ct25.xtreme.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{

		if (!(activeChar instanceof L2PcInstance))
			return;

		final L2PcInstance player = (L2PcInstance) activeChar;

		if (!player.isRidingStrider())
			return;
		if (!(player.getTarget() instanceof L2DoorInstance))
			return;

		final Castle castle = CastleManager.getInstance().getCastle(player);
		final Fort fort = FortManager.getInstance().getFort(player);

		if (castle == null && fort == null)
			return;

		if (castle != null)
		{
			if (!player.checkIfOkToUseStriderSiegeAssault(castle))
				return;
		}
		else if (!player.checkIfOkToUseStriderSiegeAssault(fort))
			return;

		try
		{
			// damage calculation
			int damage = 0;

			for (final L2Character target : (L2Character[]) targets)
			{
				final L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance) target).isFakeDeath())
					target.stopFakeDeath(true);
				else if (target.isDead())
					continue;

				final boolean dual = activeChar.isUsingDualWeapon();
				final byte shld = Formulas.calcShldUse(activeChar, target, skill);
				final boolean crit = Formulas.calcCrit(activeChar.getCriticalHit(target, skill), target);
				final boolean soul = weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER;

				if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
					damage = 0;
				else
					damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);

				if (damage > 0)
				{
					target.reduceCurrentHp(damage, activeChar, skill);
					if (soul && weapon != null)
						weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

					activeChar.sendDamageMessage(target, damage, false, false, false);

				}
				else
					activeChar.sendMessage(skill.getName() + " failed.");
			}
		}
		catch (final Exception e)
		{
			player.sendMessage("Error using siege assault:" + e);
		}
	}

	/**
	 * @see ct25.xtreme.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	public static void main(final String[] args)
	{
		new StrSiegeAssault();
	}

}
