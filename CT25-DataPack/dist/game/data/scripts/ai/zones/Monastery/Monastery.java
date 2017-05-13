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
package ai.zones.Monastery;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.templates.skills.L2SkillType;
import ct25.xtreme.gameserver.util.Util;

/**
 * Monastery AI.
 * @author Browser
 */
public class Monastery extends L2AttackableAIScript
{
	// Warriors
	private static final int CAPTAIN = 18910;
	private static final int KNIGHT = 18909;
	private static final int SCARECROW = 18912;

	// Clan Of Solina
	private static final int[] SOLINA_CLAN =
	{
		22789, // Guide Solina
		22790, // Seeker Solina
		22791, // Savior Solina
		22793, // Ascetic Solina
	};

	// Divinity Clan
	private static final int[] DIVINITY_CLAN =
	{
		22794, // Divinity Judge
		22795, // Divinity Manager
		22796, // Divinity Supervisor
		22797, // Divinity Worshipper
		22798, // Divinity Protector
		22799, // Divinity Fighter
		22800, // Divinity Magus
	};

	// Strings
	private static final String[] SOLINA_KNIGHTS_MSG =
	{
		"Punish all those who tread footsteps in this place.",
		"We are the sword of truth, the sword of Solina.",
		"We raise our blades for the glory of Solina."
	};
	private static final int[] DIVINITY_MSG =
	{
		10077, // $s1, why would you choose the path of darkness?!
		10078 // $s1! How dare you defy the will of Einhasad!
	};

	private static final SkillHolder DECREASE_SPEED = new SkillHolder(4589, 8);

	private Monastery(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		registerMobs(SOLINA_CLAN, QuestEventType.ON_AGGRO_RANGE_ENTER, QuestEventType.ON_SPELL_FINISHED);
		registerMobs(DIVINITY_CLAN, QuestEventType.ON_SKILL_SEE);
		addAggroRangeEnterId(CAPTAIN, KNIGHT);
		addAttackId(KNIGHT, CAPTAIN);
		addSpawnId(KNIGHT);

		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(KNIGHT))
			startQuestTimer("training", 5000, spawn.getLastSpawn(), null, true);

		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawns(SCARECROW))
		{
			spawn.getLastSpawn().setIsInvul(true);
			spawn.getLastSpawn().disableCoreAI(true);
		}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("training") && !npc.isInCombat() && getRandom(100) < 25)
			for (final L2Character character : npc.getKnownList().getKnownCharactersInRadius(300))
				if (character.isNpc() && ((L2Npc) character).getId() == SCARECROW)
				{
					for (final L2Skill skill : npc.getAllSkills())
						if (skill.isActive())
							npc.disableSkill(skill, 0);
					npc.setRunning();
					((L2Attackable) npc).addDamageHate(character, 0, 100);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null);
					break;
				}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (player.getActiveWeaponInstance() == null)
		{
			npc.setTarget(null);
			((L2Attackable) npc).disableAllSkills();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return super.onAggroRangeEnter(npc, player, isPet);
		}

		if (player.isVisible() && !player.isGM())
		{
			npc.setRunning();
			npc.setTarget(player);
			((L2Attackable) npc).enableAllSkills();
			if (Util.contains(SOLINA_CLAN, npc.getId()))
			{
				if (getRandom(10) < 3)
					npc.broadcastNpcSay(Say2.NPC_ALL, "You cannot carry a weapon without authorization!");
				npc.doCast(DECREASE_SPEED.getSkill());
			}
			((L2Attackable) npc).addDamageHate(player, 0, 100);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player, null);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (Util.contains(DIVINITY_CLAN, npc.getId()))
			if (skill.getSkillType() == L2SkillType.AGGDAMAGE && targets.length != 0)
				for (final L2Object obj : targets)
					if (obj.equals(npc))
					{
						final NpcSay packet = new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), DIVINITY_MSG[getRandom(1)]);
						packet.addStringParameter(caster.getName());
						npc.broadcastPacket(packet);
						((L2Attackable) npc).addDamageHate(caster, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
						break;
					}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance player, final int damage, final boolean isPet)
	{
		if (getRandom(10) < 1)
			npc.broadcastNpcSay(Say2.NPC_ALL, SOLINA_KNIGHTS_MSG[getRandom(2)]);
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		npc.broadcastNpcSay(Say2.NPC_ALL, "For the glory of Solina!");
		return super.onSpawn(npc);
	}

	@Override
	public String onSpellFinished(final L2Npc npc, final L2PcInstance player, final L2Skill skill)
	{
		if (skill.getId() == DECREASE_SPEED.getSkillId())
		{
			npc.setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	public static void main(final String[] args)
	{
		new Monastery(-1, Monastery.class.getSimpleName(), "ai/zones");
	}
}
