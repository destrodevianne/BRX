package hellbound.Quarry;

import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.network.NpcStringId;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.util.Rnd;

public class Quarry extends Quest
{
	private static final int SLAVE = 32299;
	private static final int TRUST = 10;
	private static final int ZONE = 40107;
	private static final int[] DROPLIST = { 1876, 1885, 9628 };
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_limit"))
		{
			for (L2ZoneType zone : ZoneManager.getInstance().getZones(npc))
			{
				if (zone.getId() == 40108)
				{
				npc.setTarget(null);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				npc.setAutoAttackable(false);
				npc.setRHandId(0);
				npc.teleToLocation(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());
				return null;
				}
			}
		
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.NPC_ALL, npc.getNpcId(), NpcStringId.HUN_HUNGRY));
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
			{
				startQuestTimer("time_limit", 900000, npc, null); // 15 min limit for save
			}
			return "32299-02.htm";
		}
		return event;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setAutoAttackable(false);

		return super.onSpawn(npc);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (HellboundManager.getInstance().getLevel() != 5)
			return "32299.htm";
		else
		{
			if (player.getQuestState(getName()) == null)
				newQuestState(player);

			return "32299-01.htm";
		}
	}

	@Override
	public final String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!npc.isDead())
			npc.doDie(attacker);

		return null;
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.isOffensive()
				&& !npc.isDead()
				&& targets.length > 0)
		{
			for (L2Object obj : targets)
			{
				if (obj == npc)
				{
					npc.doDie(caster);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		HellboundManager.getInstance().updateTrust(-TRUST, true);
		npc.setAutoAttackable(false);

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2Npc
				&& ((L2Npc)character).getNpcId() == SLAVE)
		{
			if (!character.isDead()
					&& !((L2Npc)character).isDecayed()
					&& character.getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if (HellboundManager.getInstance().getLevel() == 5)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new Decay((L2Npc)character), 1000);
					try
					{
						character.broadcastPacket(new CreatureSay(character.getObjectId(), Say2.NPC_ALL, character.getName(), NpcStringId.THANK_YOU_FOR_THE_RESCUE_ITS_A_SMALL_GIFT));
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		return null; 
	}

	private final class Decay implements Runnable
	{
		private final L2Npc _npc;

		public Decay(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			if (_npc != null && !_npc.isDead())
			{
				if (_npc.getTarget() instanceof L2PcInstance)
					((L2MonsterInstance)_npc).dropItem((L2PcInstance)(_npc.getTarget()), DROPLIST[Rnd.get(DROPLIST.length)], 1);

				_npc.setAutoAttackable(false);
				_npc.deleteMe();
				_npc.getSpawn().decreaseCount(_npc);
				HellboundManager.getInstance().updateTrust(TRUST, true);
			}
		}
	}

	public Quarry(int questId, String name, String descr)
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

	public static void main(String[] args)
	{
		new Quarry(-1, Quarry.class.getSimpleName(), "hellbound");
	}
}