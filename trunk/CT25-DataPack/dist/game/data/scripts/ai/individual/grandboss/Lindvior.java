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
package ai.individual.grandboss;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.network.serverpackets.ExStartScenePlayer;

/**
 * Lindvior Scene AI.
 * @author Browser
 */
public class Lindvior extends L2AttackableAIScript
{
	
	private static final int RESET_HOUR = 18;
	private static final int RESET_MIN = 58;
	private static final int RESET_DAY_1 = Calendar.TUESDAY;
	private static final int RESET_DAY_2 = Calendar.FRIDAY;
	
	private static L2ZoneType _Zone;
	
	private static boolean ALT_MODE = false;
	private static int ALT_MODE_MIN = 60; // schedule delay in minutes if ALT_MODE enabled
	
	private Lindvior(int id, String name, String descr)
	{
		super(id, name, descr);
		_Zone = ZoneManager.getInstance().getZoneById(11040);
		scheduleNextLindviorVisit();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("lindvior_visit"))
		{
			if (_Zone == null)
				return null;
			
			for (L2Character visitor : _Zone.getCharactersInside().values())
			{
				if (!(visitor instanceof L2PcInstance))
					continue;
				
				((L2PcInstance)visitor).showQuestMovie(ExStartScenePlayer.LINDVIOR);
			}
			startQuestTimer("lindvior_visit", 120000, null, null);
			scheduleNextLindviorVisit();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public void scheduleNextLindviorVisit()
	{
		long delay = (ALT_MODE) ? ALT_MODE_MIN * 60000 : scheduleNextLindviorDate();
		startQuestTimer("start", delay, null, null);
	}
	
	protected long scheduleNextLindviorDate()
	{
		GregorianCalendar date = new GregorianCalendar();
		date.set(Calendar.MINUTE, RESET_MIN);
		date.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		if (System.currentTimeMillis() >= date.getTimeInMillis())
		{
			date.add(Calendar.DAY_OF_WEEK, 1);
		}
		
		int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek <= RESET_DAY_1)
		{
			date.add(Calendar.DAY_OF_WEEK, RESET_DAY_1 - dayOfWeek);
		}
		else if (dayOfWeek <= RESET_DAY_2)
		{
			date.add(Calendar.DAY_OF_WEEK, RESET_DAY_2 - dayOfWeek);
		}
		else
		{
			date.add(Calendar.DAY_OF_WEEK, 1 + RESET_DAY_1);
		}
		return date.getTimeInMillis() - System.currentTimeMillis();
	}
	
	public static void main(String[] args)
	{
		new Lindvior(-1, Lindvior.class.getSimpleName(), "ai");
	}
}