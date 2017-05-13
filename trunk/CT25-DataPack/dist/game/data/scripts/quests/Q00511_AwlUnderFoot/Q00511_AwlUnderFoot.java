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
package quests.Q00511_AwlUnderFoot;

import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2Party;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2RaidBossInstance;
import ct25.xtreme.gameserver.model.entity.Fort;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Gigiikun
 */
public final class Q00511_AwlUnderFoot extends Quest
{
	protected class FAUWorld extends InstanceWorld
	{
	}

	public static class FortDungeon
	{
		private final int INSTANCEID;
		private long _reEnterTime = 0;

		public FortDungeon(final int iId)
		{
			INSTANCEID = iId;
		}

		public int getInstanceId()
		{
			return INSTANCEID;
		}

		public long getReEnterTime()
		{
			return _reEnterTime;
		}

		public void setReEnterTime(final long time)
		{
			_reEnterTime = time;
		}
	}

	private static final String qn = "Q00511_AwlUnderFoot";
	private static final boolean debug = false;
	private static final long REENTERTIME = 14400000;
	private static final long RAID_SPAWN_DELAY = 120000;

	private final TIntObjectHashMap<FortDungeon> _fortDungeons = new TIntObjectHashMap<>(21);

	// QUEST ITEMS
	private static final int DL_MARK = 9797;
	
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;
	
	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	protected static final int[] RAIDS1 =
	{
		25572,
		25575,
		25578
	};
	protected static final int[] RAIDS2 =
	{
		25579,
		25582,
		25585,
		25588
	};
	protected static final int[] RAIDS3 =
	{
		25589,
		25592,
		25593
	};

	private static final SkillHolder RAID_CURSE = new SkillHolder(5456, 1);

	private String checkConditions(final L2PcInstance player)
	{
		if (debug)
			return null;
		final L2Party party = player.getParty();
		if (party == null)
			return "FortressWarden-03.htm";
		if (party.getLeader() != player)
			return getHtm(player.getHtmlPrefix(), "FortressWarden-04.htm").replace("%leader%", party.getLeader().getName());
		for (final L2PcInstance partyMember : party.getPartyMembers())
		{
			final QuestState st = partyMember.getQuestState(qn);
			if (st == null || st.getInt("cond") < 1)
				return getHtm(player.getHtmlPrefix(), "FortressWarden-05.htm").replace("%player%", partyMember.getName());
			if (!Util.checkIfInRange(1000, player, partyMember, true))
				return getHtm(player.getHtmlPrefix(), "FortressWarden-06.htm").replace("%player%", partyMember.getName());
		}
		return null;
	}
	
	private void teleportPlayer(final L2PcInstance player, final int[] coords, final int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}
	
