/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ct25.xtreme.gameserver.enums;

/**
 * This class defines all category types.
 * @author xban1x
 */
public enum CategoryType
{
	FIGHTER_GROUP,
	MAGE_GROUP,
	WIZARD_GROUP,
	CLERIC_GROUP,
	ATTACKER_GROUP,
	TANKER_GROUP,
	FIRST_CLASS_GROUP,
	SECOND_CLASS_GROUP,
	THIRD_CLASS_GROUP,
	FOURTH_CLASS_GROUP,
	BOUNTY_HUNTER_GROUP,
	WARSMITH_GROUP,
	SUMMON_NPC_GROUP,
	KNIGHT_GROUP,
	WHITE_MAGIC_GROUP,
	HEAL_GROUP,
	ASSIST_MAGIC_GROUP,
	WARRIOR_GROUP,
	HUMAN_2ND_GROUP,
	ELF_2ND_GROUP,
	DELF_2ND_GROUP,
	ORC_2ND_GROUP,
	DWARF_2ND_GROUP;
	
	/**
	 * Finds category by it's name
	 * @param categoryName
	 * @return A {@code CategoryType} if category was found, {@code null} if category was not found
	 */
	public static final CategoryType findByName(String categoryName)
	{
		for (CategoryType type : values())
		{
			if (type.name().equalsIgnoreCase(categoryName))
			{
				return type;
			}
		}
		return null;
	}
}
