/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ct25.xtreme.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import ct25.xtreme.gameserver.engines.DocumentParser;
import ct25.xtreme.gameserver.model.L2SummonItem;

/** 
 * @author BossForever
 */
public final class SummonItemsData extends DocumentParser
{
	private static final Map<Integer, L2SummonItem> _summonitems = new HashMap<>();
	
	/**
	 * Instantiates a new static objects.
	 */
	protected SummonItemsData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_summonitems.clear();
		parseDatapackFile("data/SummonItems.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _summonitems.size() + " summon items templates.");
	}
	
	@Override
	protected void parseDocument()
	{
		int despawn = -1;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if(d.getNodeName().equalsIgnoreCase("summon_item"))
					{
						int itemID = Integer.valueOf(d.getAttributes().getNamedItem("itemID").getNodeValue());
						int npcID = Integer.valueOf(d.getAttributes().getNamedItem("npcID").getNodeValue());
						byte summonType = Byte.valueOf(d.getAttributes().getNamedItem("summonType").getNodeValue());					
						if (summonType == 0)
							despawn = Integer.valueOf(d.getAttributes().getNamedItem("despawnDelay").getNodeValue());						
						
						L2SummonItem summonitem = new L2SummonItem(itemID, npcID, summonType, despawn);
						_summonitems.put(itemID, summonitem);
					}
				}
			}
		}
	}
	
	public L2SummonItem getSummonItem(int itemId)
	{
		return _summonitems.get(itemId);
	}

	public int[] itemIDs()
	{
		int size = _summonitems.size();
		int[] result = new int[size];
		int i = 0;

		for(L2SummonItem si : _summonitems.values())
		{
			result[i] = si.getItemId();
			i++;
		}
		return result;
	}
	
	/**
	 * Gets the single instance of StaticObjects.
	 * @return single instance of StaticObjects
	 */
	public static SummonItemsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonItemsData _instance = new SummonItemsData();
	}
}