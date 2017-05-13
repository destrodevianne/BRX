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
package ai.zones.GiantsCave;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;

/**
 * Giant's Cave AI.
 * @author Gnacik, St3eT
 */
public final class GiantsCave extends L2AttackableAIScript
{
	// NPC
	private static final int[] SCOUTS =
	{
		22668, // Gamlin (Scout)
		22669, // Leogul (Scout)
	};

	private GiantsCave(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addAttackId(SCOUTS);
		addAggroRangeEnterId(SCOUTS);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equals("ATTACK") && player != null && npc != null && !npc.isDead())
		{
			if (npc.getId() == SCOUTS[0])
				broadcastNpcSay(npc, Say2.SHOUT, 1800865); // Intruder detected
			else
				broadcastNpcSay(npc, Say2.SHOUT, 1800861); // Oh giants, an intruder has been discovered.
				
			for (final L2Character characters : npc.getKnownList().getKnownCharactersInRadius(450))
				if (characters != null && characters.isAttackable() && getRandomBoolean())
				{
					final L2Attackable monster = (L2Attackable) characters;
					attackPlayer(monster, player);
				}
		}
		else if (event.equals("CLEAR") && npc != null && !npc.isDead())
			npc.setScriptValue(0);
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			startQuestTimer("ATTACK", 6000, npc, attacker);
			startQuestTimer("CLEAR", 120000, npc, null);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			if (getRandomBoolean())
				broadcastNpcSay(npc, Say2.ALL, 1800875); // You guys are detected!
			else
				broadcastNpcSay(npc, Say2.ALL, 1800876); // What kind of creatures are you!
			startQuestTimer("ATTACK", 6000, npc, player);
			startQuestTimer("CLEAR", 120000, npc, null);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(final String[] args)
	{
		new GiantsCave(-1, GiantsCave.class.getSimpleName(), "ai/zones");
	}
}