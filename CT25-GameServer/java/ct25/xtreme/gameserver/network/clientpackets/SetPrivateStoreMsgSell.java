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
package ct25.xtreme.gameserver.network.clientpackets;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.PrivateStoreMsgSell;
import ct25.xtreme.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreMsgSell extends L2GameClientPacket
{
	private static final String _C__77_SETPRIVATESTOREMSGSELL = "[C] 77 SetPrivateStoreMsgSell";
	//private static Logger _log = Logger.getLogger(SetPrivateStoreMsgSell.class.getName());
	
	private static final int MAX_MSG_LENGTH = 29;
	
	private String _storeMsg;
	
	@Override
	protected void readImpl()
	{
		_storeMsg = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.getSellList() == null)
			return;

		if (Config.BOT_DETECT && _storeMsg != null && _storeMsg.length() > MAX_MSG_LENGTH)
		{
			Util.handleIllegalPlayerAction(player, "BotDetector: Player " + player.getName() + " exceed store title limit.", Config.BOT_PUNISH);
			return;
		}
		
		player.getSellList().setTitle(_storeMsg);
		sendPacket(new PrivateStoreMsgSell(player));
	}
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__77_SETPRIVATESTOREMSGSELL;
	}
	
}
