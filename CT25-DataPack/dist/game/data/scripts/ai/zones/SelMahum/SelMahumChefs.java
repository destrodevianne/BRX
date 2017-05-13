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
package ai.zones.SelMahum;

import java.io.File;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.Config;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.MoveToLocation;
import ct25.xtreme.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastMap;

/**
 * @author Probe, InsOmnia updates by @Browser
 */
public class SelMahumChefs extends L2AttackableAIScript
{
	// NPCs
	private static final int SELMAHUM_CHEF = 18908;
	private static final int SELMAHUM_ESCORT_GUARD = 22779;
	protected static final int[] SELMAHUM_SQUAD_LEADERS = new int[]
	{
		22786,
		22787,
		22788
	};

	// FIREPLACE NPCs
	private static final int CAMP_FIRE = 18927;
	private static final int FIRE_FEED = 18933;

	// FIREPLACE SKILLs
	private static final int SKILL_TIRED = 6331;
	private static final int SKILL_FULL = 6332;

	// CHEF STRINGs
	protected static final String[] CHEF_FSTRINGS =
	{
		"I brought the food.",
		"Come and eat."
	};
	
	// MAHUM DYSPLAY EFFECTs
	private static final int MAHUM_EFFECT_EAT = 1;
	private static final int MAHUM_EFFECT_SLEEP = 2;

	protected static final FastMap<Integer, ChefGroup> chefGroups = new FastMap<>();
	protected static final TIntObjectHashMap<Location[]> escortSpawns = new TIntObjectHashMap<>();
	protected static final ConcurrentHashMap<L2Npc, Integer> fireplaces = new ConcurrentHashMap<>();
	protected static final ConcurrentHashMap<L2Npc, L2Npc> fireplacesFeed = new ConcurrentHashMap<>();
	
	private class ChefGroup
	{
		public final int id;
		public L2Npc chef;
		public L2Npc[] escorts;
		public int currentPoint = 0;
		public boolean atFirePlace = false;
		public int lastFirePlaceId = 0;
		public AtomicLong lastInvincible = new AtomicLong();
		public boolean reverseDirection = false;
		public TreeMap<Integer, Location> pathPoints;
		
		public ChefGroup(final int id)
		{
			this.id = id;
			lastInvincible.set(0);
		}
	}
	
