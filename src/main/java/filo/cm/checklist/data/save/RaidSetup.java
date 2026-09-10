package filo.cm.checklist.data.save;

import filo.cm.checklist.data.InstanceTemplate;
import lombok.Data;
import java.util.LinkedHashMap;
import java.util.UUID;

@Data
public class RaidSetup {
    private int ver = 0;
    private UUID id = UUID.randomUUID();
    private String name;
    private LinkedHashMap<InstanceTemplate, RoomSetup> rooms =
            new LinkedHashMap<>();

    public RaidSetup(String name) {
        this.name = name;
    }

    public RoomSetup getRoom(InstanceTemplate template)
    {
        return rooms.computeIfAbsent(template, n -> new RoomSetup());
    }
}
