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
package ai.zones.StakatoNest;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2RaidBossInstance;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.ExSetCompassZoneCode;

/**
 * @author Browser/InsOmnia
 * Queen Shyeed AI
 * Stakato Nest Zone AI (basing on Queen spawn)
 * 
 * Manage respawn time of Queen Shyeed and Stakato Nest Zone buff
 * basing on Queen Shyeed status.
 */

public class QueenShyeed extends L2AttackableAIScript
{
	private static final int QueenShyeed = 25671;
	private static final int StakatoNestZone = 20400;
	private static final int String_ID = 1800850; //Shyeed's cry is steadily dying down.
	private long QueenRespawn = 0;
	private long QueenStatus = 0; // 0 alive, 1 dead
	protected ScheduledFuture<?> _zoneTask = null;
	public QueenShyeed(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(QueenShyeed);
		addEnterZoneId(StakatoNestZone);
		addExitZoneId(StakatoNestZone);
		try
		{
			QueenRespawn = Long.valueOf(loadGlobalQuestVar("QueenRespawn"));
			QueenStatus = Long.valueOf(loadGlobalQuestVar("QueenStatus"));
		}
		catch (Exception e) {}
		saveGlobalQuestVar("QueenRespawn", String.valueOf(QueenRespawn));
		saveGlobalQuestVar("QueenStatus", String.valueOf(QueenStatus));
		if (QueenStatus == 0 && !checkIfQueenSpawned())
		{
			addSpawn(QueenShyeed, 79635, -55612, -5980, 0, false, 0);
			startQuestTimer("QueenDespawn", 10800000, null, null);
		}
		else
			startQuestTimer("QueenSpawn", QueenRespawn - System.currentTimeMillis(), null, null);
		_zoneTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new stakatoBuffTask(), 30000, 30001);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("QueenSpawn") && !checkIfQueenSpawned())
		{
			saveGlobalQuestVar("QueenStatus", String.valueOf(0));
			QueenStatus = 0;
			startQuestTimer("QueenDespawn", 10800000, null, null);
			addSpawn(QueenShyeed, 79635, -55612, -5980, 0, false, 0);
		}
		else if (event.equalsIgnoreCase("QueenDespawn"))
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(StakatoNestZone);
			for (L2Character c : zone.getCharactersInside().values())
			{
				if (c instanceof L2Npc)
				{
					if (((L2Npc) c).getId() == QueenShyeed)
					{
						long respawn = 86400000; // 24h
						saveGlobalQuestVar("QueenRespawn", String.valueOf(System.currentTimeMillis() + respawn));
						saveGlobalQuestVar("QueenStatus", String.valueOf(1));
						QueenStatus = 1;
						startQuestTimer("QueenSpawn", respawn, null, null);
						((L2RaidBossInstance) c).deleteMe();
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("CompassON"))
			player.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE));
		else if (event.equalsIgnoreCase("CompassOFF"))
			player.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		npc.broadcastNpcSay(Say2.NPC_ALL, String_ID);
		long respawn = 86400000; // 24h
		startQuestTimer("QueenSpawn", respawn, npc, null);
		saveGlobalQuestVar("QueenRespawn", String.valueOf(System.currentTimeMillis() + respawn));
		saveGlobalQuestVar("QueenStatus", String.valueOf(1));
		QueenStatus = 1;
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			startQuestTimer("CompassON", 5000, null, (L2PcInstance) character);
			if (!checkIfPc(zone) && _zoneTask == null)
				_zoneTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new stakatoBuffTask(), 30000, 30001);
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			startQuestTimer("CompassOFF", 5000, null, (L2PcInstance) character);
			if (howManyPc(zone) == 1 && _zoneTask != null)
			{
				_zoneTask.cancel(true);
				_zoneTask = null;
			}
		}
		return super.onExitZone(character, zone);
	}

	private boolean checkIfQueenSpawned()
	{
		L2ZoneType zone = ZoneManager.getInstance().getZoneById(StakatoNestZone);
		for (L2Character c : zone.getCharactersInside().values())
		{
			if (c instanceof L2Npc)
				if (((L2Npc) c).getId() == QueenShyeed)
					return true;
		}
		return false;
	}

	private boolean checkIfPc(L2ZoneType zone)
	{
		final Collection<L2Character> inside = zone.getCharactersInside().values();
		for (L2Character c : inside)
		{
			if (c instanceof L2PcInstance)
				return true;
		}
		return false;
	}

	private int howManyPc(L2ZoneType zone)
	{
		int count = 0;
		final Collection<L2Character> inside = zone.getCharactersInside().values();
		for (L2Character c : inside)
		{
			if (c instanceof L2PcInstance)
				count++;
		}
		return count;
	}

	private class stakatoBuffTask implements Runnable
	{
		public void run()
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(StakatoNestZone);
			if (howManyPc(zone) > 0)
			{
				int skillId = 0;
				for (L2Character c : zone.getCharactersInside().values())
				{
					if (QueenStatus == 0)
					{
						if (c instanceof L2PcInstance)
						{
							skillId = 6169;
							handleNestBuff(c, skillId);
						}
						else if (c instanceof L2Npc)
						{
							skillId = 6170;
							handleNestBuff(c, skillId);
						}
					}
					else
					{
						if (c instanceof L2PcInstance)
						{
							skillId = 6171;
							handleNestBuff(c, skillId);
						}
					}
				}
			}
			else if (_zoneTask != null)
			{
				_zoneTask.cancel(true);
				_zoneTask = null;
			}
		}
		private void handleNestBuff(L2Character c, int skillId)
		{
			int skillLevel = 1;
			if (skillId == 6169)
				c.stopSkillEffects(6171);
			else
				c.stopSkillEffects(6169);
			if (c.getFirstEffect(skillId) == null)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				skill.getEffects(c, c);
			}
		}
	}

	public static void main(String[] args)
	{
		new QueenShyeed(-1, QueenShyeed.class.getSimpleName(), "ai/zones");
	}
}