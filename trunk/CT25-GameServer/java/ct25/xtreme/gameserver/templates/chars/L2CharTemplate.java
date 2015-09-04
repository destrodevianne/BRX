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
package ct25.xtreme.gameserver.templates.chars;

import ct25.xtreme.gameserver.templates.StatsSet;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/02 15:57:51 $
 */
public class L2CharTemplate
{
	// BaseStats
	public final int baseSTR;
	public final int baseCON;
	public final int baseDEX;
	public final int baseINT;
	public final int baseWIT;
	public final int baseMEN;
	public float baseHpMax;
	public final float baseCpMax;
	public final float baseMpMax;
	
	/** HP Regen base */
	public final float baseHpReg;
	
	/** MP Regen base */
	public final float baseMpReg;
	
	public final int basePAtk;
	public final int baseMAtk;
	public final int basePDef;
	public final int baseMDef;
	public final int basePAtkSpd;
	public final int baseMAtkSpd;
	public final float baseMReuseRate;
	public final int baseShldDef;
	public final int baseAtkRange;
	public final int baseShldRate;
	public final int baseCritRate;
	public final int baseMCritRate;
	public final int baseWalkSpd;
	public final int baseRunSpd;
	
	// SpecialStats
	public final int baseBreath;
	public final int baseAggression;
	public final int baseBleed;
	public final int basePoison;
	public final int baseStun;
	public final int baseRoot;
	public final int baseMovement;
	public final int baseConfusion;
	public final int baseSleep;
	public final double baseAggressionVuln;
	public final double baseBleedVuln;
	public final double basePoisonVuln;
	public final double baseStunVuln;
	public final double baseRootVuln;
	public final double baseMovementVuln;
	public final double baseSleepVuln;
	public int baseFire;
	public int baseWind;
	public int baseWater;
	public int baseEarth;
	public int baseHoly;
	public int baseDark;
	public double baseFireRes;
	public double baseWindRes;
	public double baseWaterRes;
	public double baseEarthRes;
	public double baseHolyRes;
	public double baseDarkRes;
	public final double baseCritVuln;
		
	//C4 Stats
	public final int baseMpConsumeRate;
	public final int baseHpConsumeRate;
	
	/**
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> :
	 * For client info use {@link fCollisionRadius}
	 * </B></FONT><BR><BR>
	 */
	public final int collisionRadius;
	
	/**
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> :
	 * For client info use {@link fCollisionHeight}
	 * </B></FONT><BR><BR>
	 */
	public final int collisionHeight;
	
	public final double fCollisionRadius;
	public final double fCollisionHeight;
	
	public L2CharTemplate(StatsSet set)
	{
		// Base stats
		baseSTR = set.getInt("baseSTR", 0);
		baseCON = set.getInt("baseCON", 0);
		baseDEX = set.getInt("baseDEX", 0);
		baseINT = set.getInt("baseINT", 0);
		baseWIT = set.getInt("baseWIT", 0);
		baseMEN = set.getInt("baseMEN", 0);
		baseHpMax = set.getFloat("baseHpMax", 0);
		baseCpMax = set.getFloat("baseCpMax", 0);
		baseMpMax = set.getFloat("baseMpMax", 0);
		baseHpReg = set.getFloat("baseHpReg", 0);
		baseMpReg = set.getFloat("baseMpReg", 0);
		basePAtk = set.getInt("basePAtk", 0);
		baseMAtk = set.getInt("baseMAtk", 0);
		basePDef = set.getInt("basePDef", 0);
		baseMDef = set.getInt("baseMDef", 0);
		basePAtkSpd = set.getInt("basePAtkSpd", 0);
		baseMAtkSpd = set.getInt("baseMAtkSpd", 0);
		baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		baseShldDef = set.getInt("baseShldDef", 0);
		baseAtkRange = set.getInt("baseAtkRange", 0);
		baseShldRate = set.getInt("baseShldRate", 0);
		baseCritRate = set.getInt("baseCritRate", 0);
		baseMCritRate = set.getInt("baseMCritRate", 80); // CT2: The magic critical rate has been increased to 10 times.
		baseWalkSpd = set.getInt("baseWalkSpd", 0);
		baseRunSpd = set.getInt("baseRunSpd", 0);
		
		// SpecialStats
		baseBreath = set.getInt("baseBreath", 100);
		baseAggression = set.getInt("baseAggression", 0);
		baseBleed = set.getInt("baseBleed", 0);
		basePoison = set.getInt("basePoison", 0);
		baseStun = set.getInt("baseStun", 0);
		baseRoot = set.getInt("baseRoot", 0);
		baseMovement = set.getInt("baseMovement", 0);
		baseConfusion = set.getInt("baseConfusion", 0);
		baseSleep = set.getInt("baseSleep", 0);
		baseFire = set.getInt("baseFire", 0);
		baseWind = set.getInt("baseWind", 0);
		baseWater = set.getInt("baseWater", 0);
		baseEarth = set.getInt("baseEarth", 0);
		baseHoly = set.getInt("baseHoly", 0);
		baseDark = set.getInt("baseDark", 0);
		baseAggressionVuln = set.getInt("baseAggressionVuln", 0);
		baseBleedVuln = set.getInt("baseBleedVuln", 0);
		basePoisonVuln = set.getInt("basePoisonVuln", 0);
		baseStunVuln = set.getInt("baseStunVuln", 0);
		baseRootVuln = set.getInt("baseRootVuln", 0);
		baseMovementVuln = set.getInt("baseMovementVuln", 0);
		baseSleepVuln = set.getInt("baseSleepVuln", 0);
		baseFireRes = set.getInt("baseFireRes", 0);
		baseWindRes = set.getInt("baseWindRes", 0);
		baseWaterRes = set.getInt("baseWaterRes", 0);
		baseEarthRes = set.getInt("baseEarthRes", 0);
		baseHolyRes = set.getInt("baseHolyRes", 0);
		baseDarkRes = set.getInt("baseDarkRes", 0);
		baseCritVuln = set.getInt("baseCritVuln", 1);
		
		//C4 Stats
		baseMpConsumeRate = set.getInt("baseMpConsumeRate", 0);
		baseHpConsumeRate = set.getInt("baseHpConsumeRate", 0);
		
		// Geometry
		fCollisionHeight = set.getDouble("collision_height", 0);
		fCollisionRadius = set.getDouble("collision_radius", 0);
		collisionRadius = (int) fCollisionRadius;
		collisionHeight = (int) fCollisionHeight;
	}
}
