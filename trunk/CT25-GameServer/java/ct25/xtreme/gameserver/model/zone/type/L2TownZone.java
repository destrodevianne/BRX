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
package ct25.xtreme.gameserver.model.zone.type;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.zone.L2SpawnZone;

/**
 * A Town zone
 *
 * @author  durgus
 */
public class L2TownZone extends L2SpawnZone
{
	private String _townName;
	private int _townId;
	private int _taxById;
	private boolean _isPeaceZone;
	
	public L2TownZone(int id)
	{
		super(id);
		
		_taxById = 0;
		
		// Default not peace zone
		_isPeaceZone = false;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_townName = value;
		}
		if (name.equals("townId"))
		{
			_townId = Integer.parseInt(value);
		}
		else if (name.equals("taxById"))
		{
			_taxById = Integer.parseInt(value);
		}
		else if (name.equals("isPeaceZone"))
		{
			_isPeaceZone = Boolean.parseBoolean(value);
		}
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if (((L2PcInstance) character).getSiegeState() != 0 && Config.ZONE_TOWN == 1)
				return;
						
			if (Config.ZONE_TOWN == 2)
				((L2PcInstance) character).updatePvPFlag(1);   
		       
			character.sendMessage("You entered "+_townName);
		}
		
		if (_isPeaceZone && Config.ZONE_TOWN != 2)
			character.setInsideZone(L2Character.ZONE_PEACE, true);   
		
		character.setInsideZone(L2Character.ZONE_TOWN, true);
		
		
		if (character instanceof L2PcInstance)
			((L2PcInstance)character).setLastTownName(getName());
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		// TODO: there should be no exit if there was possibly no enter
		if (_isPeaceZone)
			character.setInsideZone(L2Character.ZONE_PEACE, false);
		
		character.setInsideZone(L2Character.ZONE_TOWN, false);
		
	     if (character instanceof L2PcInstance)
	     {
	    	 L2PcInstance activeChar = ((L2PcInstance) character);
	    	 character.sendMessage("You left "+_townName);
	    	 
	    	if (Config.ZONE_TOWN == 2)
	    		 activeChar.stopPvPFlag();  
	     }		
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	/**
	 * Returns this zones town id (if any)
	 * @return
	 */
	public int getTownId()
	{
		return _townId;
	}
	
	/**
	 * Returns this town zones name
	 * @return

	 */
	@Deprecated
	public String getName()
	{
		return _townName;
	}
	
	/**
	 * Returns this town zones castle id
	 * @return
	 */
	public final int getTaxById()
	{
		return _taxById;
	}
	
	public final boolean isPeaceZone()
	{
		return _isPeaceZone;
	}
}
