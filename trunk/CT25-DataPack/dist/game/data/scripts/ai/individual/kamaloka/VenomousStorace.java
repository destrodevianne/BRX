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

import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * Originally written by RosT in python
 * @author Evilus
 */
public class VenomousStorace extends L2AttackableAIScript
{

    private static final int VENOMOUS = 18571;
    private static final int GUARD = 18572;
    private boolean _isAlreadySpawned = false;
    private int _isLockSpawned = 0;

    public VenomousStorace(int questId, String name, String descr)
    {
        super(questId, name, descr);
        addAttackId(VENOMOUS);
        addKillId(GUARD);
        addKillId(VENOMOUS);
    }

    @Override
    public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        int x = player.getX();
        int y = player.getY();
        if (event.equalsIgnoreCase("time_to_spawn"))
        {
            addSpawn(GUARD, x + 100, y + 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
            addSpawn(GUARD, x - 100, y - 50, npc.getZ(), 0, false, 0, false, npc.getInstanceId());
            _isAlreadySpawned = false;
            _isLockSpawned = 2;
        }
        return null;
    }

    @Override
    public final String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
    {
        int npcId = npc.getNpcId();
        if (npcId == VENOMOUS)
        {
            if (_isAlreadySpawned == false)
            {
                if (_isLockSpawned == 0)
                {
                    startQuestTimer("time_to_spawn", 20000, npc, player);
                    _isAlreadySpawned = true;
                }
            }
            if (_isLockSpawned == 2)
            {
                return null;
            }
            else
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
        if (npcId == GUARD)
        {
            _isLockSpawned = 1;
        }
        else if (npcId == VENOMOUS)
        {
            cancelQuestTimer("time_to_spawn", npc, player);
        }
        return null;
    }

    public static void main(String[] args)
    {
        new VenomousStorace(-1, "VenomousStorace", "ai");
    }
}
