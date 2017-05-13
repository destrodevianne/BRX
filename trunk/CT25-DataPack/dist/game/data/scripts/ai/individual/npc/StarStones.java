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

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Util;

public class StarStones extends L2AttackableAIScript
{
	// Npcs
	private static final int[] mobs =
	{
		18684,
		18685,
		18686,
		18687,
		18688,
		18689,
		18690,
		18691,
		18692
	};

	// Misc
	private static final int RATE = 1;

	public StarStones(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		this.registerMobs(mobs, QuestEventType.ON_SKILL_SEE);
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (Util.contains(targets, npc) && skill.getId() == 932)
		{
			int itemId = 0;

			switch (npc.getId())
			{
				case 18684:
				case 18685:
				case 18686:
					// give Red item
					itemId = 14009;
					break;
				case 18687:
				case 18688:
				case 18689:
					// give Blue item
					itemId = 14010;
					break;
				case 18690:
				case 18691:
				case 18692:
					// give Green item
					itemId = 14011;
					break;
				default:
					// unknown npc!
					return super.onSkillSee(npc, caster, skill, targets, isPet);
			}
			if (getRandom(100) < 33)
			{
				caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED));
				caster.addItem("StarStone", itemId, getRandom(RATE + 1, 2 * RATE), null, true);
			}
			else if (skill.getLevel() == 1 && getRandom(100) < 15 || skill.getLevel() == 2 && getRandom(100) < 50 || skill.getLevel() == 3 && getRandom(100) < 75)
			{
				caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED));
				caster.addItem("StarStone", itemId, getRandom(1, RATE), null, true);
			}
			else
				caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_COLLECTION_HAS_FAILED));
			npc.deleteMe();
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	public static void main(final String[] args)
	{
		new StarStones(-1, StarStones.class.getSimpleName(), "ai/individual/npc");
	}
}
