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

import java.util.List;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.util.ArrayUtil;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Browser/InsOmnia Cannibalistic Stakato Chief AI AI used to manage Chief Spawns and reward player with cocoons.
 */

public class CannibalisticStakatoChief extends L2AttackableAIScript
{
	// Npcs
	private static final int CANNIBALISTIC_STAKATO_CHIEF = 25667;
	private static final int[] BIZARRE_COCOONS =
	{
		18795,
		18798
	};
	private static final int LARGECOCOON = 14834;
	private static final int SMALLCOCOON = 14833;

	// Constants
	private static TIntObjectHashMap<Integer> _captainSpawn = new TIntObjectHashMap<>();

	public CannibalisticStakatoChief(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addKillId(CANNIBALISTIC_STAKATO_CHIEF);
		for (final int i : BIZARRE_COCOONS)
			addSkillSeeId(i);
	}
	
	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		final int npcId = npc.getId();
		if (ArrayUtil.arrayContains(BIZARRE_COCOONS, npcId) && skill.getId() == 2905)
			if (!npc.isDead())
			{
				caster.getInventory().destroyItemByItemId("removeAccelerator", 14832, 1, caster, caster);
				npc.getSpawn().stopRespawn();
				npc.doDie(npc);
				final L2Npc captain = addSpawn(CANNIBALISTIC_STAKATO_CHIEF, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0);
				_captainSpawn.put(captain.getObjectId(), npc.getId());
				captain.setRunning();
				((L2Attackable) captain).addDamageHate(caster, 0, 500);
				captain.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
			}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		if (killer.getParty() != null)
		{
			final List<L2PcInstance> party = killer.getParty().getPartyMembers();
			for (final L2PcInstance member : party)
				if (getRandom(100) > 80)
					member.addItem("BigCocoon", LARGECOCOON, 1, npc, true);
				else
					member.addItem("SmallCocoon", SMALLCOCOON, 1, npc, true);
		}
		else if (getRandom(100) > 80)
			killer.addItem("BigCocoon", LARGECOCOON, 1, npc, true);
		else
			killer.addItem("SmallCocoon", SMALLCOCOON, 1, npc, true);
		addSpawn(_captainSpawn.get(npc.getObjectId()), npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0);
		_captainSpawn.remove(npc.getObjectId());
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(final String[] args)
	{
		new CannibalisticStakatoChief(-1, CannibalisticStakatoChief.class.getSimpleName(), "ai/zones");
	}
}