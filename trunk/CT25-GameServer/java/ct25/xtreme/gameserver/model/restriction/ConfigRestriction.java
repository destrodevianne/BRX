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

import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.TvTEvent;

/**
 * @author L0ngh0rn/ reworked by Browser
 *
 */
public class ConfigRestriction extends AbstractRestriction
{
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(ConfigRestriction.class.getName());
			
	private static final class SingletonHolder
	{
		private static final ConfigRestriction INSTANCE = new ConfigRestriction();
	}
	
	public static ConfigRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		checkingEvent(activeChar);
		
		activeChar.broadcastStatusUpdate();
		activeChar.broadcastUserInfo();
		activeChar.broadcastTitleInfo();
	}
	
	/**
	 * @param activeChar
	 */
	private static void checkingEvent(L2PcInstance activeChar)
	{
		if(Config.TVT_EVENT_ENABLED) TvTEvent.onLogin(activeChar);		
	}
}