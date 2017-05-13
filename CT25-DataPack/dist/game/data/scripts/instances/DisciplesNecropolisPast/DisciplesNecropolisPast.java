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
package instances.DisciplesNecropolisPast;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.util.Util;
import javolution.util.FastList;
import quests.Q00196_SevenSignsSealOfTheEmperor.Q00196_SevenSignsSealOfTheEmperor;

/**
 * Disciple's Necropolis Past instance zone.
 * @author Adry_85 Revised, updates and Rework: Browser
 */
public final class DisciplesNecropolisPast extends Quest
{
	protected class DNPWorld extends InstanceWorld
	{
		protected final FastList<L2Npc> anakimGroup = new FastList<>();
		protected final FastList<L2Npc> lilithGroup = new FastList<>();
		protected long storeTime = 0;
		protected int countKill = 0;
	}

	// Instance
	private static final int INSTANCEID = 112;

	// NPCs
	private static final int SEAL_DEVICE = 27384;
	private static final int PROMISE_OF_MAMMON = 32585;
	private static final int SHUNAIMAN = 32586;
	private static final int LEON = 32587;
	private static final int DISCIPLES_GATEKEEPER = 32657;
	private static final int LILITH = 32715;
	private static final int LILITHS_STEWARD = 32716;
	private static final int LILITHS_ELITE = 32717;
	private static final int ANAKIM = 32718;
	private static final int ANAKIMS_GUARDIAN = 32719;
	private static final int ANAKIMS_GUARD = 32720;
	private static final int ANAKIMS_EXECUTOR = 32721;

	// Doors
	private static final int DOOR_1 = 17240102;
	private static final int DOOR_2 = 17240104;
	private static final int DOOR_3 = 17240106;
	private static final int DOOR_4 = 17240108;
	private static final int DOOR_5 = 17240110;
	private static final int DISCIPLES_NECROPOLIS_DOOR = 17240111;

	// Items
	private static final int SACRED_SWORD_OF_EINHASAD = 15310;
	private static final int SEAL_OF_BINDING = 13846;

	// Locations
	private static final int[] ENTER =
	{
		-89559,
		216030,
		-7488
	};

	// Monsters
	private static final int LILIM_BUTCHER = 27371;
	private static final int LILIM_MAGUS = 27372;
	private static final int LILIM_KNIGHT_ERRANT = 27373;
	private static final int SHILENS_EVIL_THOUGHTS1 = 27374;
	private static final int SHILENS_EVIL_THOUGHTS2 = 27375;
	private static final int LILIM_KNIGHT = 27376;
	private static final int LILIM_SLAYER = 27377;
	private static final int LILIM_GREAT_MAGUS = 27378;
	private static final int LILIM_GUARD_KNIGHT = 27379;

	// StringId
	private static final int[] LILITH_SHOUT =
	{
		19616, // How dare you try to contend against me in strength? Ridiculous.
		19617, // Anakim! In the name of Great Shilien, I will cut your throat!
		19618 // You cannot be the match of Lilith. I'll teach you a lesson!
	};

	// Bosses Spawn
	private static final Map<Integer, Location> LILITH_SPAWN = new HashMap<>();
	private static final Map<Integer, Location> ANAKIM_SPAWN = new HashMap<>();
	static
	{
		LILITH_SPAWN.put(LILITH, new Location(-83175, 217021, -7504, 49151));
		LILITH_SPAWN.put(LILITHS_STEWARD, new Location(-83327, 216938, -7492, 50768));
		LILITH_SPAWN.put(LILITHS_ELITE, new Location(-83003, 216909, -7492, 4827));
		ANAKIM_SPAWN.put(ANAKIM, new Location(-83179, 216479, -7504, 16384));
		ANAKIM_SPAWN.put(ANAKIMS_GUARDIAN, new Location(-83321, 216507, -7492, 16166));
		ANAKIM_SPAWN.put(ANAKIMS_GUARD, new Location(-83086, 216519, -7495, 15910));
		ANAKIM_SPAWN.put(ANAKIMS_EXECUTOR, new Location(-83031, 216604, -7492, 17071));
	}