	/**
	 * This task handles the walking part of the Sel Mahum Chefs based on predefined path points parsed from xml.
	 */
	protected class WalkTask implements Runnable
	{
		@Override
		public void run()
		{
			for (final int groupId : chefGroups.keySet())
			{
				final ChefGroup group = chefGroups.get(groupId);
				if (group.chef.isInCombat() || group.chef.isDead() || group.chef.isMoving() || group.atFirePlace)
				{
					if (group.chef.isMoving())
					{
						final MoveToLocation mov = new MoveToLocation(group.chef);
						group.chef.broadcastPacket(mov);
					}
					continue;
				}
				if (doFireplace(group))
					continue;

				group.currentPoint = getNextPoint(group, group.currentPoint);
				final Location loc = group.pathPoints.get(group.currentPoint);
				final int nextPathPoint = getNextPoint(group, group.currentPoint);
				loc.setHeading(calculateHeading(loc, group.pathPoints.get(nextPathPoint)));
				group.chef.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading()));
			}
		}
	}
	
	protected class RangeCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			for (final int groupId : chefGroups.keySet())
			{
				final ChefGroup group = chefGroups.get(groupId);
				if (group.chef.isInCombat() || group.chef.isDead())
					continue;
				for (final L2Npc escort : group.escorts)
				{
					if (escort == null || escort.isDead())
						continue;
					if (Util.checkIfInRange(150, escort, group.chef, false))
						escort.setWalking();
					else
						escort.setRunning();
					if (!escort.getAI().getIntention().equals(CtrlIntention.AI_INTENTION_FOLLOW))
						escort.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, group.chef);
					final MoveToLocation mov1 = new MoveToLocation(escort);
					escort.broadcastPacket(mov1);
				}
			}
		}
	}

	private class FireplaceTask implements Runnable
	{
		private final ChefGroup group;
		private L2Npc fireplace;
		
		protected FireplaceTask(final ChefGroup group, final L2Npc fireplace)
		{
			this.group = group;
			this.fireplace = fireplace;
		}

		@Override
		public void run()
		{
			if (fireplace.getDisplayEffect() == 0 && fireplacesFeed.containsKey(fireplace))
			{
				fireplacesFeed.get(fireplace).deleteMe();
				fireplacesFeed.remove(fireplace);
			}
			else if (fireplace.getDisplayEffect() == 0)
			{
				fireplace.setDisplayEffect(1);
				for (final L2Character leader : group.chef.getKnownList().getKnownCharactersInRadius(1000))
					if (leader instanceof L2MonsterInstance)
						if (Util.contains(SELMAHUM_SQUAD_LEADERS, ((L2MonsterInstance) leader).getId()))
							if (!leader.isInCombat() && !leader.isDead() && leader.getFirstEffect(SKILL_TIRED) == null && Util.calculateDistance(fireplace, leader, true) > 300)
							{
								final int rndX = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								final int rndY = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								final Location fireplaceLoc = new Location(fireplace.getX(), fireplace.getY(), fireplace.getZ());
								final Location leaderLoc = new Location(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ());
								final L2CharPosition position = new L2CharPosition(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ(), calculateHeading(leaderLoc, fireplaceLoc));
								leader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, position);
								ThreadPoolManager.getInstance().scheduleAi(new MoveToFireplace((L2MonsterInstance) leader, 1), 100);
							}
			}
			else if (fireplace.getDisplayEffect() == 1 && !fireplacesFeed.containsKey(fireplace))
			{
				final L2Npc feed = addSpawn(FIRE_FEED, fireplace.getX(), fireplace.getY(), fireplace.getZ(), 0, false, 0, false);
				feed.setHideName(true);
				fireplacesFeed.put(fireplace, feed);
				for (final L2Character leader : group.chef.getKnownList().getKnownCharactersInRadius(1500))
					if (leader instanceof L2MonsterInstance)
						if (Util.contains(SELMAHUM_SQUAD_LEADERS, ((L2MonsterInstance) leader).getId()))
							if (!leader.isInCombat() && !leader.isDead() && leader.getFirstEffect(SKILL_FULL) == null && Util.calculateDistance(fireplace, leader, true) > 300)
							{
								final int rndX = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								final int rndY = getRandom(100) < 50 ? -getRandom(50, 100) : getRandom(50, 100);
								final Location fireplaceLoc = new Location(fireplace.getX(), fireplace.getY(), fireplace.getZ());
								final Location leaderLoc = new Location(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ());
								final L2CharPosition position = new L2CharPosition(fireplace.getX() + rndX, fireplace.getY() + rndY, fireplace.getZ(), calculateHeading(leaderLoc, fireplaceLoc));
								leader.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, position);
								ThreadPoolManager.getInstance().scheduleAi(new MoveToFireplace((L2MonsterInstance) leader, 1), 100);
							}
			}
			else if (fireplace.getDisplayEffect() == 1 && fireplacesFeed.containsKey(fireplace))
			{
				final L2Npc feed = fireplacesFeed.get(fireplace);
				fireplacesFeed.remove(fireplace);
				fireplaces.remove(fireplace);
				final L2Npc fire = addSpawn(CAMP_FIRE, fireplace);
				fire.setHideName(true);
				fire.setDisplayEffect(0);
				fireplace.deleteMe();
				fireplaces.put(fire, 1);
				fireplacesFeed.put(fire, feed);
				fireplace = fire;
				fireplace.setDisplayEffect(0);
			}
			group.lastFirePlaceId = fireplace.getObjectId();
			ThreadPoolManager.getInstance().scheduleAi(new MoveChefFromFireplace(group, fireplace), 10000);
		}
	}

	private class MoveChefFromFireplace implements Runnable
	{
		private final ChefGroup group;
		private final L2Npc fire;
		
		protected MoveChefFromFireplace(final ChefGroup group, final L2Npc fire)
		{
			this.group = group;
			this.fire = fire;
		}

		@Override
		public void run()
		{
			group.atFirePlace = false;
			fireplaces.replace(fire, 0);
		}
	}

	private class MoveToFireplace implements Runnable
	{
		private final L2MonsterInstance mob;
		private final int type;
		
		protected MoveToFireplace(final L2MonsterInstance mob, final int type)
		{
			this.mob = mob;
			this.type = type;
		}

		@Override
		public void run()
		{
			if (mob.isMoving())
				ThreadPoolManager.getInstance().scheduleAi(new MoveToFireplace(mob, type), 1000);
			else if (!mob.isInCombat() && !mob.isDead())
			{
				if (type == 0)
				{
					SkillTable.getInstance().getInfo(SKILL_TIRED, 1).getEffects(mob, mob);
					mob.setDisplayEffect(MAHUM_EFFECT_SLEEP);
				}
				else if (type == 1)
				{
					SkillTable.getInstance().getInfo(SKILL_FULL, 1).getEffects(mob, mob);
					broadcastNpcSay(mob, Say2.NPC_ALL, CHEF_FSTRINGS[getRandom(2)]);
					mob.setDisplayEffect(MAHUM_EFFECT_EAT);
				}
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
				mob.setIsNoRndWalk(true);
				ThreadPoolManager.getInstance().scheduleAi(new ReturnFromFireplace(mob), 300000);
			}
		}
	}

	private class ReturnFromFireplace implements Runnable
	{
		private final L2MonsterInstance mob;
		
		protected ReturnFromFireplace(final L2MonsterInstance mob)
		{
			this.mob = mob;
		}

		@Override
		public void run()
		{
			if (mob != null && !mob.isInCombat() && !mob.isDead())
				if (mob.getFirstEffect(SKILL_FULL) == null && mob.getFirstEffect(SKILL_TIRED) == null)
				{
					mob.setIsNoRndWalk(false);
					mob.setDisplayEffect(3);
					mob.setCanReturnToSpawnPoint(false);
				}
				else
					ThreadPoolManager.getInstance().scheduleAi(new ReturnFromFireplace(mob), 30000);
		}
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (npc.getId() == SELMAHUM_CHEF)
		{
			final ChefGroup group = getChefGroup(npc);
			if (group == null)
				return null;
			final Location[] spawns = escortSpawns.get(group.id);
			for (int i = 0; i < 2; i++)
			{
				group.escorts[i] = addSpawn(SELMAHUM_ESCORT_GUARD, spawns[i].getX(), spawns[i].getY(), spawns[i].getZ(), spawns[i].getHeading(), false, 0);
				group.escorts[i].getSpawn().stopRespawn();
				group.escorts[i].setIsNoRndWalk(true);
				group.escorts[i].setWalking();
				group.escorts[i].getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, group.chef);
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		switch (event)
		{
			case "chef_disable_reward":
			{
				npc.getVariables().set("reward_time_gone", 1);
				break;
			}
			case "reset_full_bottle_prize":
			{
				npc.getVariables().remove("full_barrel_rewarding_player");
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onEventReceived(final String eventName, final L2Npc sender, final L2Npc receiver, final L2Object reference)
	{
		switch (eventName)
		{
			case "sce_soup_failure":
			{
				if (Util.contains(SELMAHUM_SQUAD_LEADERS, receiver.getId()))
				{
					receiver.getVariables().set("full_barrel_rewarding_player", reference.getObjectId());
					startQuestTimer("reset_full_bottle_prize", 180000, receiver, null);
				}
				break;
			}
		}

		return null;
	}

	@Override
	public final String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.getId() == SELMAHUM_CHEF)
		{
			final ChefGroup group = getChefGroup(npc);
			if (group.lastInvincible.get() < System.currentTimeMillis() && npc.getCurrentHp() / npc.getMaxHp() * 100 < 50)
			{
				group.lastInvincible.set(System.currentTimeMillis() + 600000);
				SkillTable.getInstance().getInfo(5989, 1).getEffects(npc, npc);
				startQuestTimer("chef_disable_reward", 60000, npc, null);
			}
			else if (npc.getFirstEffect(5989) != null)
				// handling it manually to avoid interupting
				if (group.chef.getTarget() != null && group.chef.getTarget().equals(attacker) && attacker.getCurrentHp() / attacker.getMaxHp() * 100 < 90)
					if (!npc.isCastingNow())
						npc.doCast(SkillTable.getInstance().getInfo(6330, 1));
			for (final L2Npc escort : group.escorts)
				if (!escort.isInCombat())
				{
					escort.setRunning();
					((L2Attackable) escort).addDamageHate(attacker, 0, 500);
					escort.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
				}
		}
		else if (npc.getId() == SELMAHUM_ESCORT_GUARD)
		{
			final ChefGroup group = getChefGroup(npc);
			if (group != null && !group.chef.isDead() && !group.chef.isInCombat())
			{
				group.chef.setRunning();
				((L2Attackable) group.chef).addDamageHate(attacker, 0, 500);
				group.chef.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
			if (group != null && group.escorts != null)
				for (final L2Npc escort : group.escorts)
					if (!escort.isInCombat())
					{
						escort.setRunning();
						((L2Attackable) escort).addDamageHate(attacker, 0, 500);
						escort.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
					}
		}
		return null;
	}
	
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.isMonster() && npc.getVariables().getInt("reward_time_gone") == 0)
			((L2MonsterInstance) npc).dropItem(killer, 15492, 1);
		cancelQuestTimer("chef_disable_reward", npc, null);

		if (npc.getId() == SELMAHUM_CHEF)
		{
			final ChefGroup group = getChefGroup(npc);
			for (final L2Npc escort : group.escorts)
				if (escort != null && !npc.isDead())
					escort.deleteMe();
		}
		return null;
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isSummon)
	{
		if (npc.getId() == FIRE_FEED && skill.getId() == 9075 && Util.contains(targets, npc))
		{
			npc.doCast(SkillTable.getInstance().getInfo(6688, 1));
			npc.broadcastEvent("sce_soup_failure", 600, caster);
		}

		return null;
	}

	protected boolean doFireplace(final ChefGroup group)
	{
		if (!group.atFirePlace)
		{
			for (final L2Npc fire : fireplaces.keySet())
				if (Util.calculateDistance(group.chef, fire, true) < 400 && fire.getObjectId() != group.lastFirePlaceId && fireplaces.get(fire) == 0)
				{
					group.atFirePlace = true;
					final int xDiff = group.chef.getX() - fire.getX() > 0 ? -getRandom(30, 40) : getRandom(30, 40);
					final int yDiff = group.chef.getY() - fire.getY() > 0 ? -getRandom(30, 40) : getRandom(30, 40);
					group.chef.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(fire.getX() - xDiff, fire.getY() - yDiff, fire.getZ(), calculateHeading(group.chef, fire)));
					fireplaces.replace(fire, 1);
					ThreadPoolManager.getInstance().scheduleAi(new FireplaceTask(group, fire), 1000);
					break;
				}
			if (group.atFirePlace)
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the ChefGroup the given npc belongs to.
	 * @param npc
	 * @return null if no group found
	 */
	private ChefGroup getChefGroup(final L2Npc npc)
	{
		if (npc == null || npc.getId() != SELMAHUM_CHEF && npc.getId() != SELMAHUM_ESCORT_GUARD)
			return null;
		for (final ChefGroup group : chefGroups.values())
		{
			if (npc.getId() == SELMAHUM_CHEF && npc.equals(group.chef))
				return group;
			if (npc.getId() == SELMAHUM_ESCORT_GUARD)
				for (final L2Npc escort : group.escorts)
					if (npc.equals(escort))
						return group;
		}
		return null;
	}
	
	/**
	 * Returns the next path point the Sel Mahum Chef needs to walk to
	 * @param group
	 * @param currentPoint
	 * @return
	 */
	protected int getNextPoint(final ChefGroup group, final int currentPoint)
	{
		if (group.pathPoints.lastKey().intValue() == currentPoint)
			group.reverseDirection = true;
		else if (group.pathPoints.firstKey().intValue() == currentPoint)
			group.reverseDirection = false;
		
		if (group.reverseDirection)
			return group.pathPoints.lowerKey(currentPoint);
		return group.pathPoints.higherKey(currentPoint);
	}

	/**
	 * Calculates the heading from one Location to another
	 * @param fromLoc
	 * @param toLoc
	 * @return
	 */
	protected int calculateHeading(final Location fromLoc, final Location toLoc)
	{
		return Util.calculateHeadingFrom(fromLoc.getX(), fromLoc.getY(), toLoc.getX(), toLoc.getY());
	}

	/**
	 * Calculates the heading from one char to another
	 * @param fromLoc
	 * @param toLoc
	 * @return
	 */
	protected int calculateHeading(final L2Character fromLoc, final L2Character toLoc)
	{
		return Util.calculateHeadingFrom(fromLoc.getX(), fromLoc.getY(), toLoc.getX(), toLoc.getY());
	}

	private void loadFireplaces()
	{
		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(CAMP_FIRE))
		{
			spawn.getLastSpawn().setDisplayEffect(0);
			fireplaces.put(spawn.getLastSpawn(), 0);
			spawn.getLastSpawn().setHideName(true);
		}
	}
	
	/**
	 * Spawn all Sel Mahum Chefs and their escorts
	 */
	private void initSpawns()
	{
		for (final Integer integer : chefGroups.keySet())
		{
			final int groupId = integer;
			final ChefGroup group = chefGroups.get(groupId);
			final Location spawn = group.pathPoints.firstEntry().getValue();
			group.chef = addSpawn(SELMAHUM_CHEF, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0);
			group.chef.getSpawn().setAmount(1);
			group.chef.getSpawn().startRespawn();
			group.chef.getSpawn().setRespawnDelay(60);
			group.chef.setWalking();
			group.escorts = new L2Npc[2];
			final Location[] spawns = escortSpawns.get(groupId);
			for (int i = 0; i < 2; i++)
			{
				group.escorts[i] = addSpawn(SELMAHUM_ESCORT_GUARD, spawns[i].getX(), spawns[i].getY(), spawns[i].getZ(), spawns[i].getHeading(), false, 0);
				group.escorts[i].getSpawn().stopRespawn();
				group.escorts[i].setIsNoRndWalk(true);
				group.escorts[i].setWalking();
				group.escorts[i].getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, group.chef);
			}
		}
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new WalkTask(), 3 * 60000, 2500);
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new RangeCheckTask(), 3 * 60000, 1000);
	}
	
	/**
	 * Calculates spawn locations for the two escorts, one behind the chef on the right, one behind the chef on the left. Since this is the same for every time chefs will respawn, spawn locations are stored in a map for future uses. (escorts do not respawn on their own, but only when chef respawns)
	 */
	private void calculateEscortSpawns()
	{
		for (final Integer integer : chefGroups.keySet())
		{
			final int groupId = integer;
			final ChefGroup group = chefGroups.get(groupId);
			final Location loc = group.pathPoints.firstEntry().getValue();
			double chefAngle = Util.convertHeadingToDegree(loc.getHeading());
			chefAngle = chefAngle + 180;
			if (chefAngle > 359)
				chefAngle -= 360;
			final int xDirection = chefAngle <= 90 || chefAngle >= 270 ? 1 : -1;
			final int yDirection = chefAngle >= 180 && chefAngle < 360 ? -1 : 1;
			final Location[] spawnLocs = new Location[2];
			spawnLocs[0] = new Location();
			spawnLocs[0].setX(loc.getX() + xDirection * (int) Math.sin(45) * 100);
			spawnLocs[0].setY(loc.getY() + yDirection * (int) Math.cos(45) * 100);
			spawnLocs[0].setZ(loc.getZ());
			spawnLocs[0].setHeading(loc.getHeading());
			spawnLocs[1] = new Location();
			spawnLocs[1].setX(loc.getX() - xDirection * (int) Math.sin(45) * 100);
			spawnLocs[1].setY(loc.getY() + yDirection * (int) Math.cos(45) * 100);
			spawnLocs[1].setZ(loc.getZ());
			spawnLocs[1].setHeading(loc.getHeading());
			escortSpawns.put(groupId, spawnLocs);
		}
	}
	
	/**
	 * Load all Sel Mahum Chef path points from XML
	 */
	private void init()
	{
		final File f = new File(Config.DATAPACK_ROOT, "data/spawnZones/chefs.xml");
		if (!f.exists())
		{
			_log.warning("SelMahumChefs: Error! cooks.xml file is missing!");
			return;
		}
		
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setValidating(true);
			final Document doc = factory.newDocumentBuilder().parse(f);
			
			for (Node n = doc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling())
				if ("chef".equalsIgnoreCase(n.getNodeName()))
				{
					final int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
					final ChefGroup group = new ChefGroup(id);
					group.pathPoints = new TreeMap<>();
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if ("pathPoint".equalsIgnoreCase(d.getNodeName()))
						{
							final int order = Integer.parseInt(d.getAttributes().getNamedItem("order").getNodeValue());
							final int x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
							final int y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
							final int z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
							final Location loc = new Location(x, y, z, 0);
							group.pathPoints.put(order, loc);
						}
					chefGroups.put(id, group);
				}
		}
		catch (final Exception e)
		{
			_log.warning("SelMahumChefs: Error while loading. " + e);
		}
		
		_log.info("SelMahumChefs: Loaded " + chefGroups.size() + " chef groups.");
		calculateEscortSpawns();
		loadFireplaces();
		initSpawns();
	}
	
	public SelMahumChefs(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		final int[] mobs = new int[]
		{
			SELMAHUM_CHEF,
			SELMAHUM_ESCORT_GUARD
		};
		registerMobs(mobs, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
		addSpawnId(SELMAHUM_CHEF);
		addSkillSeeId(FIRE_FEED);
		addFirstTalkId(CAMP_FIRE, FIRE_FEED);
		addEventReceivedId(SELMAHUM_SQUAD_LEADERS);

		init();
	}
	
	public static void main(final String[] args)
	{
		new SelMahumChefs(-1, SelMahumChefs.class.getSimpleName(), "ai/zones/SelMahum");
	}
}