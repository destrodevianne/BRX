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

import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.event.UCTeam;

public class PVPMatchRecord extends L2GameServerPacket
{
	private static final String _S__FE_7E_PVPMatchRecord = "[S] FE:7E PVPMatchRecord";
	
	private final UCTeam _t1;
	private final UCTeam _t2;
	private final boolean _winner;
	
	/**
	 * @param team1 Team 1's player records
	 * @param team2 Team 2's player records
	 * @param t1wins UNK
	 */
	public PVPMatchRecord(UCTeam team1, UCTeam team2, boolean t1wins)
	{
		_t1 = team1;
		_t2 = team2;
		_winner = t1wins;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7e);
		
		writeD(0x02); // team count?
		writeD(_winner ? 0x01 : 0x02); // winner team no?
		writeD(_winner ? 0x02 : 0x01); // loser team no?
		
		writeD(0x00); // ??
		writeD(0x00); // ??
		
		writeD(_t1.getParty().getMemberCount() + _t2.getParty().getMemberCount()); // total players
		for (L2PcInstance member : _t1.getParty().getPartyMembers())
		{
			if (member == null)
				continue;
			
			writeS(member.getName()); // player name
			writeD(member.getUCKills()); // kills
			writeD(member.getUCDeaths()); // deaths
		}
		for (L2PcInstance member : _t2.getParty().getPartyMembers())
		{
			if (member == null)
				continue;
			
			writeS(member.getName()); // player name
			writeD(member.getUCKills()); // kills
			writeD(member.getUCDeaths()); // deaths
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_7E_PVPMatchRecord;
	}
}