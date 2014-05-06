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


public class ExChangeAreaState extends L2GameServerPacket
{
	private final int _areaID;
	private final int _state;
	
    public ExChangeAreaState(int id, int state)
    {
        _areaID = id;
        _state = state;
    }

    protected final void writeImpl()
    {
        writeC(254);
        writeH(193);
        writeD(_areaID);
        writeD(_state);
    }

    public String getType()
    {
        return "[S] FE:C1 ExChangeAreaState";
    }
}