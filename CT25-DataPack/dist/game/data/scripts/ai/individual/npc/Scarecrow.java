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
package ai.individual.npc;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.model.actor.L2Npc;

/**
 * @author Browser
 */
public class Scarecrow extends L2AttackableAIScript
{
    public Scarecrow(int id, String name, String descr)
    {
        super(id, name, descr);
        registerMobs(new int[]{18912});
    }

    public String onSpawn(L2Npc npc)
    {
    	npc.setIsInvul(true);
    	npc.disableCoreAI(true);
        return super.onSpawn(npc);
    }

	public static void main(String[] args)
	{
		new Scarecrow(-1, "Scarecrow", "ai");
	}
}