	// Skills
	private static final SkillHolder SEAL_ISOLATION = new SkillHolder(5980, 3);
	private static final Map<Integer, SkillHolder> SKILLS = new HashMap<>();
	static
	{
		SKILLS.put(32715, new SkillHolder(6187, 1)); // Presentation - Lilith Battle
		SKILLS.put(32716, new SkillHolder(6188, 1)); // Presentation - Lilith's Steward Battle1
		SKILLS.put(32717, new SkillHolder(6190, 1)); // Presentation - Lilith's Bodyguards Battle1
		SKILLS.put(32718, new SkillHolder(6191, 1)); // Presentation - Anakim Battle
		SKILLS.put(32719, new SkillHolder(6192, 1)); // Presentation - Anakim's Guardian Battle1
		SKILLS.put(32720, new SkillHolder(6194, 1)); // Presentation - Anakim's Guard Battle
		SKILLS.put(32721, new SkillHolder(6195, 1)); // Presentation - Anakim's Executor Battle
	}

	private DisciplesNecropolisPast()
	{
		super(-1, DisciplesNecropolisPast.class.getSimpleName(), "instances");
		addAttackId(SEAL_DEVICE);
		addFirstTalkId(SHUNAIMAN, LEON, DISCIPLES_GATEKEEPER);
		addKillId(LILIM_BUTCHER, LILIM_MAGUS, LILIM_KNIGHT_ERRANT, LILIM_KNIGHT, SHILENS_EVIL_THOUGHTS1, SHILENS_EVIL_THOUGHTS2, LILIM_SLAYER, LILIM_GREAT_MAGUS, LILIM_GUARD_KNIGHT);
		addAggroRangeEnterId(LILIM_BUTCHER, LILIM_MAGUS, LILIM_KNIGHT_ERRANT, LILIM_KNIGHT, SHILENS_EVIL_THOUGHTS1, SHILENS_EVIL_THOUGHTS2, LILIM_SLAYER, LILIM_GREAT_MAGUS, LILIM_GUARD_KNIGHT);
		addSpawnId(SEAL_DEVICE);
		addStartNpc(PROMISE_OF_MAMMON);
		addTalkId(PROMISE_OF_MAMMON, SHUNAIMAN, LEON, DISCIPLES_GATEKEEPER);
	}

	protected void spawnNPC(final DNPWorld world)
	{
		for (final Map.Entry<Integer, Location> entry : LILITH_SPAWN.entrySet())
		{
			final L2Npc npc = addSpawn(entry.getKey(), entry.getValue(), false, 0, false, world.instanceId);
			world.lilithGroup.add(npc);
		}
		for (final Map.Entry<Integer, Location> entry : ANAKIM_SPAWN.entrySet())
		{
			final L2Npc enpc = addSpawn(entry.getKey(), entry.getValue(), false, 0, false, world.instanceId);
			world.anakimGroup.add(enpc);
		}
	}

	private synchronized void checkDoors(final L2Npc npc, final DNPWorld world)
	{
		if (world.countKill == 4)
			openDoor(DOOR_1, world.instanceId);
		else if (world.countKill == 10)
			openDoor(DOOR_2, world.instanceId);
		else if (world.countKill == 18)
			openDoor(DOOR_3, world.instanceId);
		else if (world.countKill == 28)
			openDoor(DOOR_4, world.instanceId);
		else if (world.countKill == 40)
			openDoor(DOOR_5, world.instanceId);
	}

