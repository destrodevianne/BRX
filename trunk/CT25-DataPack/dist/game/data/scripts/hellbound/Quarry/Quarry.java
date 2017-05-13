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
package hellbound.Quarry;

import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2QuestGuardInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;

public class Quarry extends Quest
{
	// Npc
	private static final int SLAVE = 32299;

	// Total Trust Points
	private static final int TRUST = 10;

	// ZOne
	private static final int ZONE = 40107;

	// Drops
	protected static final int[] DROPLIST =
	{
		1876,
		1885,
		9628
	};

	// Strings
	private static final String MSG = "Thank you for saving me! Here is a small gift.";

	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_limit"))
		{
			for (final L2ZoneType zone : ZoneManager.getInstance().getZones(npc))
				if (zone.getId() == 40108)
				{
					npc.setTarget(null);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					npc.setAutoAttackable(false);
					npc.setRHandId(0);
					npc.teleToLocation(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());
					return null;
				}
			
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getId(), "Hun.. hungry"));
			npc.doDie(npc);
			return null;
		}
		else if (event.equalsIgnoreCase("FollowMe"))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
			npc.setTarget(player);
			npc.setAutoAttackable(true);
			npc.setRHandId(9136);
			npc.setWalking();

			if (getQuestTimer("time_limit", npc, null) == null)
				startQuestTimer("time_limit", 900000, npc, null); // 15 min limit for save
			return "32299-02.htm";
		}
		return event;
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		npc.setAutoAttackable(false);
		if (npc instanceof L2QuestGuardInstance)
			((L2QuestGuardInstance) npc).setPassive(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public final String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (HellboundManager.getInstance().getLevel() != 5)
			return "32299.htm";
		if (player.getQuestState(getName()) == null)
			newQuestState(player);
		
		return "32299-01.htm";
	}
	
	@Override
	public final String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (!npc.isDead())
			npc.doDie(attacker);
		
		return null;
	}
	
	@Override
	public final String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (skill.isOffensive() && !npc.isDead() && targets.length > 0)
			for (final L2Object obj : targets)
				if (obj == npc)
				{
					npc.doDie(caster);
					return null;
				}
		return null;
	}
	
	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		HellboundManager.getInstance().updateTrust(-TRUST, true);
		npc.setAutoAttackable(false);
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public final String onEnterZone(final L2Character character, final L2ZoneType zone)
	{
		if (character instanceof L2Npc && ((L2Npc) character).getId() == SLAVE)
			if (!character.isDead() && !((L2Npc) character).isDecayed() && character.getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
				if (HellboundManager.getInstance().getLevel() == 5)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new Decay((L2Npc) character), 1000);
					try
					{
						character.broadcastPacket(new CreatureSay(character.getObjectId(), 0, character.getName(), MSG));
					}
					catch (final Exception e)
					{
					}
				}
		return null;
	}
	
	private final class Decay implements Runnable
	{
		private final L2Npc _npc;
		
		public Decay(final L2Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			if (_npc != null && !_npc.isDead())
			{
				if (_npc.getTarget() instanceof L2PcInstance)
					((L2Attackable) _npc).dropItem((L2PcInstance) _npc.getTarget(), DROPLIST[getRandom(DROPLIST.length)], 1);
				
				_npc.setAutoAttackable(false);
				_npc.deleteMe();
				_npc.getSpawn().decreaseCount(_npc);
				HellboundManager.getInstance().updateTrust(TRUST, true);
			}
		}
	}
	
	public Quarry(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addSpawnId(SLAVE);
		addFirstTalkId(SLAVE);
		addStartNpc(SLAVE);
		addTalkId(SLAVE);
		addAttackId(SLAVE);
		addSkillSeeId(SLAVE);
		addKillId(SLAVE);
		addEnterZoneId(ZONE);
	}
	
	public static void main(final String[] args)
	{
		new Quarry(-1, Quarry.class.getSimpleName(), "hellbound");
	}
}