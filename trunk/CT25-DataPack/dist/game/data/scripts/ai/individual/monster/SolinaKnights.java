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
package ai.individual.monster;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.util.Rnd;

/**
 * @author Browser
 */
public class SolinaKnights extends L2AttackableAIScript
{

	private static final int KNIGHT = 18909;
	
	public SolinaKnights(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addAttackId(KNIGHT);
    }
    
    @Override
    public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
    {
        if (npc.getNpcId() == KNIGHT)
        {
            if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
            {
                if (Rnd.get(100) > 90)
                {
                    npc.broadcastNpcSay("Punish all those who tread footsteps in this place.");
                }
                else if (Rnd.get(100) > 90)
                {
                    npc.broadcastNpcSay("We are the sword of truth, the sword of Solina.");
                }
            }
        }
        return super.onAttack(npc, player, damage, isPet);
    }
    
    public static void main(String[] args)
    {
        new SolinaKnights(-1, "SolinaKnights", "ai");
    }
}