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
package ai.individual.kamaloka;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;
import javolution.util.FastList;

/**
 * @author InsOmnia
 */
public class FollowerOfMontagnar extends L2AttackableAIScript
{
	// Npcs
	private static final int MONTAGNAR = 18568;
	private static final int FOFMONTAGNAR = 18569;
	
	// Arrays
	private final FastList<L2Attackable> _minions = new FastList<>();
	
	public FollowerOfMontagnar(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addKillId(MONTAGNAR);
		addAggroRangeEnterId(MONTAGNAR);
	}
	
	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (npc.getId() == MONTAGNAR)
		{
			final L2Attackable follower1 = (L2Attackable) addSpawn(FOFMONTAGNAR, npc.getX() + getRandom(50), npc.getY() + getRandom(50), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			final L2Attackable follower2 = (L2Attackable) addSpawn(FOFMONTAGNAR, npc.getX() + getRandom(50), npc.getY() + getRandom(50), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
			_minions.add(follower1);
			_minions.add(follower2);
			follower1.setIsInvul(true);
			follower2.setIsInvul(true);
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), player.getName() + "! Get him!!!"));
			for (final L2Attackable minion : _minions)
			{
				minion.clearAggroList();
				minion.setIsRunning(true);
				minion.addDamageHate(player, 1, 99999);
				minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
			this.startQuestTimer("changetarget", 10000, npc, null, true);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("changetarget"))
		{
			L2PcInstance target = null;
			if (npc.getKnownList().getKnownCharactersInRadius(2000) != null)
				cancelQuestTimers("changetarget");
			for (final L2Character c : npc.getKnownList().getKnownCharactersInRadius(2000))
				if (c instanceof L2PcInstance)
					if (getRandom(100) <= 50)
					{
						target = (L2PcInstance) c;
						break;
					}
			if (target != null)
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), target.getName() + "! Get him!!!"));
			for (final L2Attackable minion : _minions)
			{
				minion.clearAggroList();
				minion.setIsRunning(true);
				minion.addDamageHate(target, 1, 99999);
				minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		cancelQuestTimers("changetarget");
		for (final L2Attackable minion : _minions)
			if (npc.getInstanceId() == minion.getInstanceId())
			{
				minion.decayMe();
				_minions.remove(minion);
			}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(final String[] args)
	{
		new FollowerOfMontagnar(-1, FollowerOfMontagnar.class.getSimpleName(), "ai/individual/kamaloka");
	}
}