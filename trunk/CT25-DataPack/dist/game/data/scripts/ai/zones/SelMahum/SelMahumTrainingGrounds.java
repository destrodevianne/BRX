package ai.zones.SelMahum;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.ai.CtrlEvent;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.SocialAction;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;

/**
 * 
 * @author Probe
 *
 */
public class SelMahumTrainingGrounds extends L2AttackableAIScript
{
	private static Logger _log = Logger.getLogger(SelMahumTrainingGrounds.class.getName());
	
	private static final String qName = "SelMahumTrainingGrounds";

	// Monsters
	private static final int[] SELMAHUM_RECRUIT = new int[] { 22780, 22782, 22784 };
	private static final int[] SELMAHUM_SOLDIER = new int[] { 22783, 22785 };
	private static final int[] SELMAHUM_DRILL_SERGEANT = new int[] { 22775, 22778 };
	private static final int SELMAHUM_TRAINING_OFFICER = 22776;

	// Other constants
	private static final int RESPAWN_DELAY = 60;												// In seconds
	private static final int ANIMATION_INTERVAL = 30 * 1000;									// In milliseconds
	
	// Containers
	private static final TIntObjectHashMap<Camp> camps = new TIntObjectHashMap<Camp>();
	private static final TObjectIntHashMap<L2Npc> campIdByNpc = new TObjectIntHashMap<L2Npc>();

	private class Camp
	{
		public final int id;
		public final List<SpawnData> spawns;
		public L2Npc officer;
		public ArrayList<L2Npc> recruits;

		public Camp(final int id, final List<SpawnData> spawns)
		{
			this.id = id;
			this.spawns = spawns;
			recruits = new ArrayList<L2Npc>();
		}
	}

	private class SpawnData
	{
		public final int npcId;
		public final Location spawnLoc;

		public SpawnData(final int npcId, final Location spawnLoc)
		{
			this.npcId = npcId;
			this.spawnLoc = spawnLoc;
		}
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final int npcId = npc.getNpcId();

