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

import ct25.xtreme.gameserver.instancemanager.GraciaSeedsManager;

/**
 * format: 0xfe cd(dddd)
 * FE - packet id
 * A1 00 - packet subid
 * d - seed count
 * d - x pos
 * d - y pos
 * d - z pos
 * d - sys msg no
 *
 */
public class ExShowSeedMapInfo extends L2GameServerPacket
{
	private static final String _S__FE_A1_EXSHOWSEEDMAPINFO = "[S] FE:A1 ExShowSeedMapInfo";
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0xA1); // SubId
		
		writeD(2); // seed count
		
		// Seed of Destruction
		writeD(-246857); // x coord
		writeD(251960); // y coord
		writeD(4331); // z coord
		writeD(2770 + GraciaSeedsManager.getInstance().getSoDState()); // sys msg id
		
		// Seed of Infinity
		writeD(-213770); // x coord
		writeD(210760); // y coord
		writeD(4400); // z coord
		writeD(2765 + GraciaSeedsManager.getInstance().getSoIState()); // sys msg id
	}
	
	@Override
	public String getType()
	{
		return _S__FE_A1_EXSHOWSEEDMAPINFO;
	}
}
