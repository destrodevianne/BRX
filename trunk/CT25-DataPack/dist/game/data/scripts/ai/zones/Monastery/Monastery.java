package ai.zones.Monastery;

import java.util.Collection;

import javolution.util.FastList;
import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PetInstance;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.templates.skills.L2SkillType;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;

public class Monastery extends L2AttackableAIScript
{
	static final int[] SOLINA_CLAN = 
	{
		22789, // Guide Solina
		22790, // Seeker Solina
		22791, // Savior Solina
		22793, // Ascetic Solina
	};
	
	static final int[] DIVINITY_CLAN = 
	{
		22794, // Divinity Judge
		22795, // Divinity Manager
	};
	
	static final int[] MOBMSG = 
	{
		1121006, // You cannot carry a weapon without authorization!
		10077, // $s1, why would you choose the path of darkness?!
		10078 // $s1! How dare you defy the will of Einhasad!
	};
	
	public Monastery(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(SOLINA_CLAN, QuestEventType.ON_AGGRO_RANGE_ENTER, QuestEventType.ON_SPAWN, QuestEventType.ON_SPELL_FINISHED);
		registerMobs(DIVINITY_CLAN, QuestEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (Util.contains(SOLINA_CLAN,npc.getNpcId()) && !npc.isInCombat() && npc.getTarget() == null)
		{
			if (player.getActiveWeaponInstance() != null)
			{
				npc.setTarget(player);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), MOBMSG[0]));
				switch (npc.getNpcId())
				{
					case 22124:
					case 22126:
					{
						L2Skill skill = SkillTable.getInstance().getInfo(4589,8);
						npc.doCast(skill);
						break;
					}
					default:
					{
						npc.setIsRunning(true);
						((L2Attackable) npc).addDamageHate(player, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						break;
					}
				}
			}
			else if (((L2Attackable)npc).getMostHated() == null)
				return null;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (Util.contains(DIVINITY_CLAN,npc.getNpcId()))
		{
			if (skill.getSkillType() == L2SkillType.AGGDAMAGE && targets.length != 0)
			{
				for (L2Object obj : targets)
				{
					if (obj.equals(npc))
					{
						NpcSay packet = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), MOBMSG[Rnd.get(2)+1]);
						packet.addStringParameter(caster.getName());
						npc.broadcastPacket(packet);
						((L2Attackable) npc).addDamageHate(caster, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
						break;
					}
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(SOLINA_CLAN,npc.getNpcId()))
		{
			FastList<L2Playable> result = new FastList<L2Playable>();
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			for (L2Object obj : objs)
			{
				if (obj instanceof L2PcInstance || obj instanceof L2PetInstance)
				{
					if (Util.checkIfInRange(npc.getAggroRange(), npc, obj, true) && !((L2Character) obj).isDead())
						result.add((L2Playable) obj);
				}
			}
			if (!result.isEmpty() && result.size() != 0)
			{
				Object[] characters = result.toArray();
				for (Object obj : characters)
				{
					L2Playable target = (L2Playable) (obj instanceof L2PcInstance ? obj : ((L2Summon) obj).getOwner());
					if (target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null)
					{
						npc.setTarget(target);
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), MOBMSG[0]));
						switch (npc.getNpcId())
						{
							case 22124:
							case 22126:
							case 22127:
							{
								L2Skill skill = SkillTable.getInstance().getInfo(4589,8);
								npc.doCast(skill);
								break;
							}
							default:
							{
								npc.setIsRunning(true);
								((L2Attackable) npc).addDamageHate(target, 0, 999);
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								break;
							}
						}
					}
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (Util.contains(SOLINA_CLAN,npc.getNpcId()) && skill.getId() == 4589)
		{
			npc.setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	public static void main(String[] args)
	{
		new Monastery(-1, "Monastery", "ai");
		_log.info("Loaded Monastery zones.");
	}
}