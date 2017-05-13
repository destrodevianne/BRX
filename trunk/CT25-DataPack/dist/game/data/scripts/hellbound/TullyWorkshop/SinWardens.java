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
package hellbound.TullyWorkshop;

import java.util.Map;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import javolution.util.FastMap;

public class SinWardens extends L2AttackableAIScript
{
	// Npcs
	private static final int[] SIN_WARDEN_MINIONS =
	{
		22424,
		22425,
		22426,
		22427,
		22428,
		22429,
		22430,
		22432,
		22433,
		22434,
		22435,
		22436,
		22437,
		22438
	};

	// Constants
	private final Map<Integer, Integer> killedMinionsCount = new FastMap<>();
	
	public SinWardens(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		for (final int monsterId : SIN_WARDEN_MINIONS)
			addKillId(monsterId);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.isMinion())
		{
			final L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();
			if (master != null && !master.isDead())
			{
				int killedCount = killedMinionsCount.containsKey(master.getObjectId()) ? killedMinionsCount.get(master.getObjectId()) : 0;
				killedCount++;

				if (killedCount == 5)
				{
					master.broadcastPacket(new NpcSay(master.getObjectId(), Say2.ALL, master.getId(), 1800112)); // We might need new slaves... I'll be back soon, so wait!
					master.doDie(killer);
					killedMinionsCount.remove(master.getObjectId());
				}
				else
					killedMinionsCount.put(master.getObjectId(), killedCount);
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(final String[] args)
	{
		new SinWardens(-1, SinWardens.class.getSimpleName(), "hellbound");
	}
}