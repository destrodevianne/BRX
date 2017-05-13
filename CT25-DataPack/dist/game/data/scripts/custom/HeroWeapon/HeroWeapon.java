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
package custom.HeroWeapon;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.util.Util;

public class HeroWeapon extends Quest
{
	// Npcs
	private final static int[] npcIds =
	{
		31690,
		31769,
		31770,
		31771,
		31772
	};

	// Weapons
	private final static int[] weaponIds =
	{
		6611,
		6612,
		6613,
		6614,
		6615,
		6616,
		6617,
		6618,
		6619,
		6620,
		6621,
		9388,
		9389,
		9390
	};

	public HeroWeapon(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		for (final int i : npcIds)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());

		final int weaponId = Integer.valueOf(event);
		if (Util.contains(weaponIds, weaponId))
			st.giveItems(weaponId, 1);

		st.exitQuest(true);
		return null;
	}

	@SuppressWarnings("null")
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			newQuestState(player);

		if (player.isHero())
		{
			if (hasHeroWeapon(player))
			{
				htmltext = "already_have_weapon.htm";
				st.exitQuest(true);
			}
			else
				htmltext = "weapon_list.htm";
		}
		else
		{
			htmltext = "no_hero.htm";
			st.exitQuest(true);
		}

		return htmltext;
	}

	private boolean hasHeroWeapon(final L2PcInstance player)
	{
		for (final int i : weaponIds)
			if (player.getInventory().getItemByItemId(i) != null)
				return true;
			
		return false;
	}

	public static void main(final String[] args)
	{
		new HeroWeapon(-1, "HeroWeapon", "custom");
	}
}
