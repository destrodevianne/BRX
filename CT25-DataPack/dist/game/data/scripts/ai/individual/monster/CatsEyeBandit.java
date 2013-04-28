/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual.monster;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.itemcontainer.Inventory;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;

/**
 * Cat's Eye Bandit (Quest Monster) AI.
 * @author Gladicek
 */
public final class CatsEyeBandit extends L2AttackableAIScript
{
	// NPC ID
	private static final int MOB_ID = 27038;
	// Weapons
	private static final int BOW = 1181;
	private static final int DAGGER = 1182;
	
	private CatsEyeBandit(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(MOB_ID);
		addKillId(MOB_ID);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final QuestState qs = attacker.getQuestState("403_PathToRogue"); // TODO: Replace with class name.
		if (npc.isScriptValue(0) && (qs != null) && ((qs.getItemEquipped(Inventory.PAPERDOLL_RHAND) == BOW) || (qs.getItemEquipped(Inventory.PAPERDOLL_RHAND) == DAGGER)))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), 40306));
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final QuestState qs = killer.getQuestState("403_PathToRogue"); // TODO: Replace with class name.
		if (qs != null)
		{
			 npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), 40307));
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new CatsEyeBandit(-1, CatsEyeBandit.class.getSimpleName(), "ai/individual/monster");
	}
}