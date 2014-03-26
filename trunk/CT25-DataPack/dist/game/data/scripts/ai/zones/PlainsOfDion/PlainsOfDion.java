/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.zones.PlainsOfDion;

import ai.group_template.L2AttackableAIScript;
import ct25.xtreme.gameserver.GeoData;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;

/**
 * AI for mobs in Plains of Dion (near Floran Village).
 * @author Gladicek
 */
public final class PlainsOfDion extends L2AttackableAIScript
{
	private static final int DELU_LIZARDMEN[] =
	{
		21104, // Delu Lizardman Supplier
		21105, // Delu Lizardman Special Agent
		21107, // Delu Lizardman Commander
	};
	
	private static final int[] MONSTERS_MSG =
	{
		100028, // $s1! How dare you interrupt our fight! Hey guys, help!
		1000388, // $s1! Hey! We're having a duel here!
		1000389, // The duel is over! Attack!
		1000390, // Foul! Kill the coward!
		1000391, // How dare you interrupt a sacred duel! You must be taught a lesson!

	};
	
	private static final int[] MONSTERS_ASSIST_MSG =
	{
		1000392, // Die, you coward!
		1000394, // Kill the coward!
		99702, // What are you looking at?
	};
	
	private PlainsOfDion(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(DELU_LIZARDMEN, QuestEventType.ON_ATTACK);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.isScriptValue(0))
		{
			
			{
				npc.broadcastNpcSay(Say2.NPC_ALL, MONSTERS_MSG[Rnd.get(5)]);
			}
			
			for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(npc.getFactionRange()))
			{
				if (obj.isMonster() && Util.contains(DELU_LIZARDMEN, ((L2MonsterInstance) obj).getId()) && !obj.isAttackingNow() && !obj.isDead() && GeoData.getInstance().canSeeTarget(npc, obj))
				{
					final L2MonsterInstance monster = (L2MonsterInstance) obj;
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					monster.broadcastNpcSay(Say2.NPC_ALL, MONSTERS_ASSIST_MSG[Rnd.get(3)]);
				}
			}
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new PlainsOfDion(-1, PlainsOfDion.class.getSimpleName(), "ai/zones");
	}
}