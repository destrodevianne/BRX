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
package ct25.xtreme.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javolution.util.FastMap;
import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author L0ngh0rn
 */
public class ModsBufferSkillTable
{
	private static Logger						_log		= Logger.getLogger(ModsBufferSkillTable.class.getName());
	private static String						SQL_GROUP	= "SELECT mbs.skill_group, count(*) amount FROM mods_buffer_skill AS mbs GROUP BY mbs.skill_group ORDER BY mbs.skill_group ASC";
	private static String						SQL_SKILL	= "SELECT DISTINCT mbs.skill_id, mbs.skill_level, mbs.skill_fee_id, mbs.skill_fee_amount, mbs.skill_comp, mbs.skill_desc, mbs.skill_icon, mbs.skill_group, mbs.skill_donator "
																	+ "FROM mods_buffer_skill AS mbs WHERE mbs.skill_group = ? ORDER BY mbs.skill_group ASC";

	private FastMap<String, Integer>			_group;
	private FastMap<String, List<BufferSkill>>	_skillGroup;
	private FastMap<String, BufferSkill>		_skill;

	public static ModsBufferSkillTable getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ModsBufferSkillTable	_instance	= new ModsBufferSkillTable();
	}

	public ModsBufferSkillTable()
	{
		_log.info("ModsBufferSkill System: Initializing...");
		loadGroup();
		loadSkillGroup();
	}

	public void reloadBufferSkillTable()
	{
		_log.info("ModsBufferSkill System: Reloading...");
		loadGroup();
		loadSkillGroup();
	}

	public void loadGroup()
	{
		_group = new FastMap<String, Integer>();
		int totalSkill = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_GROUP);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				_group.put(rs.getString("skill_group"), rs.getInt("amount"));
				totalSkill += rs.getInt("amount");
			}
			_log.info("ModsBufferSkill: Loaded " + _group.size() + " group(s) with a total of " + totalSkill
					+ " skills.");
		}
		catch (Exception e)
		{
			_log.warning("ModsBufferSkill: Could not load Group: " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void loadSkillGroup()
	{
		_skillGroup = new FastMap<String, List<BufferSkill>>();
		_skill = new FastMap<String, BufferSkill>();
		List<BufferSkill> skills;

		for (String group : _group.keySet())
		{
			Connection con = null;
			try
			{
				skills = new ArrayList<BufferSkill>(_group.get(group));
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SQL_SKILL);
				ps.setString(1, group);
				ResultSet rs = ps.executeQuery();
				while (rs.next())
				{
					int skillId = rs.getInt("skill_id");
					int skillLevel = rs.getInt("skill_level");
					String skillName = SkillTable.getInstance().getInfo(skillId, skillLevel).getName();
					BufferSkill bufferSkill = new BufferSkill(skillId, skillName, skillLevel,
							rs.getInt("skill_fee_id"), rs.getLong("skill_fee_amount"), rs.getString("skill_comp"),
							rs.getString("skill_desc"), rs.getString("skill_icon"), rs.getString("skill_group"),
							rs.getInt("skill_donator"));

					skills.add(bufferSkill);
					_skill.put(skillId + "-" + skillLevel, bufferSkill);
				}
				_skillGroup.put(group, skills);
			}
			catch (Exception e)
			{
				_log.warning("ModsBufferSkill: Could not load Skill Group: " + e.getMessage());
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public BufferSkill getSkillInfo(String skill)
	{
		return _skill.get(skill);
	}

	public Set<String> getGroups(L2PcInstance activeChar)
	{
		Set<String> gp = new HashSet<String>();
		for (String gpName : _group.keySet())
		{
			for (BufferSkill bs : getSkillGroup(gpName, activeChar))
			{
				if (!activeChar.isGM() && bs.getPlayer() == 2)
					continue;
				if (!activeChar.isGM() && bs.getPlayer() == 1)
					continue;
				if (!gp.contains(gpName))
					gp.add(gpName);
			}
		}
		return gp;
	}

	public Integer getCountSkill(String group, L2PcInstance activeChar)
	{
		Integer i = 0;
		for (BufferSkill bs : getSkillGroup(group, activeChar))
		{
			if (!activeChar.isGM() && bs.getPlayer() == 2)
				continue;
			if (!activeChar.isGM() && bs.getPlayer() == 1)
				continue;
			i++;
		}
		return i;
	}

	public List<BufferSkill> getSkillGroup(String group, L2PcInstance activeChar)
	{
		List<BufferSkill> list = new ArrayList<BufferSkill>();
		for (BufferSkill bs : _skillGroup.get(group))
		{
			if (!activeChar.isGM() && bs.getPlayer() == 2)
				continue;
			if (!activeChar.isGM() && bs.getPlayer() == 1)
				continue;
			list.add(bs);
		}
		return list;
	}

	public class BufferSkill
	{
		private int		id;
		private String	name;
		private int		level;
		private int		fee_id		= 0;
		private Long	fee_amount	= 0L;
		private String	comp;
		private String	desc;
		private String	icon;
		private String	group;
		private int		player		= 0;	// 0 = All 

		public BufferSkill(int id, String name, int level, int fee_id, Long fee_amount, String comp, String desc,
				String icon, String group, int player)
		{
			this.id = id;
			this.name = name;
			this.level = level;
			this.fee_id = fee_id;
			this.fee_amount = fee_amount;
			this.comp = comp;
			this.desc = desc;
			this.icon = icon;
			this.group = group;
			this.player 			= 0;
		}

		public int getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}

		public int getLevel()
		{
			return level;
		}

		public int getFee_id()
		{
			return fee_id;
		}

		public Long getFee_amount()
		{
			return fee_amount;
		}

		public String getComp()
		{
			return comp;
		}

		public String getDesc()
		{
			return desc;
		}

		public String getIcon()
		{
			return icon;
		}

		public String getGroup()
		{
			return group;
		}

		public int getPlayer()
		{
			return player;
		}
	}
}
