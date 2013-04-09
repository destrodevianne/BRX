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
package ai.individual.raidboss;

import java.util.Map;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.instancemanager.RaidBossSpawnManager;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.Location;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2RaidBossInstance;
import ct25.xtreme.gameserver.model.quest.QuestTimer;
import ct25.xtreme.gameserver.skills.SkillHolder;
import ct25.xtreme.util.Rnd;

/**
 * Anais AI.
 * @author Browser
 */
public final class Anais extends L2AttackableAIScript
{
	// NPCs
	private static final int ANAIS = 25701;
	private static final int DIVINE_BURNER = 18915;
	private static final int GRAIL_WARD = 18929;
	// Skill
	private static SkillHolder DIVINE_NOVA = new SkillHolder(6326, 1);
	// Instances
	private final L2Npc[] _divineBurners = new L2Npc[4];
	private L2Npc _anais = null;
	private L2PcInstance _nextTarget = null;
	private L2Npc _current = null;
	private int _pot = 0;
	
	private Anais(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(ANAIS);
		addKillId(GRAIL_WARD);
		
		int i = 0;
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnsByNpcId(DIVINE_BURNER))
		{
			_divineBurners[i++] = spawn.getLastSpawn();
		}
		
		L2RaidBossInstance _anais = RaidBossSpawnManager.getInstance().getBosses().get(ANAIS);
		this.onSpawn(_anais);
	}
	
	private void burnerOnAttack(int pot)
	{
		L2Npc npc = _divineBurners[pot];
		npc.setDisplayEffect(1);
		npc.setIsRunning(false);
		if (pot < 4)
		{
			_current = npc;
			QuestTimer checkAround = getQuestTimer("CHECK", _anais, null);
			if (checkAround == null) // || !checkAround.getIsActive()
			{
				startQuestTimer("CHECK", 3000, _anais, null);
			}
		}
		else
		{
			cancelQuestTimer("CHECK", _anais, null);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "CHECK":
				if (!npc.isAttackingNow())
				{
					cancelQuestTimer("CHECK", npc, null);
				}
				if ((_current != null) || (_pot < 4))
				{
					Map<Integer, L2PcInstance> players = _anais.getKnownList().getKnownPlayers();
					L2PcInstance target = players.get(Rnd.get(players.size() - 1));
					_nextTarget = target;
					if (_nextTarget == null)
					{
						_nextTarget = (L2PcInstance) _anais.getTarget();
					}
					L2Npc b = _divineBurners[_pot];
					_pot = _pot + 1;
					b.setDisplayEffect(1);
					b.setIsRunning(false);
					L2Npc ward = addSpawn(GRAIL_WARD, new Location(b.getX(), b.getY(), b.getZ()), true, 0);
					((L2Attackable) ward).addDamageHate(_nextTarget, 0, 999);
					ward.setIsRunning(true);
					ward.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _nextTarget, null);
					startQuestTimer("GUARD_ATTACK", 1000, ward, _nextTarget, true);
					startQuestTimer("SUICIDE", 20000, ward, null);
					ward.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _nextTarget);
				}
				break;
			case "GUARD_ATTACK":
				if (_nextTarget != null)
				{
					final double distance = Math.sqrt(npc.getPlanDistanceSq(_nextTarget.getX(), _nextTarget.getY()));
					if (distance < 100)
					{
						npc.doCast(DIVINE_NOVA.getSkill());
					}
					else if (distance > 2000)
					{
						npc.doDie(null);
						cancelQuestTimer("GUARD_ATTACK", npc, player);
					}
				}
				break;
			case "SUICIDE":
				npc.doCast(DIVINE_NOVA.getSkill());
				cancelQuestTimer("GUARD_ATTACK", npc, _nextTarget);
				if (_current != null)
				{
					_current.setDisplayEffect(2);
					_current.setIsRunning(false);
					_current = null;
				}
				npc.doDie(null);
				break;
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (_pot == 0)
		{
			burnerOnAttack(0);
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.75)) && (_pot == 1))
		{
			burnerOnAttack(1);
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)) && (_pot == 2))
		{
			burnerOnAttack(2);
		}
		else if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.25)) && (_pot == 3))
		{
			burnerOnAttack(3);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		npc.doCast(DIVINE_NOVA.getSkill());
		cancelQuestTimer("GUARD_ATTACK", npc, _nextTarget);
		if (_current != null)
		{
			_current.setDisplayEffect(2);
			_current.setIsRunning(false);
			_current = null;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Anais(-1, "Anais", "ai");
	}
}
