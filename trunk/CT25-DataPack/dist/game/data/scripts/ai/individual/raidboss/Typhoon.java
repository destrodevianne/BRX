package ai.individual.raidboss;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.instancemanager.RaidBossSpawnManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2RaidBossInstance;
import ct25.xtreme.gameserver.skills.SkillHolder;

public class Typhoon extends L2AttackableAIScript
{
	private static final int TYPHOON = 25539;
	
	private static SkillHolder STORM = new SkillHolder(5434, 1);

	public Typhoon (int id, String name, String descr)
	{
		super(id,name,descr);
		
		addAggroRangeEnterId(TYPHOON);
		addSpawnId(TYPHOON);
		
		L2RaidBossInstance boss = RaidBossSpawnManager.getInstance().getBosses().get(TYPHOON);

		if (HellboundManager.getInstance().getLevel() > 3 && boss != null)
			this.onSpawn(boss);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && npc != null && !npc.isDead())
		{
			npc.doSimultaneousCast(STORM.getSkill());
			startQuestTimer("cast", 10000, npc, null);
		}
		return null;
	}

 	@Override
  	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
  	{
		npc.doSimultaneousCast(STORM.getSkill());

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
			startQuestTimer("cast", 5000, npc, null);

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new Typhoon(-1, Typhoon.class.getSimpleName(), "ai/individual/raidboss");
	}
}
