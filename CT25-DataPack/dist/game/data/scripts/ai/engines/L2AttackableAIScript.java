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
package ai.engines;

import static ct25.xtreme.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import ct25.xtreme.gameserver.ai.CtrlEvent;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.instancemanager.DimensionalRiftManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2RiftInvaderInstance;
import ct25.xtreme.gameserver.model.quest.jython.QuestJython;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct25.xtreme.gameserver.util.Broadcast;
import ct25.xtreme.gameserver.util.Util;

/**
 * Overarching Superclass for all mob AI
 * @author Fulminus
 */
public class L2AttackableAIScript extends QuestJython
{

	/**
	 * This is used to register all monsters contained in mobs for a particular script<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method register ID for all QuestEventTypes<BR>
	 * Do not use for group_template AIs</B></FONT><BR>
	 * @param mobs
	 * @see #registerMobs(int[], QuestEventType...)
	 */
	public void registerMobs(final int[] mobs)
	{
		for (final int id : mobs)
		{
			addEventId(id, QuestEventType.ON_ATTACK);
			addEventId(id, QuestEventType.ON_KILL);
			addEventId(id, QuestEventType.ON_SPAWN);
			addEventId(id, QuestEventType.ON_SPELL_FINISHED);
			addEventId(id, QuestEventType.ON_SKILL_SEE);
			addEventId(id, QuestEventType.ON_FACTION_CALL);
			addEventId(id, QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
	}

	/**
	 * This is used simply for convenience of replacing jython 'element in list' boolean method.
	 * @param array 
	 * @param obj 
	 * @param <T> 
	 * @return 
	 */
	public static <T> boolean contains(final T[] array, final T obj)
	{
		for (final T element : array)
			if (element == obj)
				return true;
		return false;
	}
	
	public static boolean contains(final int[] array, final int obj)
	{
		for (final int element : array)
			if (element == obj)
				return true;
		return false;
	}

	/**
	 * This is used to register all monsters contained in mobs for a particular script event types defined in types.
	 * @param mobs
	 * @param types
	 */
	public void registerMobs(final int[] mobs, final QuestEventType... types)
	{
		for (final int id : mobs)
			for (final QuestEventType type : types)
				addEventId(id, type);
	}

	public L2AttackableAIScript(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		return null;
	}

	@Override
	public String onSpellFinished(final L2Npc npc, final L2PcInstance player, final L2Skill skill)
	{
		return null;
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (caster == null)
			return null;
		if (!(npc instanceof L2Attackable))
			return null;

		final L2Attackable attackable = (L2Attackable) npc;

		int skillAggroPoints = skill.getAggroPoints();

		if (caster.getPet() != null)
			if (targets.length == 1 && Util.contains(targets, caster.getPet()))
				skillAggroPoints = 0;
			
		if (skillAggroPoints > 0)
			if (attackable.hasAI() && attackable.getAI().getIntention() == AI_INTENTION_ATTACK)
			{
				final L2Object npcTarget = attackable.getTarget();
				for (final L2Object skillTarget : targets)
					if (npcTarget == skillTarget || npc == skillTarget)
					{
						final L2Character originalCaster = isPet ? caster.getPet() : caster;
						attackable.addDamageHate(originalCaster, 0, skillAggroPoints * 150 / (attackable.getLevel() + 7));
					}
			}

		return null;
	}

	@Override
	public String onFactionCall(final L2Npc npc, final L2Npc caller, final L2PcInstance attacker, final boolean isPet)
	{
		if (attacker == null)
			return null;

		final L2Character originalAttackTarget = isPet ? attacker.getPet() : attacker;
		if (attacker.isInParty() && attacker.getParty().isInDimensionalRift())
		{
			final byte riftType = attacker.getParty().getDimensionalRift().getType();
			final byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();

			if (caller instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
				return null;
		}

		// By default, when a faction member calls for help, attack the caller's attacker.
		// Notify the AI with EVT_AGGRESSION
		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);

		return null;
	}

	@Override
	public String onAggroRangeEnter(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (player == null)
			return null;

		final L2Character target = isPet ? player.getPet() : player;

		((L2Attackable) npc).addDamageHate(target, 0, 1);

		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		return null;
	}

	@Override
	public String onSpawn(final L2Npc npc)
	{
		return null;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (attacker != null && npc instanceof L2Attackable)
		{
			final L2Attackable attackable = (L2Attackable) npc;

			final L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
			attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
			attackable.addDamageHate(originalAttacker, damage, damage * 100 / (attackable.getLevel() + 7));
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc instanceof L2MonsterInstance)
		{
			final L2MonsterInstance mob = (L2MonsterInstance) npc;
			if (mob.getLeader() != null)
				mob.getLeader().getMinionList().onMinionDie(mob, -1);
			
			if (mob.hasMinions())
				mob.getMinionList().onMasterDie(false);
		}
		return null;
	}

	protected void attackPlayer(final L2Attackable npc, final L2Playable playable)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, playable);
	}

	public void broadcastNpcSay(final L2Npc npc, final int type, final String text)
	{
		Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getDisplayId(), text));
	}

	/**
	 * Broadcasts NpcSay packet to all known players with npc string id.
	 * @param npc
	 * @param type
	 * @param clientMsgId
	 */
	protected void broadcastNpcSay(final L2Npc npc, final int type, final int clientMsgId)
	{
		Broadcast.toKnownPlayers(npc, new NpcSay(npc.getObjectId(), type, npc.getTemplate().getDisplayId(), clientMsgId));
	}

	public static void main(final String[] args)
	{
		final L2AttackableAIScript ai = new L2AttackableAIScript(-1, "L2AttackableAIScript", "L2AttackableAIScript");
		// register all mobs here...
		for (int level = 1; level < 100; level++)
		{
			final L2NpcTemplate[] templates = NpcTable.getInstance().getAllOfLevel(level);
			if (templates != null && templates.length > 0)
				for (final L2NpcTemplate t : templates)
					try
					{
						if (L2Attackable.class.isAssignableFrom(Class.forName("ct25.xtreme.gameserver.model.actor.instance." + t.type + "Instance")))
						{
							ai.addEventId(t.npcId, QuestEventType.ON_ATTACK);
							ai.addEventId(t.npcId, QuestEventType.ON_KILL);
							ai.addEventId(t.npcId, QuestEventType.ON_SPAWN);
							ai.addEventId(t.npcId, QuestEventType.ON_SKILL_SEE);
							ai.addEventId(t.npcId, QuestEventType.ON_FACTION_CALL);
							ai.addEventId(t.npcId, QuestEventType.ON_AGGRO_RANGE_ENTER);
						}
					}
					catch (final ClassNotFoundException ex)
					{
						_log.info("Class not found " + t.type + "Instance");
					}
		}
	}
}