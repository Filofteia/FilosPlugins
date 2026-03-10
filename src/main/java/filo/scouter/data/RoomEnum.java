package filo.scouter.data;

import net.runelite.client.util.Text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

    /**
     * Could use this for blocked rooms, but I just want it to be careful with required rooms, as one letter would make scouting impossible.
     * @param userConfig string of room names separated by comma
     * @return a Set<String> of valid room names
     */
    public static Set<String> validateRequiredRooms(String userConfig)
    {
        Set<String> requiredSet = new HashSet<>();
        List<String> requiredRooms = Text.fromCSV(userConfig.toLowerCase());

        for (String roomName : requiredRooms)
        {
            if (isValidRoom(roomName))
            {
                requiredSet.add(roomName);
            }
        }

        return requiredSet;
    }

    public static boolean isValidRoom(String roomName)
    {
        for (RoomEnum roomEnum : values())
        {
            if (roomEnum.roomNames.contains(roomName))
            {
                return true;
            }
        }

        return false;
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
