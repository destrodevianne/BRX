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
package quests.TerritoryWarScripts;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ct25.xtreme.gameserver.SevenSigns;
import ct25.xtreme.gameserver.instancemanager.TerritoryWarManager;
import ct25.xtreme.gameserver.instancemanager.TerritoryWarManager.TerritoryNPCSpawn;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.TerritoryWard;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.serverpackets.ExShowScreenMessage;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;
import quests.Q00147_PathtoBecominganEliteMercenary.Q00147_PathtoBecominganEliteMercenary;
import quests.Q00176_StepsForHonor.Q00176_StepsForHonor;

public class TerritoryWarSuperClass extends Quest
{
	private static Map<Integer, TerritoryWarSuperClass> _forTheSakeScripts = new HashMap<>();
	private static Map<Integer, TerritoryWarSuperClass> _protectTheScripts = new HashMap<>();
	private static Map<Integer, TerritoryWarSuperClass> _killTheScripts = new HashMap<>();

	public static String qn = "TerritoryWarSuperClass";

	// "For the Sake of the Territory ..." quests variables
	public int CATAPULT_ID;
	public int TERRITORY_ID;
	public int[] LEADER_IDS;
	public int[] GUARD_IDS;
	public String[] Text = {};
	// "Protect the ..." quests variables
	public int[] NPC_IDS;
	// "Kill The ..."
	public int[] CLASS_IDS;
	public int RANDOM_MIN;
	public int RANDOM_MAX;

	// Used to register NPCs "For the Sake of the Territory ..." quests
	public void registerKillIds()
	{
		addKillId(CATAPULT_ID);
		for (final int mobid : LEADER_IDS)
			addKillId(mobid);
		for (final int mobid : GUARD_IDS)
			addKillId(mobid);
	}

