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
 * this program. If not, see <http://L2J.EternityWorld.ru/>.
 */
package ct25.xtreme.gameserver.network.serverpackets;

import ct25.xtreme.gameserver.model.entity.event.UCArena;

public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private static final String _S__FE_7F_EXPVPMATCHUSERDIE = "[S] FE:7F ExPVPMatchUserDie";
	
	private final int _t1;
	private final int _t2;
	
	public ExPVPMatchUserDie(UCArena a)
	{
		_t1 = a.getTeams()[0].getKillCount();
		_t2 = a.getTeams()[1].getKillCount();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7e);
		
		writeD(_t1);
		writeD(_t2);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_7F_EXPVPMATCHUSERDIE;
	}
}