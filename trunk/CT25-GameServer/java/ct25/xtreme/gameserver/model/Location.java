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
package ct25.xtreme.gameserver.model;

import ct25.xtreme.gameserver.model.actor.L2Character;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:29:33 $
 */

public final class Location
{
	private int instanceId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;

	/**
	 * Constructor
	 */
	public Location()
	{
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	/**
	 * Constructor
	 * 
	 * @param obj
	 */
	public Location(L2Object obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
	}

	/**
	 * Constructor
	 * 
	 * @param obj
	 */
	public Location(L2Character obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_heading = obj.getHeading();
	}

	/**
	 * Constructor
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 */
	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}

	/**
	 * @return the instanceId
	 */
	public int getInstanceId()
	{
		return instanceId;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(int instanceId)
	{
		this.instanceId = instanceId;
	}

	/**
	 * @return the x
	 */
	public int getX()
	{
		return _x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x)
	{
		_x = x;
	}

	/**
	 * @return the y
	 */
	public int getY()
	{
		return _y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y)
	{
		_y = y;
	}

	/**
	 * @return the z
	 */
	public int getZ()
	{
		return _z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(int z)
	{
		_z = z;
	}

	/**
	 * @return the heading
	 */
	public int getHeading()
	{
		return _heading;
	}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}
}
