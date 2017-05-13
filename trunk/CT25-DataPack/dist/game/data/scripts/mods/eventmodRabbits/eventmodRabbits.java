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
package mods.eventmodRabbits;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.Announcements;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.QuestManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2EventChestInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2EventMonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Event;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;
import javolution.util.FastList;

public class eventmodRabbits extends Event
{
	// Event NPC's list
	private List<L2Npc> _npclist;
	// Event Task
	ScheduledFuture<?> _eventTask = null;
	// Event time
	public static final int _event_time = 10;
	// Event state
	private static boolean _isactive = false;
	// Current Chest count
	private static int _chest_count = 0;
	// How much Chests
	private static final int _option_howmuch = 100;
	// NPc's
	public static final int _npc_snow = 900101;
	public static final int _npc_chest = 900102;
	// Skills
	public static final int _skill_tornado = 630;
	public static final int _skill_magic_eye = 629;

	// Drop data
	private static final int[][] DROPLIST =
	{
		{
			1540,
			80,
			10,
			15
		}, // Quick Healing Potion
		{
			1538,
			60,
			5,
			10
		}, // Blessed Scroll of Escape
		{
			3936,
			40,
			5,
			10
		}, // Blessed Scroll of Ressurection
		{
			6387,
			25,
			5,
			10
		}, // Blessed Scroll of Ressurection Pets
		{
			22025,
			15,
			5,
			10
		}, // Powerful Healing Potion
		{
			6622,
			10,
			1,
			1
		}, // Giant's Codex
		{
			20034,
			5,
			1,
			1
		}, // Revita Pop
		{
			20004,
			1,
			1,
			1
		}, // Energy Ginseng
		{
			20004,
			0,
			1,
			1
		} // Energy Ginseng
	};

	public static void main(final String[] args)
	{
		new eventmodRabbits(-1, "eventmodRabbits", "mods");
	}

	public eventmodRabbits(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(_npc_snow);
		addFirstTalkId(_npc_snow);
		addTalkId(_npc_snow);

		addFirstTalkId(_npc_chest);
		addSkillSeeId(_npc_chest);
		addSpawnId(_npc_chest);
		addAttackId(_npc_chest);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		((L2EventMonsterInstance) npc).eventSetDropOnGround(true);
		((L2EventMonsterInstance) npc).eventSetBlockOffensiveSkills(true);

		npc.setIsImmobilized(true);
		npc.disableCoreAI(true);

		return super.onSpawn(npc);
	}

	@Override
	public boolean eventStart()
	{
		// Don't start event if its active
		if (_isactive)
			return false;

		// Check Custom Table - we use custom NPC's
		if (!Config.CUSTOM_NPC_TABLE)
			return false;

		// Initialize list
		_npclist = new FastList<>();

		// Set Event active
		_isactive = true;

		// Spawn Manager
		recordSpawn(_npc_snow, -59227, -56939, -2039, 64106, false, 0);

		// Spawn Chests
		for (int i = 0; i < _option_howmuch; i++)
		{
			final int x = Rnd.get(-60653, -58772);
			final int y = Rnd.get(-55830, -57718);
			recordSpawn(_npc_chest, x, y, -2030, 0, true, _event_time * 60 * 1000);
			_chest_count++;
		}

		// Announce event start
		Announcements.getInstance().announceToAll("Rabbit Event : Chests spawned!");
		Announcements.getInstance().announceToAll("Go to Fantasy Isle and grab some rewards!");
		Announcements.getInstance().announceToAll("You have " + _event_time + " min - after that time all chests will disappear...");

		// Schedule Event end
		_eventTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				timeUp();
			}
		}, _event_time * 60 * 1000);

		return true;
	}

	protected void timeUp()
	{
		Announcements.getInstance().announceToAll("Time up !");
		eventStop();
	}

	@Override
	public boolean eventStop()
	{
		// Don't stop inactive event
		if (!_isactive)
			return false;

		// Set inactive
		_isactive = false;

		// Cancel task if any
		if (_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		// Despawn Npc's
		if (!_npclist.isEmpty())
			for (final L2Npc _npc : _npclist)
				if (_npc != null)
					_npc.deleteMe();
		_npclist.clear();

		// Announce event end
		Announcements.getInstance().announceToAll("Rabbit Event finished");

		return true;
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;

		if (event.equalsIgnoreCase("transform"))
		{
			if (player.isTransformed() || player.isInStance())
				player.untransform();

			SkillTable.getInstance().getInfo(2428, 1).getEffects(npc, player);

			return null;
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			final Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return npc.getId() + ".htm";
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (Util.contains(targets, npc))
			if (skill.getId() == _skill_tornado)
			{
				dropItem(npc, caster, DROPLIST);
				npc.deleteMe();
				_chest_count--;

				if (_chest_count <= 0)
				{
					Announcements.getInstance().announceToAll("No more chests...");
					eventStop();
				}
			}
			else if (skill.getId() == _skill_magic_eye)
				if (npc instanceof L2EventChestInstance)
					((L2EventChestInstance) npc).trigger();
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		// Some retards go to event and disturb it by breaking chests
		// So... Apply raid curse if player don't use skill on chest but attack it
		if (_isactive && npc.getId() == _npc_chest)
			SkillTable.getInstance().getInfo(4515, 1).getEffects(npc, attacker);

		return super.onAttack(npc, attacker, damage, isPet);
	}

	private static final void dropItem(final L2Npc mob, final L2PcInstance player, final int[][] droplist)
	{
		final int chance = Rnd.get(100);

		for (final int[] drop : droplist)
		{
			if (chance > drop[1])
			{
				((L2MonsterInstance) mob).dropItem(player, drop[0], Rnd.get(drop[2], drop[3]));
				return;
			}
		}
	}

	private L2Npc recordSpawn(final int npcId, final int x, final int y, final int z, final int heading, final boolean randomOffSet, final long despawnDelay)
	{
		final L2Npc _tmp = addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay);
		if (_tmp != null)
			_npclist.add(_tmp);
		return _tmp;
	}

	@Override
	public boolean eventBypass(final L2PcInstance activeChar, final String bypass)
	{
		return false;
	}
}
