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
package ct25.xtreme.gameserver.model.abstractpc;

import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Browser
 */
public class AbstractExtension
{
	protected static final Logger _log = Logger.getLogger(AbstractExtension.class.getName());

	private L2PcInstance _activeChar = null;

	public AbstractExtension(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			_log.warning("[PcExtension] _activeChar: There can be a null value!");
			return;
		}
		_activeChar = activeChar;
		if (Config.DEBUG)
			_log.info("[PcExtension] _activeChar: " + _activeChar.getObjectId() + " - " + _activeChar.getName() + ".");
	}

	public L2PcInstance getPlayer()
	{
		return _activeChar;
	}
}
