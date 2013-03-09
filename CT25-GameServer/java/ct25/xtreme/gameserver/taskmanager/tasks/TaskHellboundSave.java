package ct25.xtreme.gameserver.taskmanager.tasks;


import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.taskmanager.Task;
import ct25.xtreme.gameserver.taskmanager.TaskManager;
import ct25.xtreme.gameserver.taskmanager.TaskManager.ExecutedTask;
import ct25.xtreme.gameserver.taskmanager.TaskTypes;

public final class TaskHellboundSave extends Task
{
	public static final String NAME = "hellbound_save";

	/**
	 * 
	 * @see ct25.xtreme.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}

	/**
	 * 
	 * @see ct25.xtreme.gameserver.taskmanager.Task#onTimeElapsed(ct25.xtreme.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		HellboundManager.getInstance().saveVars();
	}

	/**
	 * 
	 * @see ct25.xtreme.gameserver.taskmanager.Task#initializate()
	 */
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "500000", "1800000", "");
	}
}