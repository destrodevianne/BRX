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
package ai.zones.StakatoNest;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.util.Rnd;

/**
 * 
 * @author Browser
 */
public class SpikedStakatoNurse extends L2AttackableAIScript
{
	private static final int SPIKED_STAKATO_BABY = 22632;
	//private static final int SPIKED_STAKATO_NURSE = 22630;
	private static final int SPIKED_STAKATO_NURSE_2ND_FORM = 22631;
	
	public SpikedStakatoNurse(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(SPIKED_STAKATO_BABY);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2Npc nurse = getNurse(npc);
		if (nurse != null && !nurse.isDead())
		{
			getNurse(npc).doDie(getNurse(npc));
			final L2Npc newForm = addSpawn(SPIKED_STAKATO_NURSE_2ND_FORM, npc.getX() + Rnd.get(10, 50), npc.getY() + Rnd.get(10, 50), npc.getZ(), 0, false, 0, true);
			newForm.setRunning();
			((L2Attackable) newForm).addDamageHate(killer, 1, 99999);
			newForm.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	public L2Npc getNurse(L2Npc couple)
	{
		// For now, minions are set as minionInstance. If they change to only monster, use the above code
		return ((L2MonsterInstance)couple).getLeader();
		
		/**
		final int spawnId = couple.getSpawn().getId() - 1;
		final L2Spawn spawnCouple = SpawnTable.getInstance().getSpawnTable().get(spawnId);
		if (spawnCouple != null && spawnCouple.getLastSpawn() != null)
		{
			if (spawnCouple.getLastSpawn().getNpcId() == SPIKED_STAKATO_NURSE)
				return spawnCouple.getLastSpawn();
		}
		return null;
		*/
	}

	public static void main(String[] args)
	{
		new SpikedStakatoNurse(-1, SpikedStakatoNurse.class.getSimpleName(), "ai/zones");
		_log.info("Stakato Nest: Loaded SpikedStakatoNurse.");
		_log.info("All Stakato Nest AI Scripts Loaded!");
	}
}
