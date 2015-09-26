/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package ct25.xtreme.gameserver.datatables;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.logging.Logger;

import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.skills.FrequentSkill;
import ct25.xtreme.gameserver.skills.SkillsEngine;

/**
 * 
 */
public class SkillTable
{
	private static Logger _log = Logger.getLogger(SkillTable.class.getName());
	
	private final TIntObjectHashMap<L2Skill> _skills;
	private final TIntIntHashMap _skillMaxLevel;
	private final TIntArrayList _enchantable;
	//GM Skills
	private static final L2Skill[] _gmSkills = new L2Skill[34];
	private static final int[] _gmSkillsId = { 7029, 7041, 7042, 7043, 7044, 7045, 7046, 7047, 7048, 7049, 7050, 7051, 7052, 7053, 7054, 7055, 7056, 7057, 7058, 7059, 7060, 7061, 7062, 7063, 7064, 7088, 7089, 7090, 7091, 7092, 7093, 7094 ,7095, 7096 };
	//Hero Skills
	private static final L2Skill[] _heroSkills = new L2Skill[5];
	private static final int[] _heroSkillsId = {395, 396, 1374, 1375, 1376};
	//Noble Skills
	private static final L2Skill[] _nobleSkills = new L2Skill[8];
	
	public static SkillTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private SkillTable()
	{
		_skills = new TIntObjectHashMap<L2Skill>();
		_skillMaxLevel = new TIntIntHashMap();
		_enchantable = new TIntArrayList();
		load();
		GMSkills();
		HeroSkills();
		NobleSkills();
	}
	
	public void reload()
	{
		load();
		//Reload Skill Tree as well.
		SkillTreesData.getInstance().load();
	}
	
	private void load()
	{
		_skills.clear();
		SkillsEngine.getInstance().loadAllSkills(_skills);
		
		_skillMaxLevel.clear();
		for (final L2Skill skill : _skills.values(new L2Skill[_skills.size()]))
		{
			final int skillId = skill.getId();
			final int skillLvl = skill.getLevel();
			if (skillLvl > 99)
			{
				if (!_enchantable.contains(skillId))
					_enchantable.add(skillId);
				continue;
			}
			
			// only non-enchanted skills
			final int maxLvl = _skillMaxLevel.get(skillId);
			if (skillLvl > maxLvl)
				_skillMaxLevel.put(skillId, skillLvl);
		}
		
		// Sorting for binarySearch
		_enchantable.sort();
		
		// Reloading as well FrequentSkill enumeration values
		for (FrequentSkill sk : FrequentSkill.values())
			sk._skill = getInfo(sk._id, sk._level);
	}
	
	/**
	 * Provides the skill hash
	 * 
	 * @param skill
	 *            The L2Skill to be hashed
	 * @return getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(L2Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	/**
	 * Centralized method for easier change of the hashing sys
	 * 
	 * @param skillId
	 *            The Skill Id
	 * @param skillLevel
	 *            The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return skillId * 1021 + skillLevel;
	}
	
	public final L2Skill getInfo(final int skillId, final int level)
	{
		final L2Skill result = _skills.get(getSkillHashCode(skillId, level));
		if (result != null)
			return result;
		
		// skill/level not found, fix for transformation scripts
		final int maxLvl = _skillMaxLevel.get(skillId);
		// requested level too high
		if (maxLvl > 0 && level > maxLvl)
			return _skills.get(getSkillHashCode(skillId, maxLvl));
		
		_log.warning("No skill info found for skill id " + skillId + " and skill level " + level + ".");
		return null;
	}
	
	public final int getMaxLevel(final int skillId)
	{
		return _skillMaxLevel.get(skillId);
	}
	
	public final boolean isEnchantable(final int skillId)
	{
		return _enchantable.binarySearch(skillId) >= 0;
	}
	
	/**
	 * Returns an array with siege skills. If addNoble == true, will add also Advanced headquarters.
	 */
	public L2Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		L2Skill[] temp = new L2Skill[2 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
		int i = 0;
		temp[i++] = _skills.get(SkillTable.getSkillHashCode(246, 1));
		temp[i++] = _skills.get(SkillTable.getSkillHashCode(247, 1));
		
		if (addNoble)
			temp[i++] = _skills.get(SkillTable.getSkillHashCode(326, 1));
		if (hasCastle)
		{
			temp[i++] = _skills.get(SkillTable.getSkillHashCode(844, 1));
			temp[i++] = _skills.get(SkillTable.getSkillHashCode(845, 1));
		}
		return temp;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SkillTable _instance = new SkillTable();
	}
	
	//GM Skills
	public static L2Skill[] getGMSkills()
	{
		return _gmSkills;
	}
	
	public static boolean isGMSkill(int skillid)
	{
		for (int id : _gmSkillsId)
		{
			if (id == skillid)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void addSkills(L2PcInstance gmchar)
	{
		for (L2Skill s : getGMSkills())
		{
			gmchar.addSkill(s, false); // Don't Save GM skills to database
		}
	}
	//Hero Skills
	public static L2Skill[] getHeroSkills()
	{
		return _heroSkills;
	}
	
	public static boolean isHeroSkill(int skillid)
	{
		for (int id : _heroSkillsId)
		{
			if (id == skillid)
				return true;
		}
		
		return false;
	}
	//Noble Skills
	public L2Skill[] getNobleSkills()
	{
		return _nobleSkills;
	}
	
	void NobleSkills()
	{
		_nobleSkills[0] = getInfo(1323, 1);
		_nobleSkills[1] = getInfo(325, 1);
		_nobleSkills[2] = getInfo(326, 1);
		_nobleSkills[3] = getInfo(327, 1);
		_nobleSkills[4] = getInfo(1324, 1);
		_nobleSkills[5] = getInfo(1325, 1);
		_nobleSkills[6] = getInfo(1326, 1);
		_nobleSkills[7] = getInfo(1327, 1);
	}
	
	void GMSkills()
	{
		for (int i = 0; i < _gmSkillsId.length; i++)
			_gmSkills[i] = getInfo(_gmSkillsId[i], 1);
	}
	
	void HeroSkills()
	{
		for (int i=0; i<_heroSkillsId.length; i++)
			_heroSkills[i] = getInfo(_heroSkillsId[i], 1);
	}
}
