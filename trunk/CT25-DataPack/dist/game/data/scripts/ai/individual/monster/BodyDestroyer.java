package ai.individual.monster;

import ai.group_template.L2AttackableAIScript;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;

public class BodyDestroyer extends L2AttackableAIScript
{
	private static final int BDESTROYER = 22363;

	boolean _isLocked = false;

	public BodyDestroyer(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(BDESTROYER);
		addKillId(BDESTROYER);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_destroy"))
		
		player.setCurrentHp(0);
		
		return "";
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int npcId = npc.getId();

		if (npcId == BDESTROYER)
		{
			if (_isLocked == false)
			{
				((L2Attackable) npc).addDamageHate(player, 0, 9999);
				_isLocked = true;
				npc.setTarget(player);
			    npc.doCast(SkillTable.getInstance().getInfo(5256, 1));
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(),player.getName() + " u will Die."));
				startQuestTimer("time_to_destroy", 30000, npc, player);
			}
		}

		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == BDESTROYER)
		{
			cancelQuestTimer("time_to_destroy", npc, player);
			player.stopSkillEffects(5256);
			_isLocked = false;
		}
		return "";
	}

	public static void main(String[] args)
	{
		new BodyDestroyer(-1, BodyDestroyer.class.getSimpleName(), "ai/individual/monster");
	}
}