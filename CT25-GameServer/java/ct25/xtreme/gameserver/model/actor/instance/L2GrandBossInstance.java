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
package ct25.xtreme.gameserver.model.actor.instance;

import ct25.xtreme.gameserver.instancemanager.RaidBossPointsManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.entity.Hero;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct25.xtreme.util.Rnd;

/**
 * This class manages all Grand Bosses.
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2GrandBossInstance extends L2MonsterInstance
{
	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
	private boolean _useRaidCurse = true;
	
	/**
	 * Constructor for L2GrandBossInstance. This represent all grandbosses.
	 * 
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2GrandBossInstance);
		setIsRaid(true);
	}
	
	@Override
	protected int getMaintenanceInterval() { return BOSS_MAINTENANCE_INTERVAL; }
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance#doDie(ct25.xtreme.gameserver.model.actor.L2Character)
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		L2PcInstance player = null;
		
		if (killer instanceof L2PcInstance)
			player = (L2PcInstance) killer;
		else if (killer instanceof L2Summon)
			player = ((L2Summon) killer).getOwner();
		
		if (player != null)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			if (player.getParty() != null)
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.getInstance().addPoints(member, getId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if(member.isNoble())
						Hero.getInstance().setRBkilled(member.getObjectId(), getId());
				}
			}
			else
			{
				RaidBossPointsManager.getInstance().addPoints(player, getId(), (getLevel() / 2) + Rnd.get(-5, 5));
				if(player.isNoble())
					Hero.getInstance().setRBkilled(player.getObjectId(), getId());
			}
		}
		return true;
	}
	
	@Override
	public float getVitalityPoints(int damage)
	{
		return - super.getVitalityPoints(damage) / 100;
	}
	
	@Override
	public boolean useVitalityRate()
	{
		return false;
	}
	
	public void setUseRaidCurse(boolean val)
	{
		_useRaidCurse = val;
	}
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.model.actor.L2Character#giveRaidCurse()
	 */
	@Override
	public boolean giveRaidCurse()
	{
		return _useRaidCurse;
	}
}