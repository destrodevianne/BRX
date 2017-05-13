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
package handlers.chathandlers;

import java.util.Collection;

import ct25.xtreme.gameserver.handler.IChatHandler;
import ct25.xtreme.gameserver.model.BlockList;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;

/**
 * Hero chat handler.
 * @author durgus
 */
public class ChatHeroVoice implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		17
	};

	@Override
	public void handleChat(final int type, final L2PcInstance activeChar, final String target, final String text)
	{
		if (activeChar.isHero() || activeChar.isGM())
		{
			if (!activeChar.getFloodProtectors().getHeroVoice().tryPerformAction("hero voice"))
			{
				activeChar.sendMessage("Action failed. Heroes are only able to speak in the global channel once every 10 seconds.");
				return;
			}
			final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);

			final Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
			for (final L2PcInstance player : pls)
				if (player != null && !BlockList.isBlocked(player, activeChar))
					player.sendPacket(cs);
		}
	}

	/**
	 * Returns the chat types registered to this handler
	 * @see ct25.xtreme.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
