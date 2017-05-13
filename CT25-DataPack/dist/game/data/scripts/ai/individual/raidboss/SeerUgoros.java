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
package ai.individual.raidboss;

import java.util.concurrent.ScheduledFuture;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.datatables.MapRegionTable;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.ZoneManager;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.zone.L2ZoneType;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.util.Util;
import quests.Q00288_HandleWithCare.Q00288_HandleWithCare;
import quests.Q00423_TakeYourBestShot.Q00423_TakeYourBestShot;

/**
 ** @author Gnacik 2010-11-14 Based on official server Naia
 */
public class SeerUgoros extends L2AttackableAIScript
{
	// Boss attack task
	ScheduledFuture<?> _thinkTask = null;
	
	// Item
	private static final int UGOROS_PASS = 15496;
	private static final int MID_SCALE = 15498;
	private static final int HIGH_SCALE = 15497;
	
	// Zone ID
	private static final int BOSS_ZONE = 70307;
	
	// NPC ID
	private static final int SEER_UGOROS = 18863;
	private static final int GATEKEEPER = 32740;
	private static final int WEED = 18867;
	
	protected static L2Npc ugoros = null;
	protected static L2Npc weed = null;
	protected static boolean weed_attack = false;
	protected static boolean weed_killed_by_player = false;
	protected static boolean killed_one_weed = false;
	protected static L2PcInstance Player = null;
	
	// State
	private static final byte ALIVE = 0;
	private static final byte FIGHTING = 1;
	private static final byte DEAD = 2;
	protected static byte STATE = DEAD;
	
