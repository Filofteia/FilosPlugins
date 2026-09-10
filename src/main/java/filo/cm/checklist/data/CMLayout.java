package filo.cm.checklist.data;

import lombok.Getter;

@Getter
public enum CMLayout
{
	/**
	 * 0123
	 * 4567
	 */
	ENTRY_F3(6, 5, 3, false, "Entry"),
	TEKTON(5,  4, 3, false, "Tekton"),
	CRABS(4, 0, 3, false, "Crabs"),
	SCAV_F3(0,1,3,false,"Scavenger"),
	ICE_DEMON(1,2,3,true,"Ice Demon", InstanceTemplate.RAIDS_ICE_DEMON_F3),
	SHAMANS(2,3,3,false,"Shamans"),
	PREP_F3(3,7, 3, true, "Prep", InstanceTemplate.RAIDS_FARMING_F3),
	END_F3(7, 7, 3, true, "End", InstanceTemplate.RAIDS_END_F3),

	ENTRY_F2(7, 6, 2, false, "Start"),
	VANGUARDS(6,  5, 2, false, "Vanguards"),
	THIEVING(5, 4, 2, true, "Theiving", InstanceTemplate.RAIDS_THIEVING_F2),
	SCAV_F2(4,0,2,false,"Scavengers"),
	VESPULA(0,1,2,false,"Vespula"),
	PREP_F2(1,2,2,true,"Prep", InstanceTemplate.RAIDS_FARMING_F2),
	TIGHTROPE(2,3, 2, false, "Tightrope"),
	END_F2(3, 3, 2, true, "End", InstanceTemplate.RAIDS_END_F2),

	ENTRY_F1(3, 2, 1, false, "Start"),
	GUARDIANS(2,  1, 1, false, "Guardians"),
	VASA(1, 0, 1, false, "Vasa"),
	SCAV_F1(0,4,1,false,"Scavenger"),
	MYSTICS(4,5,1,false,"Mystics"),
	MUTTADILES(5,6,1,false,"Muttadiles"),
	PREP_F1(6,7, 1, true, "Prep", InstanceTemplate.RAIDS_FARMING_F1),
	END_F1(7, 7, 1, true, "End", InstanceTemplate.RAIDS_END_F1),

	OLM_F0(7, 7, 0, true, "Olm", InstanceTemplate.RAIDS_OLM_F0);

	final int roomPos;
	final int nextPos;
	final int floor;
	final boolean hasStorage;
	final String roomName;
	InstanceTemplate instanceTemplate;

	CMLayout(int roomPos, int nextPos, int floor, boolean hasStorage, String roomName)
	{
		this.roomPos = roomPos;
		this.nextPos = nextPos;
		this.floor = floor;
		this.hasStorage = hasStorage;
		this.roomName = roomName;
	}

	CMLayout(int roomPos, int nextPos, int floor, boolean hasStorage, String roomName, InstanceTemplate instanceTemplate)
	{
		this.roomPos = roomPos;
		this.nextPos = nextPos;
		this.floor = floor;
		this.hasStorage = hasStorage;
		this.roomName = roomName;
		this.instanceTemplate = instanceTemplate;
	}

	public static CMLayout[] getByFloor(int floor)
	{
		CMLayout[] floorRooms = new CMLayout[8];
		for (CMLayout room : values())
		{
			if (room.floor != floor)
				continue;

			floorRooms[room.ordinal() % 8] = room;
		}
		return floorRooms;
	}
}
