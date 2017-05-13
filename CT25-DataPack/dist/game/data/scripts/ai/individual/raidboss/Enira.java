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

package ai.individual.raidboss;

import java.util.Calendar;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

public class Enira extends L2AttackableAIScript
{
	// Npc
	private static final int ENIRA = 25625;

	public Enira(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		eniraSpawn();
	}
	
	private void eniraSpawn()
	{
		final Calendar _date = Calendar.getInstance();
		final int newSecond = _date.get(Calendar.SECOND);
		final int newMinute = _date.get(Calendar.MINUTE);
		final int newHour = _date.get(Calendar.HOUR);
		
		final int targetHour = (24 - newHour) * 60 * 60 * 1000;
		final int extraMinutesAndSeconds = ((60 - newMinute) * 60 + 60 - newSecond) * 1000;
		final int timerDuration = targetHour + extraMinutesAndSeconds;
		
		startQuestTimer("enira_spawn", timerDuration, null, null);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("enira_spawn"))
		{
			if (getRandom(100) <= 40)
				for (final L2Spawn eniraSpawn : SpawnTable.getInstance().getSpawns(ENIRA))
				{
					if (eniraSpawn != null)
						eniraSpawn.getLastSpawn();
					addSpawn(ENIRA, -181989, 208968, 4030, 0, false, 3600000);
				}
			eniraSpawn();
		}
		return null;
	}
	
	public static void main(final String[] args)
	{
		new Enira(-1, Enira.class.getSimpleName(), "ai/individual/raidboss");
	}
}