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
import ct25.xtreme.gameserver.model.interfaces.ILocational;
import ct25.xtreme.gameserver.model.interfaces.IPositionable;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:29:33 $
 */

public final class Location implements IPositionable
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
	
	public Location(int x, int y, int z, int heading, int instanceId)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
		this.instanceId = instanceId;
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

	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.model.interfaces.ILocational#getLocation()
	 */
	@Override
	public IPositionable getLocation()
	{
		return this;
	}

	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.model.interfaces.IPositionable#setXYZ(int, int, int)
	 */
	@Override
	public void setXYZ(int x, int y, int z)
	{
		setX(x);
		setY(y);
		setZ(z);
	}

	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.model.interfaces.IPositionable#setXYZ(ct25.xtreme.gameserver.model.interfaces.ILocational)
	 */
	@Override
	public void setXYZ(ILocational loc)
	{
		setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}

	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.model.interfaces.IPositionable#setLocation(ct25.xtreme.gameserver.model.Location)
	 */
	@Override
	public void setLocation(Location loc)
	{
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
		_heading = loc.getHeading();
		instanceId = loc.getInstanceId();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if ((obj != null) && (obj instanceof Location))
		{
			final Location loc = (Location) obj;
			return (getX() == loc.getX()) && (getY() == loc.getY()) && (getZ() == loc.getZ()) && (getHeading() == loc.getHeading()) && (getInstanceId() == loc.getInstanceId());
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + "] X: " + getX() + " Y: " + getY() + " Z: " + getZ() + " Heading: " + _heading + " InstanceId: " + instanceId;
	}
}