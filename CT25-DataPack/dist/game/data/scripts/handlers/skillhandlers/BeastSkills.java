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
package handlers.skillhandlers;

import ct25.xtreme.gameserver.handler.ISkillHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2TamedBeastInstance;
import ct25.xtreme.gameserver.templates.skills.L2SkillType;

/**
 * @author _drunk_
 */
public class BeastSkills implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(BeastSkills.class.getName());
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BEAST_FEED,
		L2SkillType.BEAST_RELEASE,
		L2SkillType.BEAST_RELEASE_ALL,
		L2SkillType.BEAST_SKILL,
		L2SkillType.BEAST_ACCOMPANY
	};

	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		final L2SkillType type = skill.getSkillType();
		final L2PcInstance player = activeChar.getActingPlayer();
		final L2Object target = player.getTarget();

		switch (type)
		{
			case BEAST_FEED:
				final L2Object[] targetList = skill.getTargetList(activeChar);

				if (targetList == null)
					return;
					
				// This is just a dummy skill handler for the golden food and crystal food skills,
				// since the AI responce onSkillUse handles the rest.
				break;
			case BEAST_RELEASE:
				if (target != null && target instanceof L2TamedBeastInstance)
					((L2TamedBeastInstance) target).deleteMe();
				break;
			case BEAST_RELEASE_ALL:
				if (player.getTrainedBeasts() != null)
					for (final L2TamedBeastInstance beast : player.getTrainedBeasts())
						beast.deleteMe();
				break;
			case BEAST_ACCOMPANY:
				// Unknown effect now
				break;
			case BEAST_SKILL:
				if (target != null && target instanceof L2TamedBeastInstance)
					((L2TamedBeastInstance) target).castBeastSkills();
				break;
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
