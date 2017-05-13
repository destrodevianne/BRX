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
package ai.zones.CryptsOfDisgrace;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Browser
 */
public class CryptsOfDisgrace extends L2AttackableAIScript
{
	// Multiplier to compute the quadrant of the crypt
	private static final int[] MUL =
	{
		1,
		-1
	};

	// Offsets for crypts
	private static final int MIN_OFFSET = 50;
	private static final int MAX_OFFSET = 500;

	// ID of Mobs
	private static final int MOREK_WARRIOR = 22703;
	private static final int BATUR_WARRIOR = 22704;
	private static final int BATUR_COMMANDER = 22705;
	private static final int TURKA_FOLLOWER = 22706;
	private static final int TURKA_COMMANDER = 22707;
	private static final int GUARD_OF_GRAVE = 18815;
	private static final int TREASURE_CHEST = 18816;
	
	// Timer stuff
	private static final int DESPAWN_TIME = 120000;
	private static final int DESPAWN_BOX_TIME = 300000;
	
	// Strings
	private static final String[] FOLLOWER_SPEAK =
	{
		"I am tired! Do not wake me up again!",
		"Those who are in front of my eyes! will be destroyed"
	};
	private static final String[] COMMANDER_SPEAK =
	{
		"Who has awakened us from our slumber?",
		"All will pay a severe price to me and these here",
		"All is vanity! but this cannot be the end!"
	};
	
	// Coordinates of the center of each crypt
	private static final int[][] CRYPTS =
	{
		{
			50156,
			-124909,
			-3242
		},
		{
			46527,
			-124915,
			-3234
		},
		{
			53886,
			-124920,
			-3202
		},
		{
			52221,
			-122019,
			-3441
		},
		{
			50159,
			-119119,
			-3730
		},
		{
			48142,
			-122015,
			-3440
		}
	};
	
	public CryptsOfDisgrace(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addSpawnId(TREASURE_CHEST);
		addKillId(GUARD_OF_GRAVE, TREASURE_CHEST, MOREK_WARRIOR, BATUR_WARRIOR, BATUR_COMMANDER);
		addAttackId(TURKA_FOLLOWER, TURKA_COMMANDER);
	}
	
	private void sortTextMessage(final L2Npc npc)
	{
		if (npc.getId() == TURKA_FOLLOWER)
		{
			if (getRandom(100) < 1)
				broadcastText(npc, FOLLOWER_SPEAK[getRandom(FOLLOWER_SPEAK.length)]);
		}
		else if (npc.getId() == TURKA_COMMANDER)
			if (getRandom(100) < 1)
				broadcastText(npc, COMMANDER_SPEAK[getRandom(COMMANDER_SPEAK.length)]);
	}
	
	public void broadcastText(final L2Npc npc, final String text)
	{
		npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), text));
	}
	
	private void forceAttack(final L2PcInstance killer, final L2Npc npc)
	{
		((L2Attackable) npc).addDamageHate(killer, 0, 99999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer, true);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("despawnMe") && npc != null)
		{
			final L2Npc chest = addSpawn(TREASURE_CHEST, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, 0);
			chest.setIsImmobilized(true);
			chest.disableCoreAI(true);
			npc.deleteMe();
		}
		else if (event.equalsIgnoreCase("despawnBox") && npc != null)
			npc.deleteMe();
		return null;
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		startQuestTimer("despawnBox", DESPAWN_BOX_TIME, npc, null);
		return null;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		sortTextMessage(npc);
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		switch (npc.getId())
		{
			case MOREK_WARRIOR:
			case BATUR_WARRIOR:
			case BATUR_COMMANDER:
				if (getRandom(100) < 2)
				{
					switch (getRandom(2))
					{
						case 0:
							final int nearby[] = CRYPTS[getNearbyCrypt(npc.getX(), npc.getY(), npc.getZ())];
							final L2Npc commander = addSpawn(TURKA_COMMANDER, nearby[0] + getRandom(MIN_OFFSET, MAX_OFFSET) * MUL[getRandom(2)], nearby[1] + getRandom(MIN_OFFSET, MAX_OFFSET) * MUL[getRandom(2)], nearby[2], 0, false, 0, false, 0);
							forceAttack(killer, commander);
							L2Npc follower = null;
							for (int i = 0; i <= 7; i++)
							{
								follower = addSpawn(TURKA_FOLLOWER, nearby[0] + getRandom(MIN_OFFSET, MAX_OFFSET) * MUL[getRandom(2)], nearby[1] + getRandom(MIN_OFFSET, MAX_OFFSET) * MUL[getRandom(2)], nearby[2], 0, false, 0, false, 0);
								forceAttack(killer, follower);
							}
							break;
						case 1:
							if (getRandom(2) < 1)
							{
								final L2Npc leader = addSpawn(GUARD_OF_GRAVE, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, 0);
								leader.setIsImmobilized(true);
								npc.broadcastPacket(new CreatureSay(leader.getObjectId(), Say2.ALL, leader.getName(), FOLLOWER_SPEAK[1]));
								leader.doCast(SkillTable.getInstance().getInfo(5313, 1));
								forceAttack(killer, leader);
								startQuestTimer("despawnMe", DESPAWN_TIME, leader, null);
							}
							break;
					}
					break;
				}
				break;
			case GUARD_OF_GRAVE:
				cancelQuestTimer("despawnMe", npc, null);
				break;
			case TREASURE_CHEST:
				cancelQuestTimer("despawnBox", npc, null);
				break;
			
		}
		return null;
	}

	private int getNearbyCrypt(final int x, final int y, final int z)
	{
		int nearby = 0;
		double distance = 999999999, distance2D = 0, distance3D = 0;
		for (int i = 0; i < CRYPTS.length; i++)
		{
			final int[] pos = CRYPTS[i];
			distance2D = Math.pow(Math.abs(x > pos[0] ? x - pos[0] : pos[0] - x), 2) + Math.pow(Math.abs(y > pos[1] ? y - pos[1] : pos[1] - y), 2);
			distance3D = Math.sqrt(distance2D + Math.pow(Math.abs(z > pos[2] ? z - pos[2] : pos[2] - z), 2));
			if (distance3D < distance)
			{
				nearby = i;
				distance = distance3D;
			}
		}
		return nearby;
	}
	
	public static void main(final String[] args)
	{
		new CryptsOfDisgrace(-1, CryptsOfDisgrace.class.getSimpleName(), "ai/zones");
	}
}