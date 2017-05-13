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
package ai.zones.SilentValley;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.network.clientpackets.Say2;

/**
 * Silent Valley AI
 * @author malyelfik
 */
public class SilentValley extends L2AttackableAIScript
{
	// Skills
	private static final SkillHolder BETRAYAL = new SkillHolder(6033, 1); // Treasure Seeker's Betrayal
	private static final SkillHolder BLAZE = new SkillHolder(4157, 10); // NPC Blaze - Magic

	// Item
	private static final int SACK = 13799; // Treasure Sack of the Ancient Giants

	// Chance
	private static final int SPAWN_CHANCE = 2;
	private static final int CHEST_DIE_CHANCE = 5;

	// Monsters
	private static final int CHEST = 18693; // Treasure Chest of the Ancient Giants
	private static final int GUARD1 = 18694; // Treasure Chest Guard
	private static final int GUARD2 = 18695; // Treasure Chest Guard
	private static final int[] MOBS =
	{
		20965, // Chimera Piece
		20966, // Changed Creation
		20967, // Past Creature
		20968, // Nonexistent Man
		20969, // Giant's Shadow
		20970, // Soldier of Ancient Times
		20971, // Warrior of Ancient Times
		20972, // Shaman of Ancient Times
		20973, // Forgotten Ancient People
	};

	private SilentValley()
	{
		super(-1, SilentValley.class.getSimpleName(), "ai/zones");
		addAttackId(MOBS);
		addAttackId(CHEST, GUARD1, GUARD2);
		addEventReceivedId(GUARD1, GUARD2);
		addKillId(MOBS);
		addSeeCreatureId(MOBS);
		addSeeCreatureId(GUARD1, GUARD2);
		addSpawnId(CHEST, GUARD2);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (npc != null && !npc.isDead())
			switch (event)
			{
				case "CLEAR":
					npc.doDie(null);
					break;
				case "CLEAR_EVENT":
					npc.broadcastEvent("CLEAR_ALL_INSTANT", 2000, null);
					npc.doDie(null);
					break;
				case "SPAWN_CHEST":
					addSpawn(CHEST, npc.getX() - 100, npc.getY(), npc.getZ() - 100, 0, false, 0);
					break;
			}
		return null;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		switch (npc.getId())
		{
			case CHEST:
			{
				if (!isPet && npc.isScriptValue(0))
				{
					npc.setScriptValue(1);
					broadcastNpcSay(npc, Say2.ALL, 1800218); // You will be cursed for seeking the treasure!
					npc.setTarget(player);
					npc.doCast(BETRAYAL.getSkill());
				}
				else if (isPet || getRandom(100) < CHEST_DIE_CHANCE)
				{
					npc.dropItem(player, SACK, 1);
					npc.broadcastEvent("CLEAR_ALL", 2000, null);
					npc.doDie(null);
					cancelQuestTimer("CLEAR_EVENT", npc, null);
				}
				break;
			}
			case GUARD1:
			case GUARD2:
			{
				npc.setTarget(player);
				npc.doCast(BLAZE.getSkill());
				attackPlayer((L2Attackable) npc, player);
				break;
			}
			default:
			{
				if (isPet)
					attackPlayer((L2Attackable) npc, player);
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (getRandom(1000) < SPAWN_CHANCE)
		{
			final int newZ = npc.getZ() + 100;
			addSpawn(GUARD2, npc.getX() + 100, npc.getY(), newZ, 0, false, 0);
			addSpawn(GUARD1, npc.getX() - 100, npc.getY(), newZ, 0, false, 0);
			addSpawn(GUARD1, npc.getX(), npc.getY() + 100, newZ, 0, false, 0);
			addSpawn(GUARD1, npc.getX(), npc.getY() - 100, newZ, 0, false, 0);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSeeCreature(final L2Npc npc, final L2Character creature, final boolean isPet)
	{
		if (creature.isPlayable())
		{
			final L2PcInstance player = isPet ? ((L2Summon) creature).getOwner() : creature.getActingPlayer();
			if (npc.getId() == GUARD1 || npc.getId() == GUARD2)
			{
				npc.setTarget(player);
				npc.doCast(BLAZE.getSkill());
				attackPlayer((L2Attackable) npc, player);
			}
			else if (creature.isAffected(BETRAYAL.getSkillId()))
				attackPlayer((L2Attackable) npc, player);
		}
		return super.onSeeCreature(npc, creature, isPet);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		if (npc.getId() == CHEST)
		{
			npc.setIsInvul(true);
			startQuestTimer("CLEAR_EVENT", 300000, npc, null);
		}
		else
			startQuestTimer("SPAWN_CHEST", 10000, npc, null);
		return super.onSpawn(npc);
	}

	@Override
	public String onEventReceived(final String eventName, final L2Npc sender, final L2Npc receiver, final L2Object reference)
	{
		if (receiver != null && !receiver.isDead())
			switch (eventName)
			{
				case "CLEAR_ALL":
					startQuestTimer("CLEAR", 60000, receiver, null);
					break;
				case "CLEAR_ALL_INSTANT":
					receiver.doDie(null);
					break;
			}
		return super.onEventReceived(eventName, sender, receiver, reference);
	}

	public static void main(final String[] args)
	{
		new SilentValley();
	}
}