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
package quests.Q00635_IntoTheDimensionalRift;

import ct25.xtreme.gameserver.model.quest.Quest;

/**
 * Into the Dimensional Rift (635)<br>
 * NOTE: Dummy Quest shown in players' questlist when inside the rift
 * @author malyelfik
 */
public class Q00635_IntoTheDimensionalRift extends Quest
{
	private Q00635_IntoTheDimensionalRift(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
	}

	public static void main(final String[] args)
	{
		new Q00635_IntoTheDimensionalRift(635, Q00635_IntoTheDimensionalRift.class.getSimpleName(), "Into the Dimensional Rift");
	}
}