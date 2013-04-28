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
package ai.individual.npc;

import java.util.concurrent.ScheduledFuture;

import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.ExSetCompassZoneCode;
import ct25.xtreme.gameserver.network.serverpackets.StatusUpdate;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

public class LostNestTrees extends Quest
{
	private static final String qn = "LostNestTrees";
	private static final double mpBonus = 36;
	private static final int[] ZONES = { 12203, 12204 };
	protected ScheduledFuture<?> _mpTask = null;
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE));
			if (!checkIfPc(zone) && _mpTask == null)
				_mpTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new giveMp(zone), 3000, 3000);
		}
		return super.onEnterZone(character, zone);
	}
	
	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
			if (howManyPc(zone) == 1 && _mpTask != null)
			{
				_mpTask.cancel(true);
				_mpTask = null;
			}
		}
		return super.onExitZone(character, zone);
	}
	
	private boolean checkIfPc(L2ZoneType zone)
	{
		for (L2Character c : zone.getCharactersInside().values())
		{
			if (c instanceof L2PcInstance)
				return true;
		}
		return false;
	}
	
	private int howManyPc(L2ZoneType zone)
	{
		int count = 0;
		for (L2Character c : zone.getCharactersInside().values())
		{
			if (c instanceof L2PcInstance)
				count++;
		}
		return count;
	}
	
	private void updateMp(L2Character player)
	{
		double currentMp = player.getCurrentMp();
		double maxMp = player.getMaxMp();
		if (currentMp != maxMp)
		{
			double newMp = 0;
			if ((currentMp + mpBonus) >= maxMp)
				newMp = maxMp;
			else
				newMp = currentMp + mpBonus;
			player.setCurrentMp(newMp);
			StatusUpdate sump = new StatusUpdate(player.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_MP, (int) newMp);
			player.sendPacket(sump);
			SystemMessage smp = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
			smp.addNumber((int) mpBonus);
			player.sendPacket(smp);
		}
	}
	
	private class giveMp implements Runnable
	{
		private L2ZoneType _zone;
		
		public giveMp(L2ZoneType zone)
		{
			_zone = zone;
		}
		
		public void run()
		{
			if (howManyPc(_zone) > 0)
			{
				for (L2Character c : _zone.getCharactersInside().values())
				{
					if (c instanceof L2PcInstance)
						updateMp(c);
				}
			}
			else if (_mpTask != null)
			{
				_mpTask.cancel(true);
				_mpTask = null;
			}
		}
	}
	
	public LostNestTrees(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int zones : ZONES)
		{
			addEnterZoneId(zones);
			addExitZoneId(zones);
		}
	}
	
	public static void main(String[] args)
	{
		new LostNestTrees(-1, qn, "ai/individual/npc");
	}
}