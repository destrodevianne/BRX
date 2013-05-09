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

import gnu.trove.TIntObjectHashMap;

import java.util.List;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.util.ArrayUtil;
import ct25.xtreme.util.Rnd;

/**
 * @author Browser/InsOmnia
 * Cannibalistic Stakato Chief AI
 * 
 * AI used to manage Chief Spawns and
 * reward player with cocoons.
 */

public class CannibalisticStakatoChief extends L2AttackableAIScript
{
	private static final int CannibalisticChief = 25667;
	private static final int[] BizarreCocoons = { 18795, 18798 };
	private static final int LargeCocoon = 14834;
	private static final int SmallCocoon = 14833;
	private static TIntObjectHashMap<Integer> _captainSpawn = new TIntObjectHashMap<Integer>();
	public CannibalisticStakatoChief(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(CannibalisticChief);
		for (int i : BizarreCocoons)
			addSkillSeeId(i);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (ArrayUtil.arrayContains(BizarreCocoons,npcId) && skill.getId() == 2905)
		{
			if (!npc.isDead())
			{
				caster.getInventory().destroyItemByItemId("removeAccelerator", 14832, 1, caster, caster);
				npc.getSpawn().stopRespawn();
				npc.doDie(npc);
				L2Npc captain = addSpawn(CannibalisticChief, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0);
				_captainSpawn.put(captain.getObjectId(), npc.getNpcId());
				captain.setRunning();
				((L2Attackable) captain).addDamageHate(caster, 0, 500);
				captain.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet) {
		if (killer.getParty() != null)
		{
			List<L2PcInstance> party = killer.getParty().getPartyMembers();
			for (L2PcInstance member : party)
			{
				if (Rnd.get(100) > 80)
					member.addItem("BigCocoon", LargeCocoon, 1, npc, true);
				else
					member.addItem("SmallCocoon", SmallCocoon, 1, npc, true);
			}
		}
		else
		{
			if (Rnd.get(100) > 80)
				killer.addItem("BigCocoon", LargeCocoon, 1, npc, true);
			else
				killer.addItem("SmallCocoon", SmallCocoon, 1, npc, true);
		}
		addSpawn(_captainSpawn.get(npc.getObjectId()), npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0);
		_captainSpawn.remove(npc.getObjectId());
		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new CannibalisticStakatoChief(-1, CannibalisticStakatoChief.class.getSimpleName(), "ai/zones");
		_log.info("Loading Stakato Nest AI Scripts...");
		_log.info("Stakato Nest: Loaded CannibalisticStakatoChief.");
	}
}