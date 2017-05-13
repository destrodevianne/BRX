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
package teleports.Warpgate;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import quests.Q00130_PathToHellbound.Q00130_PathToHellbound;
import quests.Q00133_ThatsBloodyHot.Q00133_ThatsBloodyHot;

public final class Warpgate extends Quest
{
	// Misc
	private static final int MAP = 9994;
	private static final int ZONE = 40101;

	// Teleports
	private static final int[] WARPGATES =
	{
		32314,
		32315,
		32316,
		32317,
		32318,
		32319
	};

	// Locations
	private static final Location HELLBOUND = new Location(-11272, 236464, -3248);
	protected static final Location REMOVE_LOC = new Location(-16555, 209375, -3670);

	public Warpgate(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(WARPGATES);
		addFirstTalkId(WARPGATES);
		addTalkId(WARPGATES);
		addEnterZoneId(ZONE);
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (!canEnter(player))
			if (HellboundManager.getInstance().isLocked())
				return "warpgate-locked.htm";
		return npc.getId() + ".htm";
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (!canEnter(player))
			return "warpgate-no.htm";

		player.teleToLocation(HELLBOUND, true);
		if (HellboundManager.getInstance().isLocked())
			HellboundManager.getInstance().setLevel(1);
		return null;
	}

	@Override
	public final String onEnterZone(final L2Character character, final L2ZoneType zone)
	{
		if (character.isPlayer())
			if (!canEnter(character.getActingPlayer()) && !character.isGM())
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character), 1000);
			else if (!character.getActingPlayer().isMinimapAllowed())
				if (character.getInventory().getItemByItemId(MAP) != null)
					character.getActingPlayer().setMinimapAllowed(true);
		return null;
	}

	private static final class Teleport implements Runnable
	{
		private final L2Character _char;

		public Teleport(final L2Character c)
		{
			_char = c;
		}

		@Override
		public void run()
		{
			try
			{
				_char.teleToLocation(REMOVE_LOC, true);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static boolean canEnter(final L2PcInstance player)
	{
		if (player.isFlying())
			return false;

		if (Config.HELLBOUND_WITHOUT_QUEST)
			return true;

		QuestState st;
		if (!HellboundManager.getInstance().isLocked())
		{
			st = player.getQuestState(Q00130_PathToHellbound.class.getSimpleName());
			if (st != null && st.isCompleted())
				return true;
		}
		st = player.getQuestState(Q00133_ThatsBloodyHot.class.getSimpleName());
		return st != null && st.isCompleted();
	}

	public static void main(final String[] args)
	{
		new Warpgate(-1, "Warpgate", "teleports");
	}
}