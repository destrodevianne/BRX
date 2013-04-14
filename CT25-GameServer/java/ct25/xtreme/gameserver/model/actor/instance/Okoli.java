package ct25.xtreme.gameserver.model.actor.instance;

import ct25.xtreme.gameserver.network.serverpackets.L2GameServerPacket;

public class Okoli extends L2GameServerPacket
{

    public Okoli(int id, int state)
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
        return "[S] FE:C1 Okoli";
    }

    @SuppressWarnings("unused")
	private static final String _S__FE_C1__EXCHANGENPCSTATE = "[S] FE:C1 Okoli";
    private final int _areaID;
    private final int _state;
}