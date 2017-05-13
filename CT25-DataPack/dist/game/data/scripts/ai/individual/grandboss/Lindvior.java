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

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;

/**
 * Lindvior Scene AI.
 * @author Browser
 */
public class Lindvior extends L2AttackableAIScript
{
	// Npcs
	private static final int LINDVIOR_CAMERA = 18669;
	private static final int TOMARIS = 32552;
	private static final int ARTIUS = 32559;

	// Movie Id
	private static int LINDVIOR_SCENE_ID = 1;

	// Constants
	private static final int RESET_HOUR = 18;
	private static final int RESET_MIN = 58;
	private static final int RESET_DAY_1 = Calendar.TUESDAY;
	private static final int RESET_DAY_2 = Calendar.FRIDAY;

	private static boolean ALT_MODE = false;
	private static int ALT_MODE_MIN = 60; // schedule delay in minutes if ALT_MODE enabled

	private L2Npc _lindviorCamera = null;
	private L2Npc _tomaris = null;
	private L2Npc _artius = null;

	private Lindvior(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		scheduleNextLindviorVisit();
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		switch (event)
		{
			case "tomaris_shout1":
				broadcastNpcSay(npc, Say2.NPC_SHOUT, 1800225); // Huh? The sky looks funny. What's that?
				break;
			case "artius_shout":
				broadcastNpcSay(npc, Say2.NPC_SHOUT, 1800226); // A powerful subordinate is being held by the Barrier Orb! This reaction means...!
				break;
			case "tomaris_shout2":
				broadcastNpcSay(npc, Say2.NPC_SHOUT, 1800227); // Be careful...! Something's coming...!
				break;
			case "lindvior_scene":
				if (npc != null)
					for (final L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(4000))
						if (pl.getZ() >= 1100 && pl.getZ() <= 3100)
							pl.showQuestMovie(LINDVIOR_SCENE_ID);
				break;
			case "start":
				_lindviorCamera = SpawnTable.getInstance().getFirstSpawn(LINDVIOR_CAMERA).getLastSpawn();
				_tomaris = SpawnTable.getInstance().getFirstSpawn(TOMARIS).getLastSpawn();
				_artius = SpawnTable.getInstance().getFirstSpawn(ARTIUS).getLastSpawn();

				startQuestTimer("tomaris_shout1", 1000, _tomaris, null);
				startQuestTimer("artius_shout", 60000, _artius, null);
				startQuestTimer("tomaris_shout2", 90000, _tomaris, null);
				startQuestTimer("lindvior_scene", 120000, _lindviorCamera, null);
				scheduleNextLindviorVisit();
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}

	public void scheduleNextLindviorVisit()
	{
		final long delay = ALT_MODE ? ALT_MODE_MIN * 60000 : scheduleNextLindviorDate();
		startQuestTimer("start", delay, null, null);
	}

	protected long scheduleNextLindviorDate()
	{
		final GregorianCalendar date = new GregorianCalendar();
		date.set(Calendar.MINUTE, RESET_MIN);
		date.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		if (System.currentTimeMillis() >= date.getTimeInMillis())
			date.add(Calendar.DAY_OF_WEEK, 1);

		final int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek <= RESET_DAY_1)
			date.add(Calendar.DAY_OF_WEEK, RESET_DAY_1 - dayOfWeek);
		else if (dayOfWeek <= RESET_DAY_2)
			date.add(Calendar.DAY_OF_WEEK, RESET_DAY_2 - dayOfWeek);
		else
			date.add(Calendar.DAY_OF_WEEK, 1 + RESET_DAY_1);
		return date.getTimeInMillis() - System.currentTimeMillis();
	}

	public static void main(final String[] args)
	{
		new Lindvior(-1, Lindvior.class.getSimpleName(), "ai/individual/grandboss");
	}
}