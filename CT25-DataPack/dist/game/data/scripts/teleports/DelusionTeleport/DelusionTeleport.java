package teleports.DelusionTeleport;

import java.util.Map;

import ct25.xtreme.gameserver.instancemanager.TownManager;
import ct25.xtreme.gameserver.model.L2CharPosition;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.zone.type.L2TownZone;
import ct25.xtreme.util.Rnd;
import javolution.util.FastMap;

public class DelusionTeleport extends Quest
{
	private final static int REWARDER_ONE = 32658;
	// private final static int REWARDER_TWO = 32659;
	// private final static int REWARDER_THREE = 32660;
	// private final static int REWARDER_FOUR = 32661;
	// private final static int REWARDER_FIVE = 32663;
	private final static int REWARDER_SIX = 32662;
	private final static int START_NPC = 32484;
	
	private final static int[][] HALL_LOCATION =
	{
		{
			-114597,
			-152501,
			-6750
		},
		{
			-114589,
			-154162,
			-6750
		}
	};

	private final static Map<Integer, L2CharPosition> RETURN_LOCATION = new FastMap<>();
	
	static
	{
		RETURN_LOCATION.put(0, new L2CharPosition(43835, -47749, -792, 0)); // Undefined origin, return to Rune
		RETURN_LOCATION.put(7, new L2CharPosition(-14023, 123677, -3112, 0)); // Gludio
		RETURN_LOCATION.put(8, new L2CharPosition(18101, 145936, -3088, 0)); // Dion
		RETURN_LOCATION.put(10, new L2CharPosition(80905, 56361, -1552, 0)); // Oren
		RETURN_LOCATION.put(14, new L2CharPosition(42772, -48062, -792, 0)); // Rune
		RETURN_LOCATION.put(15, new L2CharPosition(108469, 221690, -3592, 0)); // Heine
		RETURN_LOCATION.put(17, new L2CharPosition(85991, -142234, -1336, 0)); // Schuttgart
	}
	
	public DelusionTeleport(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(START_NPC);
		addTalkId(START_NPC);
		
		for (int i = REWARDER_ONE; i <= REWARDER_SIX; i++)
			addTalkId(i);
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		final int npcId = npc.getId();
		
		if (npcId == START_NPC)
		{
			int townId = 0;
			final L2TownZone town = TownManager.getTown(npc.getX(), npc.getY(), npc.getZ());

			if (town != null)
				townId = town.getTownId();

			st.set("return_loc", Integer.toString(townId));
			final int rand = Rnd.get(2);
			player.teleToLocation(HALL_LOCATION[rand][0], HALL_LOCATION[rand][1], HALL_LOCATION[rand][2]);
			if (player.getPet() != null)
				player.getPet().teleToLocation(HALL_LOCATION[rand][0], HALL_LOCATION[rand][1], HALL_LOCATION[rand][2]);
		}
		
		else if (npcId >= REWARDER_ONE && npcId <= REWARDER_SIX)
		{
			int townId = 0;

			if (!st.get("return_loc").isEmpty())
				townId = Integer.parseInt(st.get("return_loc"));
			if (!RETURN_LOCATION.containsKey(townId))
				townId = 0;
			
			final L2CharPosition pos = RETURN_LOCATION.get(townId);
			player.teleToLocation(pos.x, pos.y, pos.z);
			
			if (player.getPet() != null)
				player.getPet().teleToLocation(pos.x, pos.y, pos.z);
			
			st.exitQuest(true);
		}
		
		return "";
	}
	
	public static void main(final String[] args)
	{
		new DelusionTeleport(-1, "DelusionTeleport", "teleports");
	}
}