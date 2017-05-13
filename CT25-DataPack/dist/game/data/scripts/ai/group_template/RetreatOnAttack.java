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
package ai.group_template;

import java.util.Map;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import javolution.util.FastMap;

/**
 * Flee onAttack (current version is rather fear than retreat) TODO: Find a way to check position instead of using a timer to stop fleeing make zones or a list of "basements" for mobs to retreat to
 * @author Browser
 */
public class RetreatOnAttack extends L2AttackableAIScript
{
	private static final Map<Integer, Integer[]> MOBSPAWNS = new FastMap<>();
	// NpcId, {HP %, Chance %}
	static
	{
		MOBSPAWNS.put(20432, new Integer[]
		{
			100,
			40
		}); // Elpy
		MOBSPAWNS.put(22228, new Integer[]
		{
			100,
			40
		}); // Grey Elpy
		MOBSPAWNS.put(20058, new Integer[]
		{
			50,
			10
		}); // Ol Mahum Guard
		MOBSPAWNS.put(20207, new Integer[]
		{
			30,
			1
		}); // Ol Mahum Guerilla
		MOBSPAWNS.put(20208, new Integer[]
		{
			50,
			1
		}); // Ol Mahum Raider
		MOBSPAWNS.put(21508, new Integer[]
		{
			30,
			100
		}); // Splinter Stakato
		MOBSPAWNS.put(21509, new Integer[]
		{
			30,
			100
		}); // Splinter Stakato Walker
		MOBSPAWNS.put(21510, new Integer[]
		{
			30,
			100
		}); // Splinter Stakato Soldier
		MOBSPAWNS.put(21511, new Integer[]
		{
			30,
			100
		}); // Splinter Stakato Drone
		MOBSPAWNS.put(21512, new Integer[]
		{
			30,
			100
		}); // Splinter Stakato Drone
		MOBSPAWNS.put(21513, new Integer[]
		{
			30,
			100
		}); // Needle Stakato
		MOBSPAWNS.put(21514, new Integer[]
		{
			30,
			100
		}); // Needle Stakato Worker
		MOBSPAWNS.put(21515, new Integer[]
		{
			30,
			100
		}); // Needle Stakato Soldier
		MOBSPAWNS.put(20158, new Integer[]
		{
			30,
			1
		}); // Medusa
		MOBSPAWNS.put(20497, new Integer[]
		{
			30,
			80
		}); // Turek Orc Skirmisher
		MOBSPAWNS.put(20500, new Integer[]
		{
			30,
			70
		}); // Turek Orc Sentinel
	}

	private static final Map<Integer, String[]> MOBTEXTS = new FastMap<>();
	// NpcId, Texts
	static
	{
		// Elpy
		MOBTEXTS.put(20432, new String[]
		{
			"Our chief despot will be aware of your actions",
			"Heee! The hunt is open!",
			"Mom! I do not want to end up in a stew!",
			"Despote is back !",
			"Despot will make you your party!"
		});
		// Grey Elpy
		MOBTEXTS.put(22228, new String[]
		{
			"Our chief despot will be aware of your actions",
			"Heee! The hunt is open!",
			"Mom! I do not want to end up in a stew!",
			"Despote is back !",
			"Despot will make you your party!"
		});
		// Ol Mahum Guard
		MOBTEXTS.put(20058, new String[]
		{
			"It will not remain so.",
			"YpK avenge me."
		});
		// Ol Mahum Guerilla
		MOBTEXTS.put(20207, new String[]
		{
			"An intruder!",
			"I'll give to 10M adena if you let me live!",
			"You will pay for this humiliation!",
			"I shall complain to a GM!",
			"I forgot the milk on the fire!",
			"Do not hit me!",
			"I'll kill you next time ..."
		});
		// Medusa
		MOBTEXTS.put(20158, new String[]
		{
			"Retreat!",
			"You're too much!",
			"Mom!",
			"I'm going to tell Yamaneko!"
		});
		// Ol Mahum Raider
		MOBTEXTS.put(20208, new String[]
		{
			"Agami will be aware of your actions!",
			"I'll make the skin!",
			"I will return one day!",
			"Your crime will not go unpunished!",
			"Aaaahh! I am dying!"
		});

		// Turek Orc Skirmisher
		MOBTEXTS.put(20497, new String[]
		{
			"Oh! How Strong!",
			"We shall see about that",
			"I'll definitely kill you next time"
		});

		// Turek Orc Sentinel
		MOBTEXTS.put(20500, new String[]
		{
			"Oh! How Strong!",
			"We shall see about that",
			"I'll definitely kill you next time"
		});
	}
	
	public RetreatOnAttack(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		for (final int id : MOBSPAWNS.keySet())
			addAttackId(id);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Retreat"))
			if (npc != null && player != null)
			{
				npc.stopFear(false);
				((L2Attackable) npc).addDamageHate(player, 0, 100);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (MOBSPAWNS.containsKey(npcId))
		{
			final Integer[] tmp = MOBSPAWNS.get(npcId);
			if (npc.getCurrentHp() <= npc.getMaxHp() * tmp[0] / 100.0 && getRandom(100) < tmp[1])
			{
				if (MOBTEXTS.containsKey(npcId))
				{
					final String[] allTexts = MOBTEXTS.get(npcId);
					final String text = allTexts[getRandom(allTexts.length)];
					npc.broadcastNpcSay(text);
				}
				
				int posX = npc.getX();
				int posY = npc.getY();
				final int posZ = npc.getZ();
				int signX = -500;
				int signY = -500;
				if (posX > attacker.getX())
					signX = 500;
				if (posY > attacker.getY())
					signY = 500;
				posX = posX + signX;
				posY = posY + signY;
				npc.startFear();
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
				startQuestTimer("Retreat", 10000, npc, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	public static void main(final String[] args)
	{
		new RetreatOnAttack(-1, RetreatOnAttack.class.getSimpleName(), "ai/group_template");
	}
}
