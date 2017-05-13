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
package ai.zones.PlainsOfDion;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.GeoData;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.util.Util;

/**
 * AI for mobs in Plains of Dion (near Floran Village).
 * @author Gladicek
 */
public final class PlainsOfDion extends L2AttackableAIScript
{
	// Npcs
	private static final int DELU_LIZARDMEN[] =
	{
		21104, // Delu Lizardman Supplier
		21105, // Delu Lizardman Special Agent
		21107, // Delu Lizardman Commander
	};

	// Strings
	private static final String[] MONSTERS_MSG =
	{
		"How dare you interrupt our fight! Hey guys, help!",
		"Hey! We're having a duel here!",
		"The duel is over! Attack!",
		"Foul! Kill the coward!",
		"How dare you interrupt a sacred duel! You must be taught a lesson!"
	
	};
	private static final String[] MONSTERS_ASSIST_MSG =
	{
		"Die, you coward!",
		"Kill the coward!",
		"What are you looking at?"
	};

	private PlainsOfDion(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		registerMobs(DELU_LIZARDMEN, QuestEventType.ON_ATTACK);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		if (npc.isScriptValue(0))
		{
			{
				npc.broadcastNpcSay(Say2.ALL, MONSTERS_MSG[getRandom(5)]);
			}

			for (final L2Character obj : npc.getKnownList().getKnownCharactersInRadius(npc.getFactionRange()))
				if (obj.isMonster() && Util.contains(DELU_LIZARDMEN, ((L2MonsterInstance) obj).getId()) && !obj.isAttackingNow() && !obj.isDead() && GeoData.getInstance().canSeeTarget(npc, obj))
				{
					final L2MonsterInstance monster = (L2MonsterInstance) obj;
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					monster.broadcastNpcSay(Say2.ALL, MONSTERS_ASSIST_MSG[getRandom(3)]);
				}
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	public static void main(final String[] args)
	{
		new PlainsOfDion(-1, PlainsOfDion.class.getSimpleName(), "ai/zones");
	}
}