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
package hellbound.TowerOfInfinitum;

import java.util.Map;

import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.util.Util;
import javolution.util.FastMap;

public class TowerOfInfinitum extends Quest
{
	// Npcs
	private static final int JERIAN = 32302;
	private static final int GK_FIRST = 32745;
	private static final int GK_LAST = 32752;
	
	// Skill
	private static final int PASS_SKILL = 2357;

	// Telecoords
	private static final Map<Integer, int[][]> TELE_COORDS = new FastMap<>();

	static
	{
		TELE_COORDS.put(32745, new int[][]
		{
			{
				-22208,
				277122,
				-13376
			},
			{
				0,
				0,
				0
			}
		});
		TELE_COORDS.put(32746, new int[][]
		{
			{
				-22208,
				277106,
				-11648
			},
			{
				-22208,
				277074,
				-15040
			}
		});
		TELE_COORDS.put(32747, new int[][]
		{
			{
				-22208,
				277120,
				-9920
			},
			{
				-22208,
				277120,
				-13376
			}
		});
		TELE_COORDS.put(32748, new int[][]
		{
			{
				-19024,
				277126,
				-8256
			},
			{
				-22208,
				277106,
				-11648
			}
		});
		
		TELE_COORDS.put(32749, new int[][]
		{
			{
				-19024,
				277106,
				-9920
			},
			{
				-22208,
				277122,
				-9920
			}
		});
		TELE_COORDS.put(32750, new int[][]
		{
			{
				-19008,
				277100,
				-11648
			},
			{
				-19024,
				277122,
				-8256
			}
		});
		TELE_COORDS.put(32751, new int[][]
		{
			{
				-19008,
				277100,
				-13376
			},
			{
				-19008,
				277106,
				-9920
			}
		});
		TELE_COORDS.put(32752, new int[][]
		{
			{
				14602,
				283179,
				-7500
			},
			{
				-19008,
				277100,
				-11648
			}
		});
	}
	
	public TowerOfInfinitum(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(JERIAN);
		addTalkId(JERIAN);

		for (int i = GK_FIRST; i <= GK_LAST; i++)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		final int npcId = npc.getId();

		if (event.equalsIgnoreCase("enter") && npcId == JERIAN)
		{
			if (HellboundManager.getInstance().getLevel() >= 11)
			{
				final L2Party party = player.getParty();
				if (party != null && party.getPartyLeaderOID() == player.getObjectId())
				{
					for (final L2PcInstance partyMember : party.getPartyMembers())
						if (!Util.checkIfInRange(300, partyMember, npc, true) || partyMember.getFirstEffect(PASS_SKILL) == null)
							return "32302-02.htm";
					for (final L2PcInstance partyMember : party.getPartyMembers())
						partyMember.teleToLocation(-22204, 277056, -15023, true);
					
					htmltext = null;
					
				}
				else
					htmltext = "32302-02a.htm";
			}
			else
				htmltext = "32302-02b.htm";

		}

		else if ((event.equalsIgnoreCase("up") || event.equalsIgnoreCase("down")) && npcId >= GK_FIRST && npcId <= GK_LAST)
		{
			final int direction = event.equalsIgnoreCase("up") ? 0 : 1;
			final L2Party party = player.getParty();
			if (party == null)
				htmltext = "gk-noparty.htm";
			else if (party.getPartyLeaderOID() != player.getObjectId())
				htmltext = "gk-noreq.htm";
			else
			{
				for (final L2PcInstance partyMember : party.getPartyMembers())
					if (!Util.checkIfInRange(1000, partyMember, npc, false) || Math.abs(partyMember.getZ() - npc.getZ()) > 100)
						return "gk-noreq.htm";
					
				final int tele[] = TELE_COORDS.get(npcId)[direction];
				for (final L2PcInstance partyMember : party.getPartyMembers())
					partyMember.teleToLocation(tele[0], tele[1], tele[2], true);
				
				htmltext = null;
			}
		}

		return htmltext;
	}
	
	public static void main(final String[] args)
	{
		new TowerOfInfinitum(-1, TowerOfInfinitum.class.getSimpleName(), "hellbound");
	}
}