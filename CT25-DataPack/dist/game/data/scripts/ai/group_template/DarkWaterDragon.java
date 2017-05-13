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

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import javolution.util.FastMap;
import javolution.util.FastSet;

public class DarkWaterDragon extends L2AttackableAIScript
{
	// Npcs
	private static final int DRAGON = 22267;
	private static final int SHADE1 = 22268;
	private static final int SHADE2 = 22269;
	private static final int FAFURION = 18482;
	private static final int DETRACTOR1 = 22270;
	private static final int DETRACTOR2 = 22271;

	// Constants
	private static FastSet<Integer> secondSpawn = new FastSet<>(); // Used to track if second Shades were already spawned
	private static FastSet<Integer> myTrackingSet = new FastSet<>(); // Used to track instances of npcs
	private static FastMap<Integer, L2PcInstance> _idmap = new FastMap<Integer, L2PcInstance>().shared(); // Used to track instances of npcs

	public DarkWaterDragon(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		final int[] mobs =
		{
			DRAGON,
			SHADE1,
			SHADE2,
			FAFURION,
			DETRACTOR1,
			DETRACTOR2
		};
		this.registerMobs(mobs, QuestEventType.ON_KILL, QuestEventType.ON_SPAWN, QuestEventType.ON_ATTACK);
		myTrackingSet.clear();
		secondSpawn.clear();
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, L2PcInstance player)
	{
		if (npc != null)
			if (event.equalsIgnoreCase("first_spawn"))
				this.startQuestTimer("1", 40000, npc, null, true); // spawns detractor every 40 seconds
			else if (event.equalsIgnoreCase("second_spawn"))
				this.startQuestTimer("2", 40000, npc, null, true); // spawns detractor every 40 seconds
			else if (event.equalsIgnoreCase("third_spawn"))
				this.startQuestTimer("3", 40000, npc, null, true); // spawns detractor every 40 seconds
			else if (event.equalsIgnoreCase("fourth_spawn"))
				this.startQuestTimer("4", 40000, npc, null, true); // spawns detractor every 40 seconds
			else if (event.equalsIgnoreCase("1"))
				Quest.addSpawn(DETRACTOR1, npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0, false, 40000);
			else if (event.equalsIgnoreCase("2"))
				Quest.addSpawn(DETRACTOR2, npc.getX() + 100, npc.getY() - 100, npc.getZ(), 0, false, 40000);
			else if (event.equalsIgnoreCase("3"))
				Quest.addSpawn(DETRACTOR1, npc.getX() - 100, npc.getY() + 100, npc.getZ(), 0, false, 40000);
			else if (event.equalsIgnoreCase("4"))
				Quest.addSpawn(DETRACTOR2, npc.getX() - 100, npc.getY() - 100, npc.getZ(), 0, false, 40000);
			else if (event.equalsIgnoreCase("fafurion_despawn")) // Fafurion Kindred disappears and drops reward
			{
				cancelQuestTimer("fafurion_poison", npc, null);
				cancelQuestTimer("1", npc, null);
				cancelQuestTimer("2", npc, null);
				cancelQuestTimer("3", npc, null);
				cancelQuestTimer("4", npc, null);

				myTrackingSet.remove(npc.getObjectId());
				player = _idmap.remove(npc.getObjectId());
				if (player != null) // You never know ...
					((L2Attackable) npc).doItemDrop(NpcTable.getInstance().getTemplate(18485), player);

				npc.deleteMe();
			}
			else if (event.equalsIgnoreCase("fafurion_poison")) // Reduces Fafurions hp like it is poisoned
			{
				if (npc.getCurrentHp() <= 500)
				{
					cancelQuestTimer("fafurion_despawn", npc, null);
					cancelQuestTimer("first_spawn", npc, null);
					cancelQuestTimer("second_spawn", npc, null);
					cancelQuestTimer("third_spawn", npc, null);
					cancelQuestTimer("fourth_spawn", npc, null);
					cancelQuestTimer("1", npc, null);
					cancelQuestTimer("2", npc, null);
					cancelQuestTimer("3", npc, null);
					cancelQuestTimer("4", npc, null);
					myTrackingSet.remove(npc.getObjectId());
					_idmap.remove(npc.getObjectId());
				}
				npc.reduceCurrentHp(500, npc, null); // poison kills Fafurion if he is not healed
			}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int npcObjId = npc.getObjectId();
		if (npcId == DRAGON)
			if (!myTrackingSet.contains(npcObjId)) // this allows to handle multiple instances of npc
			{
				myTrackingSet.add(npcObjId);
				// Spawn first 5 shades on first attack on Dark Water Dragon
				final L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
				spawnShade(originalAttacker, SHADE1, npc.getX() + 100, npc.getY() + 100, npc.getZ());
				spawnShade(originalAttacker, SHADE2, npc.getX() + 100, npc.getY() - 100, npc.getZ());
				spawnShade(originalAttacker, SHADE1, npc.getX() - 100, npc.getY() + 100, npc.getZ());
				spawnShade(originalAttacker, SHADE2, npc.getX() - 100, npc.getY() - 100, npc.getZ());
				spawnShade(originalAttacker, SHADE1, npc.getX() - 150, npc.getY() + 150, npc.getZ());
			}
			else if (npc.getCurrentHp() < npc.getMaxHp() / 2.0 && !secondSpawn.contains(npcObjId))
			{
				secondSpawn.add(npcObjId);
				// Spawn second 5 shades on half hp of on Dark Water Dragon
				final L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
				spawnShade(originalAttacker, SHADE2, npc.getX() + 100, npc.getY() + 100, npc.getZ());
				spawnShade(originalAttacker, SHADE1, npc.getX() + 100, npc.getY() - 100, npc.getZ());
				spawnShade(originalAttacker, SHADE2, npc.getX() - 100, npc.getY() + 100, npc.getZ());
				spawnShade(originalAttacker, SHADE1, npc.getX() - 100, npc.getY() - 100, npc.getZ());
				spawnShade(originalAttacker, SHADE2, npc.getX() - 150, npc.getY() + 150, npc.getZ());
			}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int npcObjId = npc.getObjectId();
		if (npcId == DRAGON)
		{
			myTrackingSet.remove(npcObjId);
			secondSpawn.remove(npcObjId);
			final L2Attackable faf = (L2Attackable) Quest.addSpawn(FAFURION, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0); // spawns Fafurion Kindred when Dard Water Dragon is dead
			_idmap.put(faf.getObjectId(), killer);
		}
		else if (npcId == FAFURION)
		{
			cancelQuestTimer("fafurion_poison", npc, null);
			cancelQuestTimer("fafurion_despawn", npc, null);
			cancelQuestTimer("first_spawn", npc, null);
			cancelQuestTimer("second_spawn", npc, null);
			cancelQuestTimer("third_spawn", npc, null);
			cancelQuestTimer("fourth_spawn", npc, null);
			cancelQuestTimer("1", npc, null);
			cancelQuestTimer("2", npc, null);
			cancelQuestTimer("3", npc, null);
			cancelQuestTimer("4", npc, null);
			myTrackingSet.remove(npcObjId);
			_idmap.remove(npcObjId);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		final int npcId = npc.getId();
		final int npcObjId = npc.getObjectId();
		if (npcId == FAFURION)
			if (!myTrackingSet.contains(npcObjId))
			{
				myTrackingSet.add(npcObjId);
				// Spawn 4 Detractors on spawn of Fafurion
				final int x = npc.getX();
				final int y = npc.getY();
				Quest.addSpawn(DETRACTOR2, x + 100, y + 100, npc.getZ(), 0, false, 40000);
				Quest.addSpawn(DETRACTOR1, x + 100, y - 100, npc.getZ(), 0, false, 40000);
				Quest.addSpawn(DETRACTOR2, x - 100, y + 100, npc.getZ(), 0, false, 40000);
				Quest.addSpawn(DETRACTOR1, x - 100, y - 100, npc.getZ(), 0, false, 40000);
				this.startQuestTimer("first_spawn", 2000, npc, null); // timer to delay timer "1"
				this.startQuestTimer("second_spawn", 4000, npc, null); // timer to delay timer "2"
				this.startQuestTimer("third_spawn", 8000, npc, null); // timer to delay timer "3"
				this.startQuestTimer("fourth_spawn", 10000, npc, null); // timer to delay timer "4"
				this.startQuestTimer("fafurion_poison", 3000, npc, null, true); // Every three seconds reduces Fafurions hp like it is poisoned
				this.startQuestTimer("fafurion_despawn", 120000, npc, null); // Fafurion Kindred disappears after two minutes
			}
		return super.onSpawn(npc);
	}

	public void spawnShade(final L2Character attacker, final int npcId, final int x, final int y, final int z)
	{
		final L2Npc shade = addSpawn(npcId, x, y, z, 0, false, 0);
		shade.setRunning();
		((L2Attackable) shade).addDamageHate(attacker, 0, 999);
		shade.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
	}

	public static void main(final String[] args)
	{
		// Quest class and state definition
		new DarkWaterDragon(-1, DarkWaterDragon.class.getSimpleName(), "ai/group_template");
	}
}