		// Handle recruits 'scattering' , and regrouping after 30 seconds
		if (isOfficer(npcId))
		{
			Camp camp = camps.get(getCampId(npc));

			L2Npc mob = camp.recruits.get(Rnd.get(camp.recruits.size()));
			if (!mob.isDead())
				mob.broadcastNpcSay("The drillmaster is dead!");
			mob = camp.recruits.get(Rnd.get(camp.recruits.size()));
			if (!mob.isDead())
				mob.broadcastNpcSay("Line up the ranks!!");

			for (Iterator<L2Npc> i = camp.recruits.iterator(); i.hasNext();)
			{
				mob = i.next();
				if (mob.getNpcId() == npcId)
					continue;
				final int fearLocX = mob.getX() + (Rnd.get(500,1000) - Rnd.get(1000));
				final int fearLocY = mob.getY() + (Rnd.get(500,1000) - Rnd.get(1000));
				final int fearHeading = Util.calculateHeadingFrom(mob.getX(), mob.getY(), fearLocX, fearLocY);
				mob.startFear();
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(fearLocX, fearLocY, mob.getZ(), fearHeading));
				startQuestTimer("LineUpRank", 30000, mob, null);
			}
			cancelQuestTimer("Animate", npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (isOfficer(npc.getNpcId()))
		{
			startQuestTimer("Animate", ANIMATION_INTERVAL, npc, null, true);
		}
		return super.onSpawn(npc);
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		Camp camp = null;
		if (isRecruit(npc.getNpcId()) || isOfficer(npc.getNpcId()))
		{
			camp = camps.get(getCampId(npc));
			if (camp.officer != null && !camp.officer.isDead())
			{
				int chance = Rnd.get(100);
				if (chance < 1)
					camp.officer.broadcastNpcSay("How dare you attack my recruits!!");
				else if (chance < 1)
					camp.officer.broadcastNpcSay("Who is disrupting the order?!");
				camp.officer.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1);
			}
			for (L2Npc recruit : camp.recruits)
			{
				if (recruit == null || recruit.isDead() || recruit.equals(npc))
					continue;
				recruit.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1);
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("LineUpRank"))
		{
			if (!npc.isDead())
			{
				npc.setHeading(npc.getSpawn().getHeading());
				npc.teleToLocation(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());
				npc.getAttackByList().clear();
				npc.setWalking();
			}
		}
		else if (event.equalsIgnoreCase("Animate"))
		{
			if (npc == null || npc.isDead() || npc.isInCombat())
				return null;
			int campId = getCampId(npc);
			Camp camp = camps.get(campId);
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 0));
			for (L2Npc recruit : camp.recruits)
			{
				if (recruit == null || recruit.isDead())
					continue;
				recruit.broadcastPacket(new SocialAction(recruit.getObjectId(), 0));
			}
		}
		return null;
	}

	private int getCampId(L2Npc npc)
	{
		if (!campIdByNpc.containsKey(npc))
			return -1;

		return campIdByNpc.get(npc);
	}

	private boolean isOfficer(int npcId)
	{
		return Util.contains(SELMAHUM_DRILL_SERGEANT, npcId) || SELMAHUM_TRAINING_OFFICER == npcId;
	}

	private boolean isRecruit(int npcId)
	{
		return Util.contains(SELMAHUM_RECRUIT, npcId) || Util.contains(SELMAHUM_SOLDIER, npcId);
	}

	private void initSpawns()
	{
		for (int id : camps.keys())
		{
			Camp camp = camps.get(id);

			for (SpawnData spawnData : camp.spawns)
			{
				Location loc = spawnData.spawnLoc;
				L2Npc npc = addSpawn(spawnData.npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0);
				npc.getSpawn().setAmount(1);
				npc.getSpawn().startRespawn();
				npc.getSpawn().setRespawnDelay(RESPAWN_DELAY);
				npc.setIsNoRndWalk(true);
				npc.setIsNoAnimation(true);
				if (isOfficer(spawnData.npcId))
					camp.officer = npc;
				else
					camp.recruits.add(npc);
				campIdByNpc.put(npc, camp.id);
			}
			startQuestTimer("Animate", ANIMATION_INTERVAL, camp.officer, null, true);
		}
	}

	private void init()
	{
		File f = new File(Config.DATAPACK_ROOT, "data/spawnZones/training_grounds.xml");
		if (!f.exists())
		{
			_log.warning("SelMahumTrainingGrounds: Error! training_grounds.xml file is missing!");
			return;
		}

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setValidating(true);
			Document doc = factory.newDocumentBuilder().parse(f);

			for (Node n = doc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equals("camp"))
				{
					NamedNodeMap attrs = n.getAttributes();
					final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());

					ArrayList<SpawnData> spawns = new ArrayList<SpawnData>();
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equals("spawn"))
						{
							attrs = d.getAttributes();
							Location loc = new Location();
							final int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
							loc.setX(Integer.parseInt(attrs.getNamedItem("x").getNodeValue()));
							loc.setY(Integer.parseInt(attrs.getNamedItem("y").getNodeValue()));
							loc.setZ(Integer.parseInt(attrs.getNamedItem("z").getNodeValue()));
							loc.setHeading(Integer.parseInt(attrs.getNamedItem("heading").getNodeValue()));

							SpawnData spawnData = new SpawnData(npcId, loc);
							spawns.add(spawnData);
						}
					}

					Camp camp = new Camp(id, spawns);
					camps.put(id, camp);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("SelMahumTrainingGrounds: Error while loading. " + e);
		}

		_log.info("SelMahumTrainingGrounds: Loaded " + camps.size() + " training camps.");

		initSpawns();
	}

	public SelMahumTrainingGrounds(int questId, String name, String descr)
	{
		super(questId, name, descr);

		QuestEventType[] eventTypes = new QuestEventType[] { 
				QuestEventType.ON_KILL, QuestEventType.ON_ATTACK, QuestEventType.ON_SPAWN
		};
		registerMobs(SELMAHUM_DRILL_SERGEANT, eventTypes);
		registerMobs(SELMAHUM_RECRUIT, eventTypes);
		registerMobs(SELMAHUM_SOLDIER, eventTypes);
		registerMobs(new int[] { SELMAHUM_TRAINING_OFFICER }, eventTypes);

		init();
	}

	public static void main(String[] args)
	{
		new SelMahumTrainingGrounds(-1, qName, "ai/zones/SelMahum");
	}
}
