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
package instances.JiniasHideout;

import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.clientpackets.Say2;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import quests.Q10284_AcquisitionOfDivineSword.Q10284_AcquisitionOfDivineSword;
import quests.Q10285_MeetingSirra.Q10285_MeetingSirra;
import quests.Q10286_ReunionWithSirra.Q10286_ReunionWithSirra;
import quests.Q10287_StoryOfThoseLeft.Q10287_StoryOfThoseLeft;

/**
 * Jinia Guild
 * @author Browser
 */
public class JiniasHideout extends Quest
{
	private class JiniasWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
			0,
			0
		};
		public int questId;
		
		public JiniasWorld()
		{
			
		}
	}
	
	// Npcs
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	private static final int SIRRA = 32762;
	
	// Locs
	private static final int[] ENTRY_POINT =
	{
		-23530,
		-8963,
		-5413
	};
	
	// Misc
	private static final int MIN_LV = 82;
	private static final int MAX_LV = 85;
	
	// Other Constants
	protected class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	private void teleportplayer(final L2PcInstance player, final teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	
	private boolean checkConditions(final L2PcInstance player)
	{
		if (player.getLevel() < MIN_LV || player.getLevel() > MAX_LV)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	protected void exitInstance(final L2PcInstance player, final teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}
	
	protected int enterInstance(final L2PcInstance player, final String template, final teleCoord teleto, final int questId)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			// this instance
			if (!(world instanceof JiniasWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}
		// New instance
		if (!checkConditions(player))
			return 0;
		
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setSpawnLoc(new int[]
		{
			player.getX(),
			player.getY(),
			player.getZ()
		});
		world = new JiniasWorld();
		world.instanceId = instanceId;
		((JiniasWorld) world).questId = questId;
		
		// Template id depends of quest
		switch (questId)
		{
			case 10284:
				world.templateId = 140;
				break;
			case 10285:
				world.templateId = 141;
				break;
			case 10286:
				world.templateId = 145;
				break;
			case 10287:
				world.templateId = 146;
				break;
		}
		
		world.status = 0;
		((JiniasWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("JiniasWorld started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player, teleto);
		world.allowed.add(player.getObjectId());
		return instanceId;
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = null;
		
		if (event.startsWith("enterInstance_") && npc.getId() == RAFFORTY)
		{
			int questId = -1;
			String tmpl = null;
			QuestState hostQuest = null;
			
			try
			{
				System.out.println(event.substring(14));
				questId = Integer.parseInt(event.substring(14));
				
				final teleCoord tele = new teleCoord();
				tele.x = ENTRY_POINT[0];
				tele.y = ENTRY_POINT[1];
				tele.z = ENTRY_POINT[2];
				
				switch (questId)
				{
					case 10284:
						hostQuest = player.getQuestState(Q10284_AcquisitionOfDivineSword.class.getSimpleName());
						tmpl = "JiniasHideout1.xml";
						htmltext = "10284_failed.htm";
						break;
					case 10285:
						hostQuest = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
						tmpl = "JiniasHideout2.xml";
						htmltext = "10285_failed.htm";
						break;
					case 10286:
						hostQuest = player.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
						tmpl = "JiniasHideout3.xml";
						htmltext = "10286_failed.htm";
						break;
					case 10287:
						hostQuest = player.getQuestState(Q10287_StoryOfThoseLeft.class.getSimpleName());
						tmpl = "JiniasHideout4.xml";
						htmltext = "10287_failed.htm";
						break;
				}
				
				if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getProgress() == 1)
					hostQuest.setCond(2, true);
				
				if (tmpl != null)
					if (enterInstance(player, tmpl, tele, questId) > 0)
						htmltext = null;
			}
			catch (final Exception e)
			{
				
			}
		}
		
		else if (event.equalsIgnoreCase("leaveInstance") && npc.getId() == JINIA)
		{
			QuestState hostQuest = null;
			final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
			final teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = inst.getSpawnLoc()[0];
			tele.y = inst.getSpawnLoc()[1];
			tele.z = inst.getSpawnLoc()[2];
			exitInstance(player, tele);
			
			switch (((JiniasWorld) world).questId)
			{
				case 10285:
					hostQuest = player.getQuestState(Q10285_MeetingSirra.class.getSimpleName());
					break;
				case 10286:
					hostQuest = player.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
					break;
				case 10287:
					hostQuest = player.getQuestState(Q10287_StoryOfThoseLeft.class.getSimpleName());
					break;
			}
			
			if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getProgress() == 2)
				switch (((JiniasWorld) world).questId)
				{
					case 10285:
						htmltext = "10285_goodbye.htm";
						break;
					case 10286:
						htmltext = "10286_goodbye.htm";
						hostQuest.setCond(5, true);
						break;
					case 10287:
						htmltext = "10287_goodbye.htm";
						hostQuest.setCond(5, true);
				}
		}
		
		return htmltext;
	}
	
	@Override
	public final String onSpawn(final L2Npc npc)
	{
		if (npc.getId() == SIRRA)
		{
			final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld != null && tmpworld instanceof JiniasWorld)
			{
				String msg = null;
				switch (tmpworld.templateId)
				{
					case 141:
						msg = "There's nothing you can't say. I can't listen to you anymore!";
						break;
					case 145:
						msg = "You advanced bravely but got such a tiny result. Hohoho.";
				}
				
				if (msg != null)
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), msg));
			}
		}
		return null;
	}
	
	public JiniasHideout(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY, JINIA);
		addSpawnId(SIRRA);
	}
	
	public static void main(final String[] args)
	{
		new JiniasHideout(-1, JiniasHideout.class.getSimpleName(), "instances");
	}
}