	// Used to register NPCs "Protect the ..." quests
	public void registerAttackIds()
	{
		for (final int mobid : NPC_IDS)
			addAttackId(mobid);
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (Util.contains(targets, npc))
			if (skill.getId() == 845)
			{
				if (TerritoryWarManager.getInstance().getHQForClan(caster.getClan()) != npc)
					return super.onSkillSee(npc, caster, skill, targets, isPet);
				npc.deleteMe();
				TerritoryWarManager.getInstance().setHQForClan(caster.getClan(), null);
			}
			else if (skill.getId() == 847)
			{
				if (TerritoryWarManager.getInstance().getHQForTerritory(caster.getSiegeSide()) != npc)
					return super.onSkillSee(npc, caster, skill, targets, isPet);
				final TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(caster);
				if (ward == null)
					return super.onSkillSee(npc, caster, skill, targets, isPet);
				if (caster.getSiegeSide() - 80 == ward.getOwnerCastleId())
				{
					for (final TerritoryNPCSpawn wardSpawn : TerritoryWarManager.getInstance().getTerritory(ward.getOwnerCastleId()).getOwnedWard())
						if (wardSpawn.getId() == ward.getTerritoryId())
						{
							wardSpawn.setNPC(wardSpawn.getNpc().getSpawn().doSpawn());
							ward.unSpawnMe();
							ward.setNpc(wardSpawn.getNpc());
						}
				}
				else
				{
					ward.unSpawnMe();
					ward.setNpc(TerritoryWarManager.getInstance().addTerritoryWard(ward.getTerritoryId(), caster.getSiegeSide() - 80, ward.getOwnerCastleId(), true));
					ward.setOwnerCastleId(caster.getSiegeSide() - 80);
					TerritoryWarManager.getInstance().getTerritory(caster.getSiegeSide() - 80).getQuestDone()[1]++;
				}
			}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	public int getTerritoryIdForThisNPCId(final int npcid)
	{
		return 0;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		if (npc.getCurrentHp() == npc.getMaxHp() && Util.contains(NPC_IDS, npc.getId()))
		{
			final int territoryId = getTerritoryIdForThisNPCId(npc.getId());
			if (territoryId >= 81 && territoryId <= 89)
				for (final L2PcInstance pl : L2World.getInstance().getAllPlayers().values())
					if (pl.getSiegeSide() == territoryId)
					{
						QuestState st = pl.getQuestState(getName());
						if (st == null)
							st = newQuestState(pl);
						if (st.getState() != State.STARTED)
						{
							st.set("cond", "1");
							st.setStateAndNotSave(State.STARTED);
						}
					}
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getId() == CATAPULT_ID)
		{
			TerritoryWarManager.getInstance().territoryCatapultDestroyed(TERRITORY_ID - 80);
			TerritoryWarManager.getInstance().giveTWPoint(killer, TERRITORY_ID, 4);
			TerritoryWarManager.getInstance().announceToParticipants(new ExShowScreenMessage(Text[0], 10000), 135000, 13500);
			handleBecomeMercenaryQuest(killer, true);
		}
		else if (Util.contains(LEADER_IDS, npc.getId()))
			TerritoryWarManager.getInstance().giveTWPoint(killer, TERRITORY_ID, 3);

		if (killer.getSiegeSide() != TERRITORY_ID && TerritoryWarManager.getInstance().getTerritory(killer.getSiegeSide() - 80) != null)
			TerritoryWarManager.getInstance().getTerritory(killer.getSiegeSide() - 80).getQuestDone()[0]++;
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onAdvEvent(String event, final L2Npc npc, final L2PcInstance player)
	{
		if (npc != null || player != null)
			return null;
		final StringTokenizer st = new StringTokenizer(event, " ");
		event = st.nextToken(); // Get actual command
		if (event.equalsIgnoreCase("setNextTWDate"))
		{
			final Calendar startTWDate = Calendar.getInstance();
			startTWDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			startTWDate.set(Calendar.HOUR_OF_DAY, 20);
			startTWDate.set(Calendar.MINUTE, 0);
			startTWDate.set(Calendar.SECOND, 0);
			if (startTWDate.getTimeInMillis() < System.currentTimeMillis())
				startTWDate.add(Calendar.DAY_OF_MONTH, 7);
			if (!SevenSigns.getInstance().isDateInSealValidPeriod(startTWDate))
				startTWDate.add(Calendar.DAY_OF_MONTH, 7);
			saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
			TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
			_log.info("Next TerritoryWarTime: " + startTWDate.getTime());
		}
		else if (event.equalsIgnoreCase("setTWDate") && st.hasMoreTokens())
		{
			final Calendar startTWDate = Calendar.getInstance();
			startTWDate.setTimeInMillis(Long.parseLong(st.nextToken()));
			saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
			TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
		}
		return null;
	}

	private void handleKillTheQuest(final L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		int kill = 1;
		int max = 10;
		if (st == null)
			st = newQuestState(player);
		if (st.getState() != State.COMPLETED)
		{
			if (st.getState() != State.STARTED)
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.set("kill", "1");
				max = Rnd.get(RANDOM_MIN, RANDOM_MAX);
				st.set("max", String.valueOf(max));
			}
			else
			{
				kill = st.getInt("kill") + 1;
				max = st.getInt("max");
			}
			if (kill >= max)
			{
				TerritoryWarManager.getInstance().giveTWQuestPoint(player);
				st.addExpAndSp(534000, 51000);
				st.set("doneDate", String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)));
				st.setState(State.COMPLETED);
				st.exitQuest(true);
				player.sendPacket(new ExShowScreenMessage(Text[1], 10000));
			}
			else
			{
				st.set("kill", String.valueOf(kill));
				player.sendPacket(new ExShowScreenMessage(Text[0].replace("MAX", String.valueOf(max)).replace("KILL", String.valueOf(kill)), 10000));
			}
		}
		else if (st.getInt("doneDate") != Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.set("kill", "1");
			max = Rnd.get(RANDOM_MIN, RANDOM_MAX);
			st.set("max", String.valueOf(max));
			player.sendPacket(new ExShowScreenMessage(Text[0].replace("MAX", String.valueOf(max)).replace("KILL", String.valueOf(kill)), 10000));
		}
		else if (player.isGM())
		{
			// just for test
			player.sendMessage("Cleaning " + getName() + " Territory War quest by force!");
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.set("kill", "1");
			max = Rnd.get(RANDOM_MIN, RANDOM_MAX);
			st.set("max", String.valueOf(max));
			player.sendPacket(new ExShowScreenMessage(Text[0].replace("MAX", String.valueOf(max)).replace("KILL", String.valueOf(kill)), 10000));
		}
	}

	private void handleBecomeMercenaryQuest(final L2PcInstance player, final boolean catapult)
	{
		final QuestState _state = player.getQuestState(Q00147_PathtoBecominganEliteMercenary.class.getSimpleName());
		if (_state != null && _state.getState() == State.STARTED)
		{
			final int _cond = _state.getInt("cond");
			if (catapult)
			{
				if (_cond == 2)
					_state.set("cond", "4");
				else if (_cond == 1)
					_state.set("cond", "3");
			}
			else if (_cond == 1 || _cond == 3)
			{
				// Get
				int _kills = _state.getInt("kills");
				// Increase
				_kills++;
				// Save
				_state.set("kills", String.valueOf(_kills));
				// Check
				if (_kills >= 10)
					if (_cond == 1)
						_state.set("cond", "2");
					else if (_cond == 3)
						_state.set("cond", "4");
			}
		}
	}

	private void handleStepsForHonor(final L2PcInstance player)
	{
		int kills = 0;
		int cond = 0;
		// Additional Handle for Quest
		final QuestState _sfh = player.getQuestState(Q00176_StepsForHonor.class.getSimpleName());
		if (_sfh != null && _sfh.getState() == State.STARTED)
		{
			cond = _sfh.getInt("cond");
			if (cond == 1 || cond == 3 || cond == 5 || cond == 7)
			{
				// Get kills
				kills = _sfh.getInt("kills");
				// Increase
				kills++;
				// Save
				_sfh.set("kills", String.valueOf(kills));
				// Check
				if (cond == 1 && kills >= 9)
				{
					_sfh.set("cond", "2");
					_sfh.set("kills", "0");
				}
				else if (cond == 3 && kills >= 18)
				{
					_sfh.set("cond", "4");
					_sfh.set("kills", "0");
				}
				else if (cond == 5 && kills >= 27)
				{
					_sfh.set("cond", "6");
					_sfh.set("kills", "0");
				}
				else if (cond == 7 && kills >= 36)
				{
					_sfh.set("cond", "8");
					_sfh.unset("kills");
				}
			}
		}
	}

	@Override
	public String onDeath(final L2Character killer, final L2Character victim, final QuestState qs)
	{
		if (killer == victim || !(victim instanceof L2PcInstance) || victim.getLevel() < 61)
			return "";
		final L2PcInstance actingPlayer = killer.getActingPlayer();
		if (actingPlayer != null && qs.getPlayer() != null)
		{
			if (actingPlayer.getParty() != null)
				for (final L2PcInstance pl : actingPlayer.getParty().getPartyMembers())
				{
					if (pl.getSiegeSide() == qs.getPlayer().getSiegeSide() || pl.getSiegeSide() == 0 || !Util.checkIfInRange(2000, killer, pl, false))
						continue;
					if (pl == actingPlayer)
					{
						handleStepsForHonor(actingPlayer);
						handleBecomeMercenaryQuest(actingPlayer, false);
					}
					handleKillTheQuest(pl);
				}
			else if (actingPlayer.getSiegeSide() != qs.getPlayer().getSiegeSide() && actingPlayer.getSiegeSide() > 0)
			{
				handleKillTheQuest(actingPlayer);
				handleStepsForHonor(actingPlayer);
				handleBecomeMercenaryQuest(actingPlayer, false);
			}
			TerritoryWarManager.getInstance().giveTWPoint(actingPlayer, qs.getPlayer().getSiegeSide(), 1);
		}
		return "";
	}

	@Override
	public String onEnterWorld(final L2PcInstance player)
	{
		final int territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
		if (territoryId > 0)
		{
			// register Territory Quest
			final TerritoryWarSuperClass territoryQuest = _forTheSakeScripts.get(territoryId);
			QuestState st = player.getQuestState(territoryQuest.getName());
			if (st == null)
				st = territoryQuest.newQuestState(player);
			st.set("cond", "1");
			st.setStateAndNotSave(State.STARTED);

			// register player on Death
			if (player.getLevel() >= 61)
			{
				final TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassId().getId());
				if (killthe != null)
				{
					st = player.getQuestState(killthe.getName());
					if (st == null)
						st = killthe.newQuestState(player);
					player.addNotifyQuestOfDeath(st);
				}
				else
					_log.warning("TerritoryWar: Missing Kill the quest for player " + player.getName() + " whose class id: " + player.getClassId().getId());
			}
		}
		return null;
	}

	@Override
	public void setOnEnterWorld(final boolean val)
	{
		super.setOnEnterWorld(val);

		for (final L2PcInstance player : L2World.getInstance().getAllPlayers().values())
			if (player.getSiegeSide() > 0)
			{
				final TerritoryWarSuperClass territoryQuest = _forTheSakeScripts.get(player.getSiegeSide());
				if (territoryQuest == null)
					continue;
				QuestState st = player.getQuestState(territoryQuest.getName());
				if (st == null)
					st = territoryQuest.newQuestState(player);
				if (val)
				{
					st.set("cond", "1");
					st.setStateAndNotSave(State.STARTED);
					// register player on Death
					if (player.getLevel() >= 61)
					{
						final TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassId().getId());
						if (killthe != null)
						{
							st = player.getQuestState(killthe.getName());
							if (st == null)
								st = killthe.newQuestState(player);
							player.addNotifyQuestOfDeath(st);
						}
						else
							_log.warning("TerritoryWar: Missing Kill the quest for player " + player.getName() + " whose class id: " + player.getClassId().getId());
					}
				}
				else
				{
					st.setStateAndNotSave(State.COMPLETED);
					st.exitQuest(false);
					for (final Quest q : _protectTheScripts.values())
						if (player.getQuestState(q.getName()) != null)
						{
							player.getQuestState(q.getName()).setStateAndNotSave(State.COMPLETED);
							player.getQuestState(q.getName()).exitQuest(false);
						}
					// unregister player on Death
					final TerritoryWarSuperClass killthe = _killTheScripts.get(player.getClassIndex());
					if (killthe != null)
					{
						st = player.getQuestState(killthe.getName());
						if (st != null)
							player.removeNotifyQuestOfDeath(st);
					}
				}
			}
	}

	public TerritoryWarSuperClass(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		if (questId < 0)
		{
			// Outpost and Ward handled by the Super Class script
			addSkillSeeId(36590);

			// Calculate next TW date
			final Calendar startTWDate = Calendar.getInstance();
			if (loadGlobalQuestVar("nextTWStartDate").equalsIgnoreCase(""))
			{
				startTWDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
				startTWDate.set(Calendar.HOUR_OF_DAY, 20);
				startTWDate.set(Calendar.MINUTE, 0);
				startTWDate.set(Calendar.SECOND, 0);
				if (startTWDate.getTimeInMillis() < System.currentTimeMillis())
					startTWDate.add(Calendar.DAY_OF_MONTH, 7);
				if (!SevenSigns.getInstance().isDateInSealValidPeriod(startTWDate))
					startTWDate.add(Calendar.DAY_OF_MONTH, 7);
				saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
			}
			else
			{
				startTWDate.setTimeInMillis(Long.parseLong(loadGlobalQuestVar("nextTWStartDate")));
				if (startTWDate.getTimeInMillis() < System.currentTimeMillis() && SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getMilliToPeriodChange() > 172800000)
				{
					startTWDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
					startTWDate.set(Calendar.HOUR_OF_DAY, 20);
					startTWDate.set(Calendar.MINUTE, 0);
					startTWDate.set(Calendar.SECOND, 0);
					if (startTWDate.getTimeInMillis() < System.currentTimeMillis())
						startTWDate.add(Calendar.DAY_OF_MONTH, 7);
					if (!SevenSigns.getInstance().isDateInSealValidPeriod(startTWDate))
						startTWDate.add(Calendar.DAY_OF_MONTH, 7);
					saveGlobalQuestVar("nextTWStartDate", String.valueOf(startTWDate.getTimeInMillis()));
				}
			}
			TerritoryWarManager.getInstance().setTWStartTimeInMillis(startTWDate.getTimeInMillis());
			_log.info("Next TerritoryWarTime: " + startTWDate.getTime());
		}
	}

	public static void main(final String[] args)
	{
		// initialize superclass
		new TerritoryWarSuperClass(-1, qn, "Territory_War");

		// initialize subclasses
		// "For The Sake" quests
		final TerritoryWarSuperClass gludio = new TheTerritoryGludio();
		_forTheSakeScripts.put(gludio.TERRITORY_ID, gludio);
		final TerritoryWarSuperClass dion = new TheTerritoryDion();
		_forTheSakeScripts.put(dion.TERRITORY_ID, dion);
		final TerritoryWarSuperClass giran = new TheTerritoryGiran();
		_forTheSakeScripts.put(giran.TERRITORY_ID, giran);
		final TerritoryWarSuperClass oren = new TheTerritoryOren();
		_forTheSakeScripts.put(oren.TERRITORY_ID, oren);
		final TerritoryWarSuperClass aden = new TheTerritoryAden();
		_forTheSakeScripts.put(aden.TERRITORY_ID, aden);
		final TerritoryWarSuperClass innadril = new TheTerritoryInnadril();
		_forTheSakeScripts.put(innadril.TERRITORY_ID, innadril);
		final TerritoryWarSuperClass goddard = new TheTerritoryGoddard();
		_forTheSakeScripts.put(goddard.TERRITORY_ID, goddard);
		final TerritoryWarSuperClass rune = new TheTerritoryRune();
		_forTheSakeScripts.put(rune.TERRITORY_ID, rune);
		final TerritoryWarSuperClass schuttgart = new TheTerritorySchuttgart();
		_forTheSakeScripts.put(schuttgart.TERRITORY_ID, schuttgart);
		// "Protect the" quests
		final TerritoryWarSuperClass catapult = new ProtectTheCatapult();
		_protectTheScripts.put(catapult.getId(), catapult);
		final TerritoryWarSuperClass military = new ProtectTheMilitary();
		_protectTheScripts.put(military.getId(), military);
		final TerritoryWarSuperClass religious = new ProtectTheReligious();
		_protectTheScripts.put(religious.getId(), religious);
		final TerritoryWarSuperClass supplies = new ProtectTheSupplies();
		_protectTheScripts.put(supplies.getId(), supplies);
		final TerritoryWarSuperClass economic = new ProtectTheEconomic();
		_protectTheScripts.put(economic.getId(), economic);
		// "Kill the" quests
		final TerritoryWarSuperClass knights = new KillTheKnights();
		for (final int i : knights.CLASS_IDS)
			_killTheScripts.put(i, knights);
		final TerritoryWarSuperClass warriors = new KillTheWarriors();
		for (final int i : warriors.CLASS_IDS)
			_killTheScripts.put(i, warriors);
		final TerritoryWarSuperClass wizards = new KillTheWizards();
		for (final int i : wizards.CLASS_IDS)
			_killTheScripts.put(i, wizards);
		final TerritoryWarSuperClass priests = new KillThePriests();
		for (final int i : priests.CLASS_IDS)
			_killTheScripts.put(i, priests);
		final TerritoryWarSuperClass keys = new KillTheKeyTargets();
		for (final int i : keys.CLASS_IDS)
			_killTheScripts.put(i, keys);
	}
}
