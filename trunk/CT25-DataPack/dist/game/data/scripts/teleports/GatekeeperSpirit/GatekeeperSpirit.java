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
package teleports.GatekeeperSpirit;

import ct25.xtreme.gameserver.SevenSigns;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public class GatekeeperSpirit extends Quest
{
	private final static int ENTER_GK = 31111;
	private final static int EXIT_GK = 31112;

	private final static int Lilith = 25283;
	private final static int Anakim = 25286;

	public GatekeeperSpirit(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(ENTER_GK);
		addFirstTalkId(ENTER_GK);
		addTalkId(ENTER_GK);

		addStartNpc(EXIT_GK);
		addFirstTalkId(EXIT_GK);
		addTalkId(EXIT_GK);

		this.addEventId(Lilith, QuestEventType.ON_KILL);
		this.addEventId(Anakim, QuestEventType.ON_KILL);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		
		if (event.equalsIgnoreCase("enter"))
		{
			final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
			final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
			final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
			final boolean validation = SevenSigns.getInstance().isSealValidationPeriod();

			if (validation && playerCabal == sealAvariceOwner && playerCabal == compWinner)
			{
				switch (sealAvariceOwner)
				{
					case SevenSigns.CABAL_DAWN:
						player.teleToLocation(184448, -10112, -5504, false);
						break;
					case SevenSigns.CABAL_DUSK:
						player.teleToLocation(184464, -13104, -5504, false);
						break;
				}
				return null;
			}
			htmltext = "spirit_gate_q0506_01.htm";
		}
		else if (event.equalsIgnoreCase("exit"))
		{
			player.teleToLocation(182960, -11904, -4897, true);
			return null;
		}
		
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		switch (npc.getId())
		{
			case ENTER_GK:
				htmltext = "spirit_gate001.htm";
				break;
			case EXIT_GK:
				htmltext = "spirit_gate002.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (npcId == Lilith)
			addSpawn(EXIT_GK, 184410, -10111, -5488, 0, false, 900000);
		else if (npcId == Anakim)
			addSpawn(EXIT_GK, 184410, -13102, -5488, 0, false, 900000);
		return super.onKill(npc, killer, isPet);
	}

	public static void main(final String[] args)
	{
		new GatekeeperSpirit(-1, "GatekeeperSpirit", "teleports");
	}
}