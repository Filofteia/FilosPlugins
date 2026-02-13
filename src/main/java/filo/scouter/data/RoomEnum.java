package filo.scouter.data;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RoomEnum {
    COMBAT("muttadiles", "tekton", "vasa", "guardians", "shamans", "mystics", "vespula", "vanguards"),
    PUZZLE("crabs", "thieving", "tightrope", "ice demon");

    private final Set<String> roomNames;

    RoomEnum(String... roomNames) {
        this.roomNames = Arrays.stream(roomNames)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    /**
     * Returns roomtype based on String input
     * @param roomName name of room to check
     * @return RoomEnum.Combat | RoomEnum.Puzzle | Null if input is bad
     */
    public static RoomEnum fromRoomName(String roomName)
    {
        String roomNameLower = roomName.toLowerCase();
        for (RoomEnum roomEnum : values())
        {
            if (roomEnum.roomNames.contains(roomNameLower))
            {
                return roomEnum;
            }
        }

        return null;
    }

    public static boolean hasPuzzleRoom(String roomNames)
    {
        for (String roomName : roomNames.split(","))
        {
            if (fromRoomName(roomName) == RoomEnum.PUZZLE)
            {
                return true;
            }
        }

        return false;
    }
}
