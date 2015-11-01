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
package ct25.xtreme.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.WalkingManager;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Object.InstanceType;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2CabaleBufferInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2FestivalGuideInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2NpcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.Quest.QuestEventType;

public class NpcKnownList extends CharKnownList
{
	private ScheduledFuture<?> _trackingTask = null;
	
	public NpcKnownList(L2Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}
		
		if (getActiveObject().isNpc() && (object instanceof L2Character))
		{
			final L2Npc npc = (L2Npc) getActiveObject();
			final Quest[] quests = npc.getTemplate().getEventQuests(QuestEventType.ON_SEE_CREATURE);
			if (quests != null)
			{
				for (Quest quest : quests)
				{
					quest.notifySeeCreature(npc, (L2Character) object, object.isPet());
				}
			}
		}
		return true;
	}
	
	@Override
	public L2Npc getActiveChar() { return (L2Npc)super.getActiveChar(); }
	
	@Override
	public int getDistanceToForgetObject(L2Object object) { return 2 * getDistanceToWatchObject(object); }
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
			return 4000;
		
		if (object instanceof L2NpcInstance || !(object instanceof L2Character))
			return 0;
		
		if (object instanceof L2CabaleBufferInstance)
			return 900;
		
		if (object instanceof L2Playable)
			return 1500;
		
		return 500;
	}
	
	// Support for Walking monsters aggro
		public void startTrackingTask()
		{
			if ((_trackingTask == null) && (getActiveChar().getAggroRange() > 0))
			{
				_trackingTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new TrackingTask(), 2000, 2000);
			}
		}
		
		// Support for Walking monsters aggro
		public void stopTrackingTask()
		{
			if (_trackingTask != null)
			{
				_trackingTask.cancel(true);
				_trackingTask = null;
			}
		}
		
		// Support for Walking monsters aggro
		protected class TrackingTask implements Runnable
		{
			@Override
			public void run()
			{
				if (getActiveChar() instanceof L2Attackable)
				{
					final L2Attackable monster = (L2Attackable) getActiveChar();
					if (monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
					{
						final Collection<L2PcInstance> players = getKnownPlayers().values();
						if (players != null)
						{
							for (L2PcInstance pl : players)
							{
								if (!pl.isDead() && !pl.isInvul() && pl.isInsideRadius(monster, monster.getAggroRange(), true, false) && (monster.isMonster() || (monster.isInstanceTypes(InstanceType.L2GuardInstance) && (pl.getKarma() > 0))))
								{
									// Send aggroRangeEnter
									if (monster.getHating(pl) == 0)
									{
										monster.addDamageHate(pl, 0, 0);
									}
									
									// Skip attack for other targets, if one is already chosen for attack
									if ((monster.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK) && !monster.isCoreAIDisabled())
									{
										WalkingManager.getInstance().stopMoving(getActiveChar(), false, true);
										monster.addDamageHate(pl, 0, 100);
										monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, pl, null);
									}
								}
							}
						}
					}
				}
			}
		}
	}
