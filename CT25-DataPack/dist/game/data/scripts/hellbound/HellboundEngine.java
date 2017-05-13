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
package hellbound;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.Announcements;
import ct25.xtreme.gameserver.datatables.DoorTable;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import javolution.util.FastMap;

/**
 * Hellbound Engine.
 * @author Zoey76
 */
public class HellboundEngine extends Quest implements Runnable
{
	private static final String pointsInfoFile = "data/hellboundTrustPoints.xml";

	private static final int UPDATE_INTERVAL = 10000;

	private static final int[][] DOOR_LIST =
	{
		{
			19250001,
			5
		},
		{
			19250002,
			5
		},
		{
			20250001,
			9
		},
		{
			20250002,
			7
		}
	};

	private static final int[] MAX_TRUST =
	{
		0,
		300000,
		600000,
		1000000,
		1010000,
		1400000,
		1490000,
		2000000,
		2000001,
		2500000,
		4000000,
		0
	};

	private static final String ANNOUNCE = "Hellbound now has reached level: %lvl%";

	private int _cachedLevel = -1;

	private static Map<Integer, PointsInfoHolder> pointsInfo = new FastMap<>();

	// Holds info about points for mob killing
	private class PointsInfoHolder
	{
		protected int pointsAmount;
		protected int minHbLvl;
		protected int maxHbLvl;
		protected int lowestTrustLimit;

		protected PointsInfoHolder(final int points, final int min, final int max, final int trust)
		{
			pointsAmount = points;
			minHbLvl = min;
			maxHbLvl = max;
			lowestTrustLimit = trust;
		}
	}

	private final void onLevelChange(final int newLevel)
	{
		try
		{
			HellboundManager.getInstance().setMaxTrust(MAX_TRUST[newLevel]);
			HellboundManager.getInstance().setMinTrust(MAX_TRUST[newLevel - 1]);
		}
		catch (final ArrayIndexOutOfBoundsException e)
		{
			HellboundManager.getInstance().setMaxTrust(0);
			HellboundManager.getInstance().setMinTrust(0);
		}

		HellboundManager.getInstance().updateTrust(0, false);
		HellboundManager.getInstance().doSpawn();

		for (final int[] doorData : DOOR_LIST)
			try
			{
				final L2DoorInstance door = DoorTable.getInstance().getDoor(doorData[0]);
				if (door.getOpen())
				{
					if (newLevel < doorData[1])
						door.closeMe();
				}
				else if (newLevel >= doorData[1])
					door.openMe();
			}
			catch (final Exception e)
			{
				_log.log(Level.WARNING, "Hellbound doors problem!", e);
			}

		if (_cachedLevel > 0)
		{
			Announcements.getInstance().announceToAll(ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));
			_log.info("HellboundEngine: New Level: " + newLevel);
		}
		_cachedLevel = newLevel;
	}

	private void loadPointsInfoData()
	{
		final File file = new File(Config.DATAPACK_ROOT, pointsInfoFile);
		if (!file.exists())
		{
			_log.warning("Cannot locate points info file: " + pointsInfoFile);
			return;
		}

		Document doc = null;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "Could not parse " + pointsInfoFile + " file: " + e.getMessage(), e);
			return;
		}

		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if ("list".equalsIgnoreCase(n.getNodeName()))
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if ("npc".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						Node att;

						att = attrs.getNamedItem("id");
						if (att == null)
						{
							_log.severe("[Hellbound Trust Points Info] Missing NPC ID, skipping record");
							continue;
						}

						final int npcId = Integer.parseInt(att.getNodeValue());

						att = attrs.getNamedItem("points");
						if (att == null)
						{
							_log.severe("[Hellbound Trust Points Info] Missing reward point info for NPC ID " + npcId + ", skipping record");
							continue;
						}
						final int points = Integer.parseInt(att.getNodeValue());

						att = attrs.getNamedItem("minHellboundLvl");
						if (att == null)
						{
							_log.severe("[Hellbound Trust Points Info] Missing minHellboundLvl info for NPC ID " + npcId + ", skipping record");
							continue;
						}
						final int minHbLvl = Integer.parseInt(att.getNodeValue());

						att = attrs.getNamedItem("maxHellboundLvl");
						if (att == null)
						{
							_log.severe("[Hellbound Trust Points Info] Missing maxHellboundLvl info for NPC ID " + npcId + ", skipping record");
							continue;
						}
						final int maxHbLvl = Integer.parseInt(att.getNodeValue());

						att = attrs.getNamedItem("lowestTrustLimit");
						int lowestTrustLimit = 0;
						if (att != null)
							lowestTrustLimit = Integer.parseInt(att.getNodeValue());

						pointsInfo.put(npcId, new PointsInfoHolder(points, minHbLvl, maxHbLvl, lowestTrustLimit));
					}
		_log.info("HellboundEngine: Loaded: " + pointsInfo.size() + " trust point reward data");
	}

	@Override
	public void run()
	{
		int level = HellboundManager.getInstance().getLevel();
		if (level > 0 && level == _cachedLevel)
		{
			if (HellboundManager.getInstance().getTrust() == HellboundManager.getInstance().getMaxTrust() && level != 4) // only exclusion is kill of Derek
			{
				level++;
				HellboundManager.getInstance().setLevel(level);
				onLevelChange(level);
			}
		}
		else
			onLevelChange(level); // first run or changed by admin
	}

	// Let's try to manage all trust changes for killing here
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isSummon)
	{
		final int npcId = npc.getId();
		if (pointsInfo.containsKey(npcId))
		{
			final PointsInfoHolder npcInfo = pointsInfo.get(npcId);

			if (HellboundManager.getInstance().getLevel() >= npcInfo.minHbLvl && HellboundManager.getInstance().getLevel() <= npcInfo.maxHbLvl && (npcInfo.lowestTrustLimit == 0 || HellboundManager.getInstance().getTrust() > npcInfo.lowestTrustLimit))
				HellboundManager.getInstance().updateTrust(npcInfo.pointsAmount, true);

			if (npc.getId() == 18465 && HellboundManager.getInstance().getLevel() == 4)
				HellboundManager.getInstance().setLevel(5);
		}

		return super.onKill(npc, killer, isSummon);
	}

	public HellboundEngine(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		HellboundManager.getInstance().registerEngine(this, UPDATE_INTERVAL);
		loadPointsInfoData();

		// Register onKill for all rewardable monsters
		for (final int npcId : pointsInfo.keySet())
			addKillId(npcId);

		_log.info("HellboundEngine: Mode: levels 0-3");
		_log.info("HellboundEngine: Level: " + HellboundManager.getInstance().getLevel());
		_log.info("HellboundEngine: Trust: " + HellboundManager.getInstance().getTrust());
		if (HellboundManager.getInstance().isLocked())
			_log.info("HellboundEngine: State: locked");
		else
			_log.info("HellboundEngine: State: unlocked");
	}

	public static void main(final String[] args)
	{
		new HellboundEngine(-1, HellboundEngine.class.getSimpleName(), "hellbound");
	}
}