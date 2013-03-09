package ai.zones.fantasy_isle;

import ct25.xtreme.gameserver.instancemanager.QuestManager;

public class StartMCShow implements Runnable
{
	@Override
	public void run()
	{
		QuestManager.getInstance().getQuest("MC_Show").notifyEvent("Start", null, null);
	}
}
