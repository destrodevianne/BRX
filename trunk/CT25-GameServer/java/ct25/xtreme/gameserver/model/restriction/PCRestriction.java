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
package ct25.xtreme.gameserver.model.restriction;

import java.util.logging.Level;
import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.TvTEvent;
import ct25.xtreme.gameserver.util.Util;


/**
 * @author L0ngh0rn
 */
public class PCRestriction extends AbstractRestriction
{
	private static Logger	_log	= Logger.getLogger(PCRestriction.class.getName());

	private static final class SingletonHolder
	{
		private static final PCRestriction	INSTANCE	= new PCRestriction();
	}

	public static PCRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		checkIllegalEnchant(activeChar);
		checkingEvent(activeChar);
	}

	@Override
	public void playerDisconnected(L2PcInstance activeChar)
	{
	}

	private static void checkingEvent(L2PcInstance activeChar)
	{
		if (Config.TVT_EVENT_ENABLED)
			TvTEvent.onLogin(activeChar);
	}
	
	private void checkIllegalEnchant(L2PcInstance activeChar)
	{
		if (!activeChar.isGM() && Config.ALLOW_VALID_ENCHANT)
		{
			for (L2ItemInstance i : activeChar.getInventory().getItems())
			{
				if (i.isArmor() && i.getEnchantLevel() > Config.ENCHANT_MAX_ARMOR)
					actionIllegalEnchant(activeChar, i, "Armor");
				else if (i.isWeapon() && i.getEnchantLevel() > Config.ENCHANT_MAX_WEAPON)
					actionIllegalEnchant(activeChar, i, "Weapon");
			}
		}
	}

	public static void actionIllegalEnchant(L2PcInstance activeChar, L2ItemInstance i, String cat)
	{
		if (Config.DESTROY_ENCHANT_ITEM)
		{
			activeChar.getInventory().destroyItem("Over Enchant", i, activeChar, null);
			_log.log(Level.WARNING, "Player: " + activeChar.getName() + " use illegal item (" + cat + "). Item: " + i
					+ " was deleted!");
		}
		else
			_log.log(Level.WARNING, "Player: " + activeChar.getName() + " use illegal item (" + cat + "). Item: " + i
					+ "!");
		activeChar.sendMessage("[Server]: You have Items over enchanted!");
		if (Config.PUNISH_PLAYER)
			Util.handleIllegalPlayerAction(activeChar, "Player: " + activeChar.getName() + " use illegal item (" + cat
					+ "). Item: " + i + "!", Config.DEFAULT_PUNISH);
	}
}