	private static void teleportPlayer(final L2PcInstance player, final int[] coords, final int instanceId)
	{
		removeBuffs(player);
		if (player.getPet() != null)
			removeBuffs(player.getPet());
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private synchronized void enterInstance(final L2PcInstance player, final String template)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (world.templateId != INSTANCEID)
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
		}
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("DisciplesNecropolisPast.xml");
			// New instance
			world = new DNPWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCEID;
			InstanceManager.getInstance().addWorld(world);
			((DNPWorld) world).storeTime = System.currentTimeMillis();
			_log.info("Disciple's Necropolis Past started " + world.templateId + " Instance: " + instanceId + " created by player: " + player.getName());
			spawnNPC((DNPWorld) world);
			world.allowed.add(player.getObjectId());
			// teleport players
			teleportPlayer(player, ENTER, instanceId);
		}
	}

	protected void exitInstance(final L2PcInstance player)
	{
		player.setInstanceId(0);
		player.teleToLocation(171782, -17612, -4901);
	}

	private void makeCast(final L2Npc npc, final FastList<L2Npc> targets)
	{
		npc.setTarget(targets.get(getRandom(targets.size())));
		if (SKILLS.containsKey(npc.getId()))
			npc.doCast(SKILLS.get(npc.getId()).getSkill());
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof DNPWorld)
		{
			final DNPWorld world = (DNPWorld) tmpworld;
			switch (event)
			{
				case "FINISH":
				{
					if (getQuestItemsCount(player, SEAL_OF_BINDING) >= 4)
					{
						player.showQuestMovie(13);
						startQuestTimer("TELEPORT", 27000, null, player);

						// destroy instance after 5 min
						final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
						inst.setDuration(5 * 60000);
						inst.setEmptyDestroyTime(0);
					}
					break;
				}
				case "TELEPORT":
				{
					player.teleToLocation(-89559, 216030, -7488);
					break;
				}
				case "FIGHT":
				{
					for (final L2Npc caster : world.anakimGroup)
					{
						if (caster != null && !caster.isCastingNow())
							makeCast(caster, world.lilithGroup);
						if (caster != null && caster.getId() == ANAKIM)
							if (caster.isScriptValue(0))
							{
								caster.broadcastPacket(new NpcSay(caster.getObjectId(), Say2.NPC_SHOUT, caster.getId(), 19615)); // You, such a fool! The victory over this war belongs to Shilien!!!
								caster.setScriptValue(1);
							}
							else if (getRandom(100) < 10)
								caster.broadcastPacket(new NpcSay(caster.getObjectId(), Say2.NPC_SHOUT, caster.getId(), LILITH_SHOUT[getRandom(3)]));
					}
					for (final L2Npc caster : world.lilithGroup)
					{
						if (caster != null && !caster.isCastingNow())
							makeCast(caster, world.anakimGroup);
						if (caster != null && caster.getId() == 32715)
							if (caster.isScriptValue(0))
							{
								caster.broadcastPacket(new NpcSay(caster.getObjectId(), Say2.NPC_SHOUT, caster.getId(), 19606)); // For the eternity of Einhasad!!!
								if (Util.checkIfInRange(2000, caster, player, true))
									player.sendPacket(new NpcSay(caster.getObjectId(), Say2.TELL, caster.getId(), 19611)); // My power's weakening.. Hurry and turn on the sealing device!!!
								caster.setScriptValue(1);
							}
							else if (getRandom(100) < 10)
								switch (getRandom(3))
								{
									case 0:
									{
										caster.broadcastPacket(new NpcSay(caster.getObjectId(), Say2.NPC_SHOUT, caster.getId(), 19607)); // Dear Shillien's offsprings! You are not capable of confronting us!
										if (Util.checkIfInRange(2000, caster, player, true))
											player.sendPacket(new NpcSay(caster.getObjectId(), Say2.TELL, caster.getId(), 19612)); // All 4 sealing devices must be turned on!!!
										break;
									}
									case 1:
									{
										caster.broadcastPacket(new NpcSay(caster.getObjectId(), Say2.NPC_SHOUT, caster.getId(), 19608)); // I'll show you the real power of Einhasad!
										if (Util.checkIfInRange(2000, caster, player, true))
											player.sendPacket(new NpcSay(caster.getObjectId(), Say2.TELL, caster.getId(), 19613)); // Lilith's attack is getting stronger! Go ahead and turn it on!
										break;
									}
									case 2:
									{
										caster.broadcastPacket(new NpcSay(caster.getObjectId(), Say2.NPC_SHOUT, caster.getId(), 19609)); // Dear Military Force of Light! Go destroy the offsprings of Shillien!!!
										if (Util.checkIfInRange(2000, caster, player, true))
										{
											final NpcSay ns = new NpcSay(caster.getObjectId(), Say2.TELL, caster.getId(), 19614); // ,Dear $s1, give me more strength.
											ns.addStringParameter(player.getName());
											caster.broadcastPacket(ns);
										}
										break;
									}
								}
						startQuestTimer("FIGHT", 1000, null, player);
					}
					break;
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		switch (npc.getId())
		{
			case LILIM_BUTCHER:
			case LILIM_GUARD_KNIGHT:
			{
				if (npc.isScriptValue(0))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1000268)); // This place once belonged to Lord Shilen.
					npc.setScriptValue(1);
				}
				break;
			}
			case LILIM_MAGUS:
			case LILIM_GREAT_MAGUS:
			{
				if (npc.isScriptValue(0))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1000208)); // Who dares enter this place?
					npc.setScriptValue(1);
				}
				break;
			}
			case LILIM_KNIGHT_ERRANT:
			case LILIM_KNIGHT:
			{
				if (npc.isScriptValue(0))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1000108)); // Those who are afraid should get away and those who are brave should fight!
					npc.setScriptValue(1);
				}
				break;
			}
			case LILIM_SLAYER:
			{
				if (npc.isScriptValue(0))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1000222)); // Leave now!
					npc.setScriptValue(1);
				}
				break;
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof DNPWorld)
		{
			if (npc.isScriptValue(0))
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.1)
				{
					giveItems(player, SEAL_OF_BINDING, 1);
					player.sendPacket(SystemMessageId.THE_SEALING_DEVICE_ACTIVATION_COMPLETE);
					npc.setScriptValue(1);
					startQuestTimer("FINISH", 1000, npc, player);
					cancelQuestTimer("FIGHT", npc, player);
				}
			if (getRandom(100) < 50)
				npc.doCast(SEAL_ISOLATION.getSkill());
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		switch (npc.getId())
		{
			case SHUNAIMAN:
			{
				return "32586.htm";
			}
			case LEON:
			{
				return "32587.htm";
			}
			case DISCIPLES_GATEKEEPER:
			{
				return "32657.htm";
			}
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (tmpworld instanceof DNPWorld)
		{
			final DNPWorld world = (DNPWorld) tmpworld;
			world.countKill++;
			checkDoors(npc, world);
		}

		switch (npc.getId())
		{
			case LILIM_MAGUS:
			case LILIM_GREAT_MAGUS:
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1000247)); // Lord Shilen... some day... you will accomplish... this mission...
				break;
			}
			case LILIM_KNIGHT_ERRANT:
			case LILIM_KNIGHT:
			case LILIM_GUARD_KNIGHT:
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 1000270)); // Why are you getting in our way?
				break;
			}
			case LILIM_SLAYER:
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), 8564)); // For Shilen!
				break;
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public final String onSpawn(final L2Npc npc)
	{
		npc.setIsMortal(false);
		npc.setIsImmobilized(true);
		return super.onSpawn(npc);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance talker)
	{
		final QuestState qs = talker.getQuestState(Q00196_SevenSignsSealOfTheEmperor.class.getSimpleName());
		String htmltext = getNoQuestMsg(talker);
		if (qs == null)
			return htmltext;

		switch (npc.getId())
		{
			case PROMISE_OF_MAMMON:
			{
				if (qs.isCond(3) || qs.isCond(4))
				{
					enterInstance(talker, "DisciplesNecropolisPast.xml");
					return "";
				}
				break;
			}
			case LEON:
			{
				if (qs.getCond() >= 3)
				{
					takeItems(talker, SACRED_SWORD_OF_EINHASAD, -1);
					final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(talker);
					world.allowed.remove(world.allowed.indexOf(talker.getObjectId()));
					exitInstance(talker);
					htmltext = "32587-01.html";
				}
				break;
			}
			case DISCIPLES_GATEKEEPER:
			{
				if (qs.getCond() >= 3)
				{
					final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
					if (tmpworld instanceof DNPWorld)
					{
						final DNPWorld world = (DNPWorld) tmpworld;
						openDoor(DISCIPLES_NECROPOLIS_DOOR, world.instanceId);
						talker.showQuestMovie(12);
						startQuestTimer("FIGHT", 1000, null, talker);
					}
				}
				break;
			}
		}
		return htmltext;
	}

	private static final void removeBuffs(final L2Character ch)
	{
		ch.stopAllEffectsExceptThoseThatLastThroughDeath();
		if (ch.hasPet())
			ch.getPet().stopAllEffectsExceptThoseThatLastThroughDeath();
	}

	public static void main(final String[] args)
	{
		new DisciplesNecropolisPast();
	}
}
