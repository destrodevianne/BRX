package ct25.xtreme.gameserver.skills;

import ct25.xtreme.gameserver.model.L2Skill;

/**
 * Enum to hold some important references to frequently used (hardcoded) skills in core
 * 
 * @author DrHouse
 */
public enum FrequentSkill
{
	RAID_CURSE(4215, 1),
	RAID_CURSE2(4515, 1),
	SEAL_OF_RULER(246, 1),
	BUILD_HEADQUARTERS(247, 1),
	WYVERN_BREATH(4289, 1),
	STRIDER_SIEGE_ASSAULT(325, 1),
	FAKE_PETRIFICATION(4616, 1),
	FIREWORK(5965, 1),
	LARGE_FIREWORK(2025, 1),
	BLESSING_OF_PROTECTION(5182, 1),
	ARENA_CP_RECOVERY(4380, 1),
	VOID_BURST(3630, 1),
	VOID_FLOW(3631, 1),
	THE_VICTOR_OF_WAR(5074, 1),
	THE_VANQUISHED_OF_WAR(5075, 1),
	SPECIAL_TREE_RECOVERY_BONUS(2139, 1);
	
	public final int _id;
	public final int _level;
	public L2Skill _skill = null;
	
	private FrequentSkill(int id, int level)
	{
		_id = id;
		_level = level;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
}