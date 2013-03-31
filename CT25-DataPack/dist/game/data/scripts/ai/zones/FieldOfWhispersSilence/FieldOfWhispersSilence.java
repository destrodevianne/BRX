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
package ai.zones.FieldOfWhispersSilence;

import ai.group_template.L2AttackableAIScript;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Browser
 */
public class FieldOfWhispersSilence extends L2AttackableAIScript
{
	private static final int BrazierOfPurity = 18806;
	private static final int GuardianSpiritsOfMagicForce = 22659;

	public FieldOfWhispersSilence(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] mobs = new int[] { BrazierOfPurity, GuardianSpiritsOfMagicForce };
		this.registerMobs(mobs,QuestEventType.ON_AGGRO_RANGE_ENTER);
		super.addAggroRangeEnterId(BrazierOfPurity);
		super.addAggroRangeEnterId(GuardianSpiritsOfMagicForce);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == BrazierOfPurity)
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "The Magic Force is being threatened... Protect the Magic Force, Guardian Spirits...!"));
		else if (npcId == GuardianSpiritsOfMagicForce)
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Magic Force must be protected even this life is sacrif iced in return!"));
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new FieldOfWhispersSilence(-1, "FieldOfWhispersSilence", "zones");
		_log.info("Loaded Field of Whispers / Silence zones.");
	}
}