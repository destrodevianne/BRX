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
package handlers.itemhandlers;

import ct25.xtreme.gameserver.handler.IItemHandler;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.templates.item.L2Item;
import ct25.xtreme.gameserver.templates.item.L2Weapon;
import ct25.xtreme.gameserver.util.Broadcast;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:07 $
 */

public class SpiritShot implements IItemHandler
{
	/**
	 * @see ct25.xtreme.gameserver.handler.IItemHandler#useItem(ct25.xtreme.gameserver.model.actor.L2Playable, ct25.xtreme.gameserver.model.L2ItemInstance, boolean)
	 */
	@Override
	public synchronized void useItem(final L2Playable playable, final L2ItemInstance item, final boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		final L2PcInstance activeChar = (L2PcInstance) playable;
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		final int itemId = item.getId();

		// Check if Spirit shot can be used
		if (weaponInst == null || weaponItem.getSpiritShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
			return;
		}

		// Check if Spirit shot is already active
		if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
			return;

		final int weaponGrade = weaponItem.getCrystalType();

		boolean gradeCheck = true;

		switch (weaponGrade)
		{
			case L2Item.CRYSTAL_NONE:
				if (itemId != 5790 && itemId != 2509)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_D:
				if (itemId != 2510 && itemId != 22077)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_C:
				if (itemId != 2511 && itemId != 22078)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_B:
				if (itemId != 2512 && itemId != 22079)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_A:
				if (itemId != 2513 && itemId != 22080)
					gradeCheck = false;
				break;
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				if (itemId != 2514 && itemId != 22081)
					gradeCheck = false;
				break;
		}

		if (!gradeCheck)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));

			return;
		}

		// Consume Spirit shot if player has enough of them
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (!activeChar.disableAutoShot(itemId))
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS));
			return;
		}

		// Charge Spirit shot
		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
		int skillId = 0;
		switch (itemId)
		{
			case 2509:
			case 5790:
				skillId = 2061;
				break;
			case 2510:
				skillId = 2155;
				break;
			case 2511:
				skillId = 2156;
				break;
			case 2512:
				skillId = 2157;
				break;
			case 2513:
				skillId = 2158;
				break;
			case 2514:
				skillId = 2159;
				break;
			case 22077:
				skillId = 26055;
				break;
			case 22078:
				skillId = 26056;
				break;
			case 22079:
				skillId = 26057;
				break;
			case 22080:
				skillId = 26058;
				break;
			case 22081:
				skillId = 26059;
				break;
			
		}
		// Send message to client
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENABLED_SPIRITSHOT));
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skillId, 1, 0, 0), 360000);
	}
}
