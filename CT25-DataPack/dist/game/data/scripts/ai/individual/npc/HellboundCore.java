package ai.individual.npc;

import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.skills.SkillHolder;

public class HellboundCore extends Quest
{

	private static final int NAIA = 18484;
	private static final int HELLBOUND_CORE = 32331;
	
	private static SkillHolder BEAM = new SkillHolder(5493, 1);

	public HellboundCore (int id, String name, String descr)
	{
		super(id,name,descr);
		
		addSpawnId(HELLBOUND_CORE);
	}

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && HellboundManager.getInstance().getLevel() <= 6)
		{
			for (L2Character naia : npc.getKnownList().getKnownCharactersInRadius(900))
			{
				if (naia != null && naia instanceof L2MonsterInstance && ((L2MonsterInstance) naia).getId() == NAIA && !naia.isDead())
				{
					naia.setTarget(npc);
					naia.doSimultaneousCast(BEAM.getSkill());
				}
			}
			
			startQuestTimer("cast", 10000, npc, null);
		}

		return null;
	}


	@Override
	public final String onSpawn(L2Npc npc)
	{
		startQuestTimer("cast", 10000, npc, null);
		return super.onSpawn(npc);
	}


	public static void main(String[] args)
	{
		new HellboundCore(-1, HellboundCore.class.getSimpleName(), "ai/individual/npc");
	}
}
