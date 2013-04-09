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
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * Originally written by RosT in python
 * @author Evilus
 */
public class WeirdBunei extends L2AttackableAIScript
{

    private static final int WEIRD = 18564;
    private boolean _isAlreadyStarted = false;

    public WeirdBunei(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addAttackId(WEIRD);
        addKillId(WEIRD);
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.equalsIgnoreCase("time_to_skill"))
        {
            if (_isAlreadyStarted == true)
            {
                _isAlreadyStarted = false;
                npc.setTarget(player);
                npc.doCast(SkillTable.getInstance().getInfo(5625, 1));
            }
            else
            {
                return null;
            }
        }

        return null;
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
    {
        int npcId = npc.getNpcId();
        if (npcId == WEIRD)
        {
            if (_isAlreadyStarted == false)
            {
                startQuestTimer("time_to_skill", 30000, npc, player);
                _isAlreadyStarted = true;
            }
            else if (_isAlreadyStarted == true)
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {
        int npcId = npc.getNpcId();
        if (npcId == WEIRD)
        {
            cancelQuestTimer("time_to_skill", npc, player);
        }
        return null;
    }

    public static void main(String[] args)
    {
        new WeirdBunei(-1, "WeirdBunei", "ai");
    }
}
