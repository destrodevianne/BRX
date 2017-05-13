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
package hellbound.Entrance;

import java.util.List;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.taskmanager.DecayTaskManager;

public class Slaves extends L2AttackableAIScript
{
	// Npcs
	private static final int[] MASTERS =
	{
		22320,
		22321
	};

	// Others
	private static final L2CharPosition MOVE_TO = new L2CharPosition(-25451, 252291, -3252, 3500);
	private static final int FSTRING_ID = 1800024;
	private static final int TRUST_REWARD = 10;
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		((L2MonsterInstance) npc).enableMinions(HellboundManager.getInstance().getLevel() < 5);
		((L2MonsterInstance) npc).setOnKillDelay(1000);
		
		return super.onSpawn(npc);
	}
	
	// Let's count trust points for killing in Engine
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (((L2MonsterInstance) npc).getMinionList() != null)
		{
			final List<L2MonsterInstance> slaves = ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions();
			if (slaves != null && !slaves.isEmpty())
				for (final L2MonsterInstance slave : slaves)
				{
					if (slave == null || slave.isDead())
						continue;

					slave.clearAggroList();
					slave.abortAttack();
					slave.abortCast();
					slave.broadcastPacket(new NpcSay(slave.getObjectId(), Say2.ALL, slave.getId(), FSTRING_ID));
					
					if (HellboundManager.getInstance().getLevel() >= 1 && HellboundManager.getInstance().getLevel() <= 2)
						HellboundManager.getInstance().updateTrust(TRUST_REWARD, false);
					
					slave.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO);
					DecayTaskManager.getInstance().addDecayTask(slave);
				}
		}

		return super.onKill(npc, killer, isPet);
	}
	
	public Slaves(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int npcId : MASTERS)
		{
			addSpawnId(npcId);
			addKillId(npcId);
		}
	}
	
	public static void main(final String[] args)
	{
		new Slaves(-1, Slaves.class.getSimpleName(), "hellbound");
	}
}