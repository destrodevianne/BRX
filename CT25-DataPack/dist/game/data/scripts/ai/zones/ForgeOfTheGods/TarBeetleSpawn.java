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
package ai.zones.ForgeOfTheGods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ct25.xtreme.gameserver.GeoData;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.engines.DocumentParser;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.L2Territory;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.util.Rnd;
import javolution.util.FastMap;

/**
 * Tar Beetle zone spawn
 * @author malyelfik
 */
public class TarBeetleSpawn extends DocumentParser
{
	private static final Map<Integer, SpawnZone> _spawnZoneList = new HashMap<>();
	private static final Map<Integer, L2Npc> _spawnList = new FastMap<>();

	public static List<Integer> lowerZones = new ArrayList<>();
	public static List<Integer> upperZones = new ArrayList<>();

	public static int lowerNpcCount = 0;
	public static int upperNpcCount = 0;

	public TarBeetleSpawn()
	{
		load();
	}

	@Override
	public void load()
	{
		_spawnZoneList.clear();
		_spawnList.clear();
		parseDatapackFile("data/spawnZones/tar_beetle.xml");
		_log.info(TarBeetleSpawn.class.getSimpleName() + ": Loaded " + _spawnZoneList.size() + " spawn zones.");
	}

	@Override
	protected void parseDocument()
	{
		final Node n = getCurrentDocument().getFirstChild();
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			if (d.getNodeName().equals("spawnZones"))
				for (Node r = d.getFirstChild(); r != null; r = r.getNextSibling())
					if (r.getNodeName().equals("zone"))
					{
						NamedNodeMap attrs = r.getAttributes();
						final int id = parseInteger(attrs, "id");
						final int minZ = parseInteger(attrs, "minZ");
						final int maxZ = parseInteger(attrs, "maxZ");
						final String type = parseString(attrs, "type");
						if (type.equals("upper"))
							upperZones.add(id);
						else if (type.equals("lower"))
							lowerZones.add(id);

						int[] bZones = null;
						final String bZonesStr = parseString(attrs, "bZones", "");
						if (!bZonesStr.isEmpty())
						{
							final String[] str = bZonesStr.split(";");
							bZones = new int[str.length];
							for (int i = 0; i < str.length; i++)
								bZones[i] = Integer.parseInt(str[i]);
						}

						final SpawnZone zone = new SpawnZone(id, bZones);
						for (Node c = r.getFirstChild(); c != null; c = c.getNextSibling())
							if (c.getNodeName().equals("point"))
							{
								attrs = c.getAttributes();
								final int x = parseInteger(attrs, "x");
								final int y = parseInteger(attrs, "y");
								zone.add(x, y, minZ, maxZ, 0);
							}
						_spawnZoneList.put(id, zone);
					}
	}

	public void removeBeetle(final L2Npc npc)
	{
		npc.deleteMe();
		_spawnList.remove(npc.getObjectId());
		if (npc.getSpawn().getLocz() < -5000)
			lowerNpcCount--;
		else
			upperNpcCount--;
	}

	public void spawn(final List<Integer> zone)
	{
		try
		{
			Collections.shuffle(zone);
			final int[] loc = getSpawnZoneById(zone.get(0)).getRandomPoint();

			final L2Spawn spawn = new L2Spawn(NpcTable.getInstance().getTemplate(18804));
			spawn.setHeading(Rnd.get(65535));
			spawn.setLocx(loc[0]);
			spawn.setLocy(loc[1]);
			spawn.setLocz(GeoData.getInstance().getSpawnHeight(loc[0], loc[1], loc[2], loc[3], null));

			final L2Npc npc = spawn.doSpawn();
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
			npc.setIsInvul(true);
			npc.disableCoreAI(true);
			npc.setScriptValue(5);

			_spawnList.put(npc.getObjectId(), npc);
		}
		catch (final Exception e)
		{
			_log.warning(TarBeetleSpawn.class.getSimpleName() + ": Could not spawn npc! Error: " + e.getMessage());
		}
	}

	public void startTasks()
	{
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SpawnTask(), 1000, 60000);
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new NumShotTask(), 300000, 300000);
	}

	public SpawnZone getSpawnZoneById(final int id)
	{
		return _spawnZoneList.get(id);
	}

	public L2Npc getBeetle(final L2Npc npc)
	{
		return _spawnList.get(npc.getObjectId());
	}

	public static Map<Integer, L2Npc> getSpawnList()
	{
		return _spawnList;
	}

	private class SpawnZone extends L2Territory
	{
		private final int[] _bZones;

		public SpawnZone(final int terr, final int[] bZones)
		{
			super(terr);
			_bZones = bZones;
		}

		@Override
		public int[] getRandomPoint()
		{
			int[] loc = super.getRandomPoint();
			while (isInsideBannedZone(loc))
				loc = super.getRandomPoint();
			return loc;
		}

		private boolean isInsideBannedZone(final int[] loc)
		{
			if (_bZones != null)
				for (final int i : _bZones)
					if (getSpawnZoneById(i).isInside(loc[0], loc[1]))
						return true;
			return false;
		}
	}

	public class SpawnTask implements Runnable
	{
		@Override
		public void run()
		{
			while (lowerNpcCount < 4)
			{
				spawn(lowerZones);
				lowerNpcCount++;
			}

			while (upperNpcCount < 12)
			{
				spawn(upperZones);
				upperNpcCount++;
			}
		}
	}

	public class NumShotTask implements Runnable
	{
		@Override
		public void run()
		{
			final Iterator<L2Npc> iterator = getSpawnList().values().iterator();
			while (iterator.hasNext())
			{
				final L2Npc npc = iterator.next();
				final int val = npc.getScriptValue();
				if (val == 5)
				{
					npc.deleteMe();
					iterator.remove();
					if (npc.getSpawn().getLocz() < -5000)
						lowerNpcCount--;
					else
						upperNpcCount--;
				}
				else
					npc.setScriptValue(val + 1);
			}
		}
	}
}