	protected String enterInstance(final L2PcInstance player, final String template, final int[] coords, final FortDungeon dungeon, String ret)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof FAUWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			teleportPlayer(player, coords, world.instanceId);
			return "";
		}
		// New instance
		if (ret != null)
			return ret;
		ret = checkConditions(player);
		if (ret != null)
			return ret;
		final L2Party party = player.getParty();
		final int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setSpawnLoc(new int[]
		{
			player.getX(),
			player.getY(),
			player.getZ()
		});
		world = new FAUWorld();
		world.instanceId = instanceId;
		world.templateId = dungeon.getInstanceId();
		world.status = 0;
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTERTIME);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Fortress AwlUnderFoot started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid((FAUWorld) world), RAID_SPAWN_DELAY);
		
		// teleport players
		if (player.getParty() == null)
		{
			teleportPlayer(player, coords, instanceId);
			world.allowed.add(player.getObjectId());
		}
		else
			for (final L2PcInstance partyMember : party.getPartyMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.allowed.add(partyMember.getObjectId());
				if (partyMember.getQuestState(qn) == null)
					newQuestState(partyMember);
			}
		return getHtm(player.getHtmlPrefix(), "FortressWarden-08.htm").replace("%clan%", player.getClan().getName());
	}

	private class spawnRaid implements Runnable
	{
		private final FAUWorld _world;

		public spawnRaid(final FAUWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			try
			{
				int spawnId;
				if (_world.status == 0)
					spawnId = RAIDS1[Rnd.get(RAIDS1.length)];
				else if (_world.status == 1)
					spawnId = RAIDS2[Rnd.get(RAIDS2.length)];
				else
					spawnId = RAIDS3[Rnd.get(RAIDS3.length)];
				final L2Npc raid = addSpawn(spawnId, 53319, 245814, -6576, 0, false, 0, false, _world.instanceId);
				if (raid instanceof L2RaidBossInstance)
					((L2RaidBossInstance) raid).setUseRaidCurse(false);
			}
			catch (final Exception e)
			{
				_log.warning("Fortress AwlUnderFoot Raid Spawn error: " + e);
			}
		}
	}

	private String checkFortCondition(final L2PcInstance player, final L2Npc npc, final boolean isEnter)
	{
		final Fort fortress = npc.getFort();
		final FortDungeon dungeon = _fortDungeons.get(npc.getId());
		if (player == null || fortress == null || dungeon == null)
			return "FortressWarden-01.htm";
		if (player.getClan() == null || player.getClan().getHasFort() != fortress.getFortId())
			return "FortressWarden-01.htm";
		else if (fortress.getFortState() == 0)
			return "FortressWarden-02a.htm";
		else if (fortress.getFortState() == 2)
			return "FortressWarden-02b.htm";
		else if (isEnter && dungeon.getReEnterTime() > System.currentTimeMillis())
			return "FortressWarden-07.htm";
		
		return null;
	}

	private void rewardPlayer(final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		if (st.getInt("cond") == 1)
		{
			st.giveItems(DL_MARK, 140);
			st.playSound("ItemSound.quest_itemget");
		}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			final int[] tele = new int[3];
			tele[0] = 53322;
			tele[1] = 246380;
			tele[2] = -6580;
			return enterInstance(player, "fortdungeon.xml", tele, _fortDungeons.get(npc.getId()), checkFortCondition(player, npc, true));
		}
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		final int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("FortressWarden-10.htm"))
		{
			if (cond == 0)
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("FortressWarden-15.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		final String ret = checkFortCondition(player, npc, false);
		if (ret != null)
			return ret;
		else if (st != null)
		{
			final int npcId = npc.getId();
			int cond = 0;
			if (st.getState() == State.CREATED)
				st.set("cond", "0");
			else
				cond = st.getInt("cond");
			if (_fortDungeons.containsKey(npcId) && cond == 0)
			{
				if (player.getLevel() >= 60)
					htmltext = "FortressWarden-09.htm";
				else
				{
					htmltext = "FortressWarden-00.htm";
					st.exitQuest(true);
				}
			}
			else if (_fortDungeons.containsKey(npcId) && cond > 0 && st.getState() == State.STARTED)
			{
				final long count = st.getQuestItemsCount(DL_MARK);
				if (cond == 1 && count > 0)
				{
					htmltext = "FortressWarden-14.htm";
					st.takeItems(DL_MARK, count);
					st.rewardItems(KNIGHT_EPALUETTE, count);
				}
				else if (cond == 1 && count == 0)
					htmltext = "FortressWarden-10.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		final L2Playable attacker = isPet ? player.getPet() : player;
		if (attacker.getLevel() - npc.getLevel() >= 9)
			if (attacker.getBuffCount() > 0 || attacker.getDanceCount() > 0)
			{
				npc.setTarget(attacker);
				npc.doSimultaneousCast(RAID_CURSE.getSkill());
			}
			else if (player.getParty() != null)
				for (final L2PcInstance pmember : player.getParty().getPartyMembers())
					if (pmember.getBuffCount() > 0 || pmember.getDanceCount() > 0)
					{
						npc.setTarget(pmember);
						npc.doSimultaneousCast(RAID_CURSE.getSkill());
					}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FAUWorld)
		{
			final FAUWorld world = (FAUWorld) tmpworld;
			if (Util.contains(RAIDS3, npc.getId()))
			{
				if (player.getParty() != null)
					for (final L2PcInstance pl : player.getParty().getPartyMembers())
						rewardPlayer(pl);
				else
					rewardPlayer(player);

				final Instance instanceObj = InstanceManager.getInstance().getInstance(world.instanceId);
				instanceObj.setDuration(360000);
				instanceObj.removeNpcs();
			}
			else
			{
				world.status++;
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid(world), RAID_SPAWN_DELAY);
			}
		}
		return null;
	}
	
	public Q00511_AwlUnderFoot(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		_fortDungeons.put(35666, new FortDungeon(22));
		_fortDungeons.put(35698, new FortDungeon(23));
		_fortDungeons.put(35735, new FortDungeon(24));
		_fortDungeons.put(35767, new FortDungeon(25));
		_fortDungeons.put(35804, new FortDungeon(26));
		_fortDungeons.put(35835, new FortDungeon(27));
		_fortDungeons.put(35867, new FortDungeon(28));
		_fortDungeons.put(35904, new FortDungeon(29));
		_fortDungeons.put(35936, new FortDungeon(30));
		_fortDungeons.put(35974, new FortDungeon(31));
		_fortDungeons.put(36011, new FortDungeon(32));
		_fortDungeons.put(36043, new FortDungeon(33));
		_fortDungeons.put(36081, new FortDungeon(34));
		_fortDungeons.put(36118, new FortDungeon(35));
		_fortDungeons.put(36149, new FortDungeon(36));
		_fortDungeons.put(36181, new FortDungeon(37));
		_fortDungeons.put(36219, new FortDungeon(38));
		_fortDungeons.put(36257, new FortDungeon(39));
		_fortDungeons.put(36294, new FortDungeon(40));
		_fortDungeons.put(36326, new FortDungeon(41));
		_fortDungeons.put(36364, new FortDungeon(42));
		
		for (final int i : _fortDungeons.keys())
		{
			addStartNpc(i);
			addTalkId(i);
		}

		for (final int i : RAIDS1)
			addKillId(i);
		for (final int i : RAIDS2)
			addKillId(i);
		for (final int i : RAIDS3)
			addKillId(i);

		for (int i = 25572; i <= 25595; i++)
			addAttackId(i);
	}

	public static void main(final String[] args)
	{
		// now call the constructor (starts up the)
		new Q00511_AwlUnderFoot(511, qn, "instances");
	}
}