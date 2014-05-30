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
package ct25.xtreme.gameserver.network.serverpackets;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.BotPunish;
import ct25.xtreme.gameserver.model.actor.L2Character;

/**
 * 0000: 01  7a 73 10 4c  b2 0b 00 00  a3 fc 00 00  e8 f1 ff    .zs.L...........
 * 0010: ff  bd 0b 00 00  b3 fc 00 00  e8 f1 ff ff             .............

 *
 * ddddddd
 *
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class MoveToLocation extends L2GameServerPacket
{
	private static final String _S__01_CHARMOVETOLOCATION = "[S] 2f MoveToLocation";
	private int _charObjId, _x, _y, _z, _xDst, _yDst, _zDst;
	private L2Character _cha;
	
	public MoveToLocation(L2Character cha)
	{
		_cha = cha;
		_charObjId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_xDst = cha.getXdestination();
		_yDst = cha.getYdestination();
		_zDst = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		// Bot punishment restriction
		if(_cha instanceof L2PcInstance && Config.ENABLE_BOTREPORT)
		{
			L2PcInstance actor = (L2PcInstance) _cha;
			if(actor.isBeingPunished())
			{
				if(actor.getPlayerPunish().canWalk() && actor.getPlayerPunish().getBotPunishType() == BotPunish.PunishType.MOVEBAN)
				{
					actor.endPunishment();
				}
				else
				{
					actor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_REPORTED_120_MIN_MOVEMENT_BLOCKED));
					return;
				}
			}
		}
		writeC(0x2f);
		
		writeD(_charObjId);
		
		writeD(_xDst);
		writeD(_yDst);
		writeD(_zDst);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__01_CHARMOVETOLOCATION;
	}
	
}
