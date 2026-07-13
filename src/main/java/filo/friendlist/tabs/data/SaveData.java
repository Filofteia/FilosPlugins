package filo.friendlist.tabs.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class SaveData {
    private List<String> groupNames;
    private Map<String, FriendTab> tabByName;
    private Map<String, String> playerToGroup;

    public SaveData(List<String> groupNames, Map<String, FriendTab> tabByName, Map<String, String> playerToGroup) {
        this.groupNames = groupNames;
        this.tabByName = tabByName;
        this.playerToGroup = playerToGroup;
    }
}
