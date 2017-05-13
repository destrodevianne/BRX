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
 * @author Browser/InsOmnia Queen Shyeed AI Stakato Nest Zone AI (basing on Queen spawn) Manage respawn time of Queen Shyeed and Stakato Nest Zone buff basing on Queen Shyeed status.
 */

public class QueenShyeed extends L2AttackableAIScript
{
	// Boss
	private static final int SHYEED = 25671;

	// Zone
	private static final int ZONE = 20400;

	// String
	private static final int STRING = 1800850;
	
	// Zone Buffs
	private static final int SHYEED_FURY1 = 6169;
	private static final int SHYEED_FURY2 = 6170;
	private static final int FULL_AUTHORITY = 6171;

	// Tracking
	protected long QueenRespawn = 0;
	protected long QueenStatus = 0; // 0 alive, 1 dead
	protected ScheduledFuture<?> _zoneTask = null;

	public QueenShyeed(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addKillId(SHYEED);
		addEnterZoneId(ZONE);
		addExitZoneId(ZONE);
		try
		{
			QueenRespawn = Long.valueOf(loadGlobalQuestVar("QueenRespawn"));
			QueenStatus = Long.valueOf(loadGlobalQuestVar("QueenStatus"));
		}
		catch (final Exception e)
		{
		}
		saveGlobalQuestVar("QueenRespawn", String.valueOf(QueenRespawn));
		saveGlobalQuestVar("QueenStatus", String.valueOf(QueenStatus));
		if (QueenStatus == 0 && !checkIfQueenSpawned())
		{
			addSpawn(SHYEED, 79635, -55612, -5980, 0, false, 0);
			startQuestTimer("QueenDespawn", 10800000, null, null);
		}
		else
			startQuestTimer("QueenSpawn", QueenRespawn - System.currentTimeMillis(), null, null);
		_zoneTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new stakatoBuffTask(), 30000, 30001);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("QueenSpawn") && !checkIfQueenSpawned())
		{
			saveGlobalQuestVar("QueenStatus", String.valueOf(0));
			QueenStatus = 0;
			startQuestTimer("QueenDespawn", 10800000, null, null);
			addSpawn(SHYEED, 79635, -55612, -5980, 0, false, 0);
		}
		else if (event.equalsIgnoreCase("QueenDespawn"))
		{
			final L2ZoneType zone = ZoneManager.getInstance().getZoneById(ZONE);
			for (final L2Character c : zone.getCharactersInside().values())
				if (c instanceof L2Npc)
					if (((L2Npc) c).getId() == SHYEED)
					{
						final long respawn = 86400000; // 24h
						saveGlobalQuestVar("QueenRespawn", String.valueOf(System.currentTimeMillis() + respawn));
						saveGlobalQuestVar("QueenStatus", String.valueOf(1));
						QueenStatus = 1;
						startQuestTimer("QueenSpawn", respawn, null, null);
						((L2RaidBossInstance) c).deleteMe();
					}
		}
		else if (event.equalsIgnoreCase("CompassON"))
			player.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE));
		else if (event.equalsIgnoreCase("CompassOFF"))
			player.sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
		return null;
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		npc.broadcastNpcSay(Say2.ALL, STRING); // Shyeed's cry is steadily dying down.
		final long respawn = 86400000; // 24h
		startQuestTimer("QueenSpawn", respawn, npc, null);
		saveGlobalQuestVar("QueenRespawn", String.valueOf(System.currentTimeMillis() + respawn));
		saveGlobalQuestVar("QueenStatus", String.valueOf(1));
		QueenStatus = 1;
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onEnterZone(final L2Character character, final L2ZoneType zone)
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
	public String onExitZone(final L2Character character, final L2ZoneType zone)
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
		final L2ZoneType zone = ZoneManager.getInstance().getZoneById(ZONE);
		for (final L2Character c : zone.getCharactersInside().values())
			if (c instanceof L2Npc)
				if (((L2Npc) c).getId() == SHYEED)
					return true;
		return false;
	}
	
	private boolean checkIfPc(final L2ZoneType zone)
	{
		final Collection<L2Character> inside = zone.getCharactersInside().values();
		for (final L2Character c : inside)
			if (c instanceof L2PcInstance)
				return true;
		return false;
	}
	
	protected int howManyPc(final L2ZoneType zone)
	{
		int count = 0;
		final Collection<L2Character> inside = zone.getCharactersInside().values();
		for (final L2Character c : inside)
			if (c instanceof L2PcInstance)
				count++;
		return count;
	}
	
	protected class stakatoBuffTask implements Runnable
	{
		@Override
		public void run()
		{
			final L2ZoneType zone = ZoneManager.getInstance().getZoneById(ZONE);
			if (howManyPc(zone) > 0)
			{
				int skillId = 0;
				for (final L2Character c : zone.getCharactersInside().values())
					if (QueenStatus == 0)
					{
						if (c instanceof L2PcInstance)
						{
							skillId = SHYEED_FURY1;
							handleNestBuff(c, skillId);
						}
						else if (c instanceof L2Npc)
						{
							skillId = SHYEED_FURY2;
							handleNestBuff(c, skillId);
						}
					}
					else if (c instanceof L2PcInstance)
					{
						skillId = FULL_AUTHORITY;
						handleNestBuff(c, skillId);
					}
			}
			else if (_zoneTask != null)
			{
				_zoneTask.cancel(true);
				_zoneTask = null;
			}
		}
		
		private void handleNestBuff(final L2Character c, final int skillId)
		{
			final int skillLevel = 1;
			if (skillId == SHYEED_FURY1)
				c.stopSkillEffects(FULL_AUTHORITY);
			else
				c.stopSkillEffects(SHYEED_FURY1);
			if (c.getFirstEffect(skillId) == null)
			{
				final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				skill.getEffects(c, c);
			}
		}
	}
	
	public static void main(final String[] args)
	{
		new QueenShyeed(-1, QueenShyeed.class.getSimpleName(), "ai/zones");
	}
}