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
package hellbound.HellboundTown;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.SkillHolder;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;

public class Amaskari extends L2AttackableAIScript
{
	// Npcs
	private static final int AMASKARI = 22449;
	private static final int AMASKARI_PRISONER = 22450;

	// Misc
	private static final int BUFF_ID = 4632;
	private static SkillHolder[] BUFF =
	{
		new SkillHolder(BUFF_ID, 1),
		new SkillHolder(BUFF_ID, 2),
		new SkillHolder(BUFF_ID, 3)
	};
	
	// Strings
	private static final int[] AMASKARI_FSTRING_ID =
	{
		1000105, // I'll make everyone feel the same suffering as me!
		1800124, // Ha-ha yes, die slowly writhing in pain and agony!
		1800125, // More... need more... severe pain...
		1800127 // Something is burning inside my body!
	};
	private static final int[] MINIONS_FSTRING_ID =
	{
		1800126, // Ahh! My life is being drained out!
		1000503, // Thank you for saving me.
		1000138, // It... will... kill... everyone...
		1010451 // Eeek... I feel sick...yow...!
	};
	
	public Amaskari(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		addKillId(AMASKARI, AMASKARI_PRISONER);
		addAttackId(AMASKARI);
		addSpawnId(AMASKARI_PRISONER);
	}
	
	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("stop_toggle"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), AMASKARI_FSTRING_ID[2]));
			((L2MonsterInstance) npc).clearAggroList();
			((L2MonsterInstance) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			npc.setIsInvul(false);
		}

		else if (event.equalsIgnoreCase("onspawn_msg") && npc != null && !npc.isDead())
		{
			if (getRandom(100) > 20)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), MINIONS_FSTRING_ID[2]));
			else if (getRandom(100) > 40)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), MINIONS_FSTRING_ID[3]));

			startQuestTimer("onspawn_msg", (getRandom(8) + 1) * 30000, npc, null);
		}

		return null;
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		if (npc.getId() == AMASKARI && getRandom(1000) < 25)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), AMASKARI_FSTRING_ID[0]));
			for (final L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
				if (minion != null && !minion.isDead() && getRandom(10) == 0)
				{
					minion.broadcastPacket(new NpcSay(minion.getObjectId(), Say2.ALL, minion.getId(), MINIONS_FSTRING_ID[0]));
					minion.setCurrentHp(minion.getCurrentHp() - minion.getCurrentHp() / 5);
				}
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (npc.getId() == AMASKARI_PRISONER)
		{
			final L2MonsterInstance master = ((L2MonsterInstance) npc).getLeader();
			if (master != null && !master.isDead())
			{
				master.broadcastPacket(new NpcSay(master.getObjectId(), Say2.ALL, master.getId(), AMASKARI_FSTRING_ID[1]));
				final L2Effect e = master.getFirstEffect(BUFF_ID);

				if (e != null && e.getAbnormalLvl() == 3 && master.isInvul())
					master.setCurrentHp(master.getCurrentHp() + master.getCurrentHp() / 5);
				else
				{
					master.clearAggroList();
					master.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					if (e == null)
						master.doCast(BUFF[0].getSkill());
					
					else if (e.getAbnormalLvl() < 3)
						master.doCast(BUFF[e.getAbnormalLvl()].getSkill());
					
					else
					{
						master.broadcastPacket(new NpcSay(master.getObjectId(), Say2.ALL, master.getId(), AMASKARI_FSTRING_ID[3]));
						// master.doCast(INVINCIBILITY.getSkill())
						master.setIsInvul(true);
						startQuestTimer("stop_toggle", 10000, master, null);
					}
				}
			}
		}

		else if (npc.getId() == AMASKARI)
			for (final L2MonsterInstance minion : ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions())
				if (minion != null && !minion.isDead())
				{
					if (getRandom(1000) > 300)
						minion.broadcastPacket(new NpcSay(minion.getObjectId(), Say2.ALL, minion.getId(), MINIONS_FSTRING_ID[1]));
					
					HellboundManager.getInstance().updateTrust(30, true);
					minion.deleteMe();
				}
			
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (!npc.isTeleporting())
			startQuestTimer("onspawn_msg", (getRandom(3) + 1) * 30000, npc, null);
		
		return super.onSpawn(npc);
	}
	
	public static void main(final String[] args)
	{
		new Amaskari(-1, Amaskari.class.getSimpleName(), "hellbound");
	}
	
}