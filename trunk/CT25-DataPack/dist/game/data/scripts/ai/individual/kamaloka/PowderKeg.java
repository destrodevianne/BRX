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
package ai.individual.kamaloka;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

public class PowderKeg extends L2AttackableAIScript
{

    private static final int POWDERK = 18622;

    public PowderKeg(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addAttackId(POWDERK);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.equalsIgnoreCase("Boom"))
        {
            npc.setTarget(npc);
            npc.doCast(SkillTable.getInstance().getInfo(5714, 1));
            npc.setCurrentHp(0);
            npc.decayMe();
        }
        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
    {
        int npcId;
        npcId = npc.getId();
        if (npcId == POWDERK)
        {
            startQuestTimer("Boom", 2000, npc, player);
        }
        return super.onAttack(npc, player, damage, isPet);
    }

    public static void main(String[] args)
    {
        new PowderKeg(-1, PowderKeg.class.getSimpleName(), "ai/individual/kamaloka");
    }
}