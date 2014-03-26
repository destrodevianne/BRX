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
package ai.individual.grandboss;


import java.util.List;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;

/**
 * @author InsOmnia
 */
public class Baylor extends L2AttackableAIScript
{

    private static final int BAYLOR = 29099;
    //									s13 / s14 / s cursed 14
    private static final int RED13[] =
    {
        5908, 9570, 10160
    };
    private static final int GREEN13[] =
    {
        5911, 9572, 10162
    };
    private static final int BLUE13[] =
    {
        5914, 9571, 10161
    };

    public Baylor(int questId, String name, String descr)
    {
        super(questId, name, descr);
        int[] mobs = new int[]
        {
            BAYLOR
        };
        this.registerMobs(mobs, QuestEventType.ON_KILL);
    }

    @Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc != null && npc.getId() == BAYLOR)
		{
			if (killer.getParty() != null)
			{
				List<L2PcInstance> party = killer.getParty().getPartyMembers();
				for (L2PcInstance member : party)
				{
					if (Util.checkIfInRange(1500, npc, member, true))
						crystalCheck(member);
				}
			}
			else
				crystalCheck(killer);
		}
		return super.onKill(npc, killer, isPet);
	}

    private void crystalCheck(L2PcInstance member)
    {
        QuestState st = member.getQuestState(getName());
        if (st == null)
        {
            st = this.newQuestState(member);
        }
        boolean lvled = false;
        L2ItemInstance[] inventory = member.getInventory().getItems();
        for (L2ItemInstance item : inventory)
        {
            int itemId = item.getId();
            if (!lvled && (itemId == RED13[0] || itemId == GREEN13[0] || itemId == BLUE13[0]))
            {
                int rnd = Rnd.get(1000);
                if (!lvled && itemId == RED13[0])
                {
                    if (!lvled && (rnd > 850))
                    { // 15% chance to get Soul Crystal Stage 14
                        st.takeItems(RED13[0], 1);
                        st.giveItems(RED13[1], 1);
                        lvled = true;
                    }
                    else if (!lvled && (rnd > 700))
                    { // 30% chance to get Cursed Soul Crystal Stage 14
                        st.takeItems(RED13[0], 1);
                        st.giveItems(RED13[2], 1);
                        member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
                        lvled = true;
                    }
                    else
                    {
                        member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
                        lvled = true;
                    }
                }
                else if (!lvled && itemId == GREEN13[0])
                {
                    if (!lvled && (rnd > 850))
                    { // 15% chance to get Soul Crystal Stage 14
                        st.takeItems(GREEN13[0], 1);
                        st.giveItems(GREEN13[1], 1);
                        lvled = true;
                    }
                    else if (!lvled && (rnd > 700))
                    { // 30% chance to get Cursed Soul Crystal Stage 14
                        st.takeItems(GREEN13[0], 1);
                        st.giveItems(GREEN13[2], 1);
                        member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
                        lvled = true;
                    }
                    else
                    {
                        member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
                        lvled = true;
                    }
                }
                else if (!lvled && itemId == BLUE13[0])
                {
                    if (!lvled && (rnd > 850))
                    { // 15% chance to get Soul Crystal Stage 14
                        st.takeItems(BLUE13[0], 1);
                        st.giveItems(BLUE13[1], 1);
                        lvled = true;
                    }
                    else if (!lvled && (rnd > 700))
                    { // 30% chance to get Cursed Soul Crystal Stage 14
                        st.takeItems(BLUE13[0], 1);
                        st.giveItems(BLUE13[2], 1);
                        member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
                        lvled = true;
                    }
                    else
                    {
                        member.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
                        lvled = true;
                    }
                }
            }
        }
    }

    public static void main(String[] args)
    {
        new Baylor(-1, Baylor.class.getSimpleName(), "ai/individual/grandboss");
    }
}