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
package ai.zones.PlainsOfLizardman;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.handler.ISkillHandler;
import ct25.xtreme.gameserver.handler.SkillHandler;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.gameserver.util.ArrayUtil;

/**
 ** @author Gnacik
 */
public class PlainsOfLizardman extends L2AttackableAIScript
{
	// Spawn chance Tanta Guard x/1000
	private static final int CHANCE = 2;
	
	// Mobs e Skills
	private static final int[] _MOBS =
	{
		18864,
		18865,
		18866,
		18868
	};

	private static final int FANTASY_MUSHROOM = 18864;
	private static final int FANTASY_MUSHROOM_SKILL = 6427;

	private static final int RAINBOW_FROG = 18866;
	private static final int RAINBOW_FROG_SKILL = 6429;

	private static final int STICKY_MUSHROOM = 18865;
	private static final int STICKY_MUSHROOM_SKILL = 6428;

	private static final int ENERGY_PLANT = 18868;
	private static final int ENERGY_PLANT_SKILL = 6430;

	private static final int TANTA_GUARD = 18862;

	private static final int[] TANTA_LIZARDMEN =
	{
		22768, // Tanta Lizardman Scout
		22769, // Tanta Lizardman Warrior
		22770, // Tanta Lizardman Soldier
		22771, // Tanta Lizardman Berserker
		22772, // Tanta Lizardman Archer
		22773, // Tanta Lizardman Magician
		22774, // Tanta Lizardman Summoner
	};

	public PlainsOfLizardman(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		registerMobs(_MOBS, QuestEventType.ON_ATTACK);
		addKillId(TANTA_LIZARDMEN);
	}

	public static void main(final String[] args)
	{
		new PlainsOfLizardman(-1, PlainsOfLizardman.class.getSimpleName(), "ai/zones");
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (player != null && !player.isAlikeDead())
		{
			boolean isPet = false;
			if (event.endsWith("_pet") && player.getPet() != null && !player.getPet().isDead())
				isPet = true;

			if (event.startsWith("rainbow_frog"))
				triggerSkill(npc, isPet ? player.getPet() : player, RAINBOW_FROG_SKILL, 1);
			else if (event.startsWith("energy_plant"))
				triggerSkill(npc, isPet ? player.getPet() : player, ENERGY_PLANT_SKILL, 1);
			else if (event.startsWith("sticky_mushroom"))
				triggerSkill(npc, isPet ? player.getPet() : player, STICKY_MUSHROOM_SKILL, 1);
			else if (event.startsWith("fantasy_mushroom"))
			{
				final L2Skill skill = SkillTable.getInstance().getInfo(FANTASY_MUSHROOM_SKILL, 1);
				npc.doCast(skill);
				for (final L2Character target : npc.getKnownList().getKnownCharactersInRadius(200))
					if (target != null && target instanceof L2Attackable && target.getAI() != null)
					{
						skill.getEffects(npc, target);
						attackPlayer((L2Attackable) target, isPet ? player.getPet() : player);
					}
				npc.doDie(player);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		// Tanta Guard
		if (ArrayUtil.arrayContains(TANTA_LIZARDMEN, npc.getId()) && getRandom(1000) <= CHANCE)
		{
			final L2Npc guard = addSpawn(TANTA_GUARD, npc);
			attackPlayer((L2Attackable) guard, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.isDead())
			return null;

		if (npc.getId() == RAINBOW_FROG)
		{
			if (isPet)
				startQuestTimer("rainbow_frog_pet", 2000, npc, attacker);
			else
				startQuestTimer("rainbow_frog", 2000, npc, attacker);
			npc.doDie(attacker);
		}
		else if (npc.getId() == STICKY_MUSHROOM)
		{
			if (isPet)
				startQuestTimer("sticky_mushroom_pet", 2000, npc, attacker);
			else
				startQuestTimer("sticky_mushroom", 2000, npc, attacker);
			npc.doDie(attacker);
		}
		else if (npc.getId() == ENERGY_PLANT)
		{
			if (isPet)
				startQuestTimer("energy_plant_pet", 2000, npc, attacker);
			else
				startQuestTimer("energy_plant", 2000, npc, attacker);
			npc.doDie(attacker);
		}
		else if (npc.getId() == FANTASY_MUSHROOM)
		{
			for (final L2Character target : npc.getKnownList().getKnownCharactersInRadius(1000))
				if (target != null && target instanceof L2Attackable && target.getAI() != null)
				{
					target.setIsRunning(true);
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(npc.getX(), npc.getY(), npc.getZ(), 0));
				}
			if (isPet)
				startQuestTimer("fantasy_mushroom_pet", 3000, npc, attacker);
			else
				startQuestTimer("fantasy_mushroom", 3000, npc, attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	private void triggerSkill(final L2Character caster, final L2Playable playable, final int skill_id, final int skill_level)
	{
		final L2Character[] targets = new L2Character[1];
		targets[0] = playable;

		final L2Skill trigger = SkillTable.getInstance().getInfo(skill_id, skill_level);

		if (trigger != null && playable.isInsideRadius(caster, trigger.getCastRange(), true, false) && playable.getInstanceId() == caster.getInstanceId())
		{
			playable.broadcastPacket(new MagicSkillUse(playable, playable, skill_id, skill_level, 0, 0));

			final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(trigger.getSkillType());
			if (handler != null)
				handler.useSkill(playable, trigger, targets);
			else
				trigger.useSkill(playable, targets);
		}
	}
}