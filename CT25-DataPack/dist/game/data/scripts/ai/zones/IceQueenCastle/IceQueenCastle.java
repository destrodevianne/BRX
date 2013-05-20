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
package ai.zones.IceQueenCastle;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.Text;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.datatables.SpawnTable;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.network.serverpackets.Scenkos;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct25.xtreme.util.Rnd;

/**
 * This class...
 * @author Browser
 */
public class IceQueenCastle extends L2AttackableAIScript
{
	
	private static final int freyaThrone = 29177;
	private static final int freyaSpelling = 29178;
	private static final int freyaStand = 29179;
	private static final int glacier = 18853;
	private static final int archer_knight = 18855;
	private static final int archer_breathe = 18854;
	private static final int jinia = 18850;
	private static final int kegor = 18851;
	
	@SuppressWarnings("unused")
	private String[] sounds = 
	{
			"Freya.freya_voice_03", "Freya.freya_voice_04",
			"Freya.freya_voice_05", "Freya.freya_voice_06",
			"Freya.freya_voice_10", "Freya.freya_voice_11"
	};
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("setDisplayEffect2"))
		{
			if (!npc.isDead())
				npc.setDisplayEffect(2);
		}
		else if (event.equalsIgnoreCase("cast"))
		{
			if (npc != null && !npc.isDead())
			{
				L2Skill skill = SkillTable.getInstance().getInfo(6437, Rnd.get(1,3));
				for (L2PcInstance plr : npc.getKnownList().getKnownPlayersInRadius(skill.getSkillRadius()))
				{
					if (!hasBuff(6437, plr) && !plr.isDead() && !plr.isAlikeDead())
						skill.getEffects(npc, plr);
				}
				startQuestTimer("cast", 10000, npc, null);
			}
		}
		else if (event.equalsIgnoreCase("show_string"))
		{
			if (npc != null && !npc.isDead())
				broadcastString(npc.getInstanceId());
		}
		else if (event.equalsIgnoreCase("summon_breathe"))
		{
			L2Npc mob = spawnNpc(archer_breathe, npc.getX() + Rnd.get(-90, 90), npc.getY() + Rnd.get(-90, 90), npc.getZ(), npc.getHeading(), npc.getInstanceId());
			mob.setRunning();
			if (npc.getTarget() != null)
			{
				mob.setTarget(npc.getTarget());
				((L2Attackable) mob).addDamageHate((L2Character) npc.getTarget(), 0, 99999);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc.getTarget());
			}
		}
		return null;
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == jinia || npcId == kegor)
		{
			((L2Attackable) npc).reduceHate(attacker, 999999999);
			npc.setCurrentHp(npc.getMaxHp());
		}
		else if (npcId == archer_knight)
		{
			if (npc.getDisplayEffect() == 1)
			{
				npc.setDisplayEffect(2);
				npc.setIsImmobilized(false);
			}
		}
		else if (npcId == freyaSpelling)
		{
			npc.setCurrentHp(npc.getMaxHp());
			((L2Attackable) npc).clearAggroList();
		}
		else if (npcId == freyaStand || npcId == freyaThrone)
		{
			if (!npc.isCastingNow())
			{
				callSkillAI(npc);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.getNpcId() == freyaStand || npc.getNpcId() == freyaThrone)
		{
			callSkillAI(npc);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		int npcId = npc.getNpcId();
		if (npcId == glacier)
		{
			npc.setDisplayEffect(1);
			npc.setIsImmobilized(true);
			((L2Attackable) npc).setOnKillDelay(0);
			startQuestTimer("setDisplayEffect2", 1000, npc, null);
			startQuestTimer("cast", 10000, npc, null);
		}
		else if (npc.getNpcId() == freyaSpelling)
		{
			npc.setIsImmobilized(true);
		}
		else if (npcId == jinia || npcId == kegor)
		{
			npc.setIsNoRndWalk(true);
			((L2Attackable) npc).setisReturningToSpawnPoint(false);
			L2Npc target = findFreyaTarget(npc.getInstanceId());
			if (target != null)
			{
				npc.setTarget(target);
				((L2Attackable) npc).addDamageHate(target, 0, 999999);
			}
		}
		if (npc instanceof L2Attackable)
			((L2Attackable) npc).setisReturningToSpawnPoint(false);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == glacier)
		{
			npc.setDisplayEffect(3);
			L2Npc mob = spawnNpc(archer_breathe, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), npc.getInstanceId());
			mob.setRunning();
			mob.setTarget(killer);
			((L2Attackable) mob).addDamageHate(killer, 0, 99999);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	private void callSkillAI(L2Npc mob)
	{
		int[][] freya_skills = 
		{
			// id, lvl, time to string, chance
			{6274, 1, 4000, 10},
			{6276, 1, -1, 100},
			{6277, 1, -1, 100},
			{6278, 1, -1, 100},
			{6279, 1, 2000, 100},
			{6282, 1, -1, 100}
		};
		
		int iter = Rnd.get(0, 2);
		
		if (freya_skills[iter][3] < 100 && Rnd.get(100) > freya_skills[iter][3])
			iter = 3;
		
		mob.doCast(SkillTable.getInstance().getInfo(freya_skills[iter][0], freya_skills[iter][1]));
		if (freya_skills[iter][2] > 0)
		{
			startQuestTimer("show_string", freya_skills[iter][2], mob, null);
		}
		
		if (freya_skills[iter][0] == 6277)
		{
			startQuestTimer("summon_breathe", 1500, mob, null);
		}
	}
	
	private boolean hasBuff(int id, L2PcInstance player)
	{
		for (L2Effect e : player.getAllEffects())
		{
			if (e.getSkill().getId() == id)
				return true;
		}
		return false;
	}
	
	private L2Npc findFreyaTarget(int instanceId)
	{
		for(L2Npc mob : InstanceManager.getInstance().getInstance(instanceId).getNpcs())
		{
			if (mob.getNpcId() == freyaStand && !mob.isDead())
				return mob;
		}
		return null;
	}
	
	private void broadcastString(int instanceId)
	{
		Text sm = new Text(1801111, 3000, Text.ScreenMessageAlign.MIDDLE_CENTER, true, false, -1, false);
		Scenkos.toPlayersInInstance(sm, instanceId);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (npc.getNpcId() == archer_breathe || npc.getNpcId() == archer_knight)
		{
			if (npc.isImmobilized())
			{
				npc.abortAttack();
				npc.abortCast();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	private L2Npc spawnNpc(int npcId, int x, int y, int z, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setLocx(x);
			npcSpawn.setLocy(y);
			npcSpawn.setLocz(z);
			npcSpawn.setHeading(heading);
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch(Exception ignored)
		{
		}
		return null;
	}
	
	public IceQueenCastle(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = {archer_breathe, archer_knight, freyaSpelling, freyaStand, freyaThrone, glacier, jinia, kegor};
		this.registerMobs(mobs);
	}

	public static void main(String[] args)
	{
		new IceQueenCastle(-1, IceQueenCastle.class.getSimpleName(), "ai/zones");
		_log.info("Loaded Ice Queen Castle zones.");
	}
}