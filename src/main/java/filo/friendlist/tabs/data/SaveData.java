package filo.friendlist.tabs.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
public class SaveData {
    private int version;
    private List<String> groupNames;
    private Map<String, FriendTab> tabByName;
    private Map<String, Set<String>> playerToGroups;

    public SaveData(int version, List<String> groupNames, Map<String, FriendTab> tabByName, Map<String, Set<String>> playerToGroups) {
        this.version = version;
        this.groupNames = groupNames;
        this.tabByName = tabByName;
        this.playerToGroups = playerToGroups;
    }
}
