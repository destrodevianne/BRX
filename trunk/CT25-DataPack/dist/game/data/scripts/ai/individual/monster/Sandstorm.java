package ai.individual.monster;

import ai.group_template.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

public class Sandstorm extends L2AttackableAIScript
{
	private static final int Sandstorm = 32350;
	 
	public Sandstorm (int questId, String name, String descr)
	{
		super(questId, name, descr);
		super.addAttackId(Sandstorm);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
	int npcId = npc.getNpcId();

		if (npcId == Sandstorm)
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5435, 1));
		}

	return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new Sandstorm (-1, "Sandstorm ", "ai");
	}
}