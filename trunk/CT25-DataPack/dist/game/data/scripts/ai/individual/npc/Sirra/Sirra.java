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
package ai.individual.npc.Sirra;

import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

/**
 * Sirra AI.
 * @author St3eT
 */
public final class Sirra extends Quest
{
	// NPC
	private static final int SIRRA = 32762;

	// Misc
	private static final int FREYA_INSTID = 139;
	private static final int FREYA_HARD_INSTID = 144;

	private Sirra(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(SIRRA);
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if (world != null && world.templateId == FREYA_INSTID)
			return world.status == 0 ? "32762-easy.html" : "32762-easyfight.html";
		else if (world != null && world.templateId == FREYA_HARD_INSTID)
			return world.status == 0 ? "32762-hard.html" : "32762-hardfight.html";
		return "32762.html";
	}

	public static void main(final String[] args)
	{
		new Sirra(-1, Sirra.class.getSimpleName(), "ai/individual/npc");
	}
}