	// Skill
	private static final L2Skill _ugoros_skill = SkillTable.getInstance().getInfo(6426, 1);
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("ugoros_respawn") && ugoros == null)
		{
			ugoros = addSpawn(SEER_UGOROS, 96804, 85604, -3720, 34360, false, 0);
			
			broadcastInRegion(ugoros, "Listen, oh Tantas! I have returned! The Prophet Yugoros of the Black Abyss is with me, so do not be afraid!");
			
			STATE = ALIVE;
			
			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if (event.equalsIgnoreCase("ugoros_shout"))
		{
			if (STATE == FIGHTING)
			{
				final L2ZoneType _zone = ZoneManager.getInstance().getZoneById(BOSS_ZONE);
				if (Player == null)
					STATE = ALIVE;
				else if (!_zone.isCharacterInZone(Player))
				{
					STATE = ALIVE;
					Player = null;
				}
			}
			else if (STATE == ALIVE)
				broadcastInRegion(ugoros, "Listen, oh Tantas! The Black Abyss is famished! Find some fresh offerings!");
			startQuestTimer("ugoros_shout", 120000, null, null);
		}
		else if (event.equalsIgnoreCase("ugoros_attack"))
		{
			if (Player != null)
			{
				changeAttackTarget(Player);
				
				broadcastInRegion(ugoros, "Welcome, " + Player.getName() + "! Let us see if you have broght a worthy offering for the Black Abyss!");
				
				if (_thinkTask != null)
					_thinkTask.cancel(true);
				
				_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 1000, 3000);
			}
		}
		else if (event.equalsIgnoreCase("weed_check"))
		{
			if (weed_attack == true && ugoros != null && weed != null)
			{
				if (weed.isDead() && !weed_killed_by_player)
				{
					killed_one_weed = true;
					weed = null;
					weed_attack = false;
					ugoros.getStatus().setCurrentHp(ugoros.getStatus().getCurrentHp() + ugoros.getMaxHp() * 0.2);
					ugoros.broadcastNpcSay("What a formidable foe! But i have the Abyss Weed given to me by the Black Abyss! Let me see...");
				}
				else
					startQuestTimer("weed_check", 2000, null, null);
			}
			else
			{
				weed = null;
				weed_attack = false;
			}
		}
		else if (event.equalsIgnoreCase("ugoros_expel"))
		{
			if (Player != null)
			{
				Player.teleToLocation(94701, 83053, -3580);
				Player = null;
			}
		}
		else if (event.equalsIgnoreCase("teleportInside"))
		{
			if (player != null && STATE == ALIVE)
			{
				if (player.getInventory().getItemByItemId(UGOROS_PASS) != null)
				{
					STATE = FIGHTING;
					
					Player = player;
					killed_one_weed = false;
					
					player.teleToLocation(95984, 85692, -3720);
					player.destroyItemByItemId("SeerUgoros", UGOROS_PASS, 1, npc, true);
					
					startQuestTimer("ugoros_attack", 2000, null, null);
					
					final QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
					if (st != null)
						st.set("drop", "1");
				}
				else
				{
					final QuestState st = player.getQuestState(Q00423_TakeYourBestShot.class.getSimpleName());
					if (st == null)
						return "<html><body>Gatekeeper Batracos:<br>You look too inexperienced to make a journey to see Tanta Seer Ugoros. If you can convince Chief Investigator Johnny that you should go, then I will let you pass. Johnny has been everywhere and done everything. He may not be of my people but he has my respect, and anyone who has his will in turn have mine as well.<br></body></html>";
					return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
				}
			}
			else
				return "<html><body>Gatekeeper Batracos:<br>Tanta Seer Ugoros is hard to find. You'll just have to keep looking.<br></body></html>";
		}
		else if (event.equalsIgnoreCase("teleport_back"))
			if (player != null)
			{
				player.teleToLocation(94701, 83053, -3580);
				Player = null;
			}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.isDead())
			return null;
		
		if (npc.getId() == WEED)
		{
			if (ugoros != null && weed != null && npc.equals(weed))
			{
				// Reset weed
				weed = null;
				// Reset attack state
				weed_attack = false;
				// Set it
				weed_killed_by_player = true;
				// Complain
				ugoros.broadcastNpcSay("No! How dare you to stop me from using the Abyss Weed... Do you know what you have done?!");
				// Cancel current think-task
				if (_thinkTask != null)
					_thinkTask.cancel(true);
				// Re-setup task to re-think attack again
				_thinkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ThinkTask(), 500, 3000);
			}
			
			npc.doDie(attacker);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (npc.getId() == SEER_UGOROS)
		{
			if (_thinkTask != null)
			{
				_thinkTask.cancel(true);
				_thinkTask = null;
			}
			
			STATE = DEAD;
			
			broadcastInRegion(ugoros, "Ah... How could I lose... Oh, Black Abyss, receive me...");
			
			ugoros = null;
			
			addSpawn(GATEKEEPER, 96782, 85918, -3720, 34360, false, 50000);
			
			startQuestTimer("ugoros_expel", 50000, null, null);
			startQuestTimer("ugoros_respawn", 60000, null, null);
			
			final QuestState st = player.getQuestState(Q00288_HandleWithCare.class.getSimpleName());
			if (st != null && st.getInt("cond") == 1 && st.getInt("drop") == 1)
			{
				if (killed_one_weed)
				{
					player.addItem("SeerUgoros", MID_SCALE, 1, npc, true);
					st.set("cond", "2");
				}
				else
				{
					player.addItem("SeerUgoros", HIGH_SCALE, 1, npc, true);
					st.set("cond", "3");
				}
				st.unset("drop");
			}
		}
		return null;
	}
	
	private void broadcastInRegion(final L2Npc npc, final String _text)
	{
		if (npc == null)
			return;
		final NpcSay cs = new NpcSay(npc.getObjectId(), 1, npc.getId(), _text);
		final int region = MapRegionTable.getInstance().getMapRegion(npc.getX(), npc.getY());
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers().values())
			if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
				if (Util.checkIfInRange(6000, npc, player, false))
					player.sendPacket(cs);
	}
	
	private class ThinkTask implements Runnable
	{
		
		protected ThinkTask()
		{
		}
		
		@Override
		public void run()
		{
			final L2ZoneType _zone = ZoneManager.getInstance().getZoneById(BOSS_ZONE);
			
			if (STATE == FIGHTING && Player != null && _zone.isCharacterInZone(Player) && !Player.isDead())
			{
				if (weed_attack && weed != null)
				{
					// Dummy, just wait
				}
				else if (getRandom(10) < 6)
				{
					weed = null;
					
					for (final L2Character _char : ugoros.getKnownList().getKnownCharactersInRadius(2000))
						if (_char instanceof L2Attackable && !_char.isDead() && ((L2Attackable) _char).getId() == WEED)
						{
							weed_attack = true;
							weed = (L2Attackable) _char;
							changeAttackTarget(weed);
							startQuestTimer("weed_check", 1000, null, null);
							break;
						}
					if (weed == null)
						changeAttackTarget(Player);
				}
				else
					changeAttackTarget(Player);
			}
			else
			{
				STATE = ALIVE;
				
				Player = null;
				
				if (_thinkTask != null)
				{
					_thinkTask.cancel(true);
					_thinkTask = null;
				}
			}
		}
	}
	
	protected void changeAttackTarget(final L2Character _attack)
	{
		((L2Attackable) ugoros).getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		((L2Attackable) ugoros).clearAggroList();
		((L2Attackable) ugoros).setTarget(_attack);
		
		if (_attack instanceof L2Attackable)
		{
			weed_killed_by_player = false;
			
			ugoros.disableSkill(_ugoros_skill, 100000);
			
			((L2Attackable) ugoros).setIsRunning(true);
			((L2Attackable) ugoros).addDamageHate(_attack, 0, Integer.MAX_VALUE);
		}
		else
		{
			ugoros.enableSkill(_ugoros_skill);
			
			((L2Attackable) ugoros).addDamageHate(_attack, 0, 99);
			((L2Attackable) ugoros).setIsRunning(false);
		}
		((L2Attackable) ugoros).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _attack);
	}
	
	public SeerUgoros(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(GATEKEEPER);
		addTalkId(GATEKEEPER);
		addKillId(SEER_UGOROS);
		addAttackId(WEED);
		
		startQuestTimer("ugoros_respawn", 60000, null, null);
	}
	
	public static void main(final String[] args)
	{
		new SeerUgoros(-1, SeerUgoros.class.getSimpleName(), "ai/individual/raidboss");
	}
}
