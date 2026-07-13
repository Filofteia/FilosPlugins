package filo.friendlist.tabs;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import filo.friendlist.tabs.data.FriendTab;
import filo.friendlist.tabs.data.SaveData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FLGroupManager
{
    private final ConfigManager configManager;
    private final FLOverhaulConfig config;
    private final Gson gson;

    public FLGroupManager(Gson gson, ConfigManager configManager, FLOverhaulConfig config)
    {
        this.gson = gson;
        this.configManager = configManager;
        this.config = config;

        isExpanded = config.ungroupedStartExpanded();
    }

    @Getter private List<String> groupNames;
    private Map<String, FriendTab> tabByName;
    private Map<String, String> playerToGroup;
    private Map<String, List<String>> groupToPlayers;

    private boolean configLoaded = false;
    private boolean isExpanded;	// Ungrouped

    public void setPlayerGroup(String playerName, String targetGroup)
    {
        if (!groupNames.contains(targetGroup))
            return;

        String sourceGroup = playerToGroup.put(playerName, targetGroup);
        if (sourceGroup != null)
        {
            List<String> sourceGroupPlayers = groupToPlayers.get(sourceGroup);
            if (sourceGroupPlayers != null)
                sourceGroupPlayers.remove(playerName);
        }

        groupToPlayers.computeIfAbsent(targetGroup, g -> new ArrayList<>()).add(playerName);

        saveConfig();
    }

    public void removePlayerFromGroup(String playerName)
    {
        String sourceGroup = playerToGroup.remove(playerName);

        if (sourceGroup != null) {
            List<String> sourceGroupPlayers = groupToPlayers.get(sourceGroup);
            if (sourceGroupPlayers != null)
                sourceGroupPlayers.remove(playerName);
        }
    }

    public void saveConfig()
    {
        if (!configLoaded)
            return;

        configManager.setRSProfileConfiguration("filo.friendtab", "savedata", generateSaveJson());
    }

    public String generateSaveJson()
    {
        SaveData saveData = new SaveData(groupNames, tabByName, playerToGroup);
        return gson.toJson(saveData);
    }

    public void resetConfig()
    {
        configManager.setRSProfileConfiguration("filo.friendtab", "savedata", "");
        configLoaded = false;
        loadConfig();
    }

    public void loadConfig()
    {
        if (configManager.getRSProfileKey() == null)
            return;

        if (configLoaded) // Prevent Duplicate loading
            return;

        configLoaded = true;

        String saveJson = configManager.getRSProfileConfiguration("filo.friendtab", "savedata");
        importSave(saveJson);
    }

    public void importSave(String saveJson)
    {
        SaveData saveData;
        try
        {
            saveData = gson.fromJson(saveJson, SaveData.class);
        }
        catch (JsonSyntaxException e)
        {
            return;
        }

        if (saveData == null)
        {
            tabByName = new LinkedHashMap<>();
            groupNames = new ArrayList<>();
            playerToGroup = new HashMap<>();
            groupToPlayers = new HashMap<>();
            return;
        }

        this.tabByName = saveData.getTabByName() != null
                ? new LinkedHashMap<>(saveData.getTabByName())
                : new LinkedHashMap<>();

        this.playerToGroup = saveData.getPlayerToGroup() != null
                ? new HashMap<>(saveData.getPlayerToGroup())
                : new HashMap<>();

        this.groupNames = saveData.getGroupNames() != null
                ? new ArrayList<>(saveData.getGroupNames())
                : new ArrayList<>();

        groupNames.removeIf(group -> !tabByName.containsKey(group));    // Just for safety

        groupToPlayers = new HashMap<>();
        for (String group : groupNames)
        {
            groupToPlayers.put(group, new ArrayList<>());
        }

        for (Map.Entry<String, String> entry : new HashMap<>(playerToGroup).entrySet()) // Could use iterator instead
        {
            String player = entry.getKey();
            String group = entry.getValue();

            if (!groupNames.contains(group))	// 'add to group', had that commented but have no memory of the issue
            {
                playerToGroup.remove(player);
                continue;
            }

            groupToPlayers.computeIfAbsent(group, g -> new ArrayList<>()).add(player);
        }
    }

    boolean isExpanded(String group)
    {
        if (isDefaultGroup(group))
            return isExpanded;

        FriendTab tab = getTab(group);
        if (tab != null)
            return tab.isExpanded();

        return false;
    }

    public Color getTextColour(String group)
    {
        FriendTab tab = getTab(group);
        if (tab != null)
            return new Color(tab.getCategoryColor());

        return config.defaultColor();
    }

    public Color getBackdropColour(String group)
    {
        FriendTab tab = getTab(group);
        if (tab != null)
            return new Color(tab.getBackdropColor());

        return config.defaultBackdrop();
    }

    public void toggleExpanded(String group)
    {
        if (isDefaultGroup(group))
        {
            isExpanded = !isExpanded;
            return;
        }

        FriendTab tab = getTab(group);
        if (tab != null)
            tab.toggleExpanded();
    }

    public void renameGroup(String targetGroup, String newGroup)
    {
        //TODO: This does work as tabByName, groupNames, and playerToGroup are synced. However I should re-write this if I add more.
        int groupIndex = groupNames.indexOf(targetGroup);
        if (groupIndex == -1)
            return;

        if (tabByName.containsKey(newGroup))    // Prevent renaming to an existing group
            return;

        if (!tabByName.containsKey(targetGroup)
                || !groupToPlayers.containsKey(targetGroup))
            return;

        groupNames.set(groupIndex, newGroup);

        FriendTab tab = tabByName.remove(targetGroup);
        tabByName.put(newGroup, tab);

        playerToGroup.replaceAll((player, group) ->
                group.equals(targetGroup) ? newGroup : group);

        List<String> playerList = groupToPlayers.remove(targetGroup);
        groupToPlayers.put(newGroup, playerList);

        saveConfig();
    }

    public void createGroup(String group)
    {
        if (group.isBlank())
            return;

        if (hasGroup(group))
            return;

        if (isDefaultGroup(group))
            return;

        FriendTab tab = createDefaultTab();

        groupNames.add(group);
        tabByName.put(group, tab);
        groupToPlayers.put(group, new ArrayList<>());

        saveConfig();
    }

    private FriendTab createDefaultTab()
    {
        return new FriendTab(config.defaultColor().getRGB(), config.defaultBackdrop().getRGB());
    }

    public void deleteGroup(String group)
    {
        groupNames.remove(group);
        tabByName.remove(group);
        playerToGroup.values().removeIf(group::equals);
        groupToPlayers.remove(group);

        saveConfig();
    }

    public void swapGroupOrder(int groupSource, int groupTarget)
    {
        Collections.swap(groupNames, groupSource, groupTarget);
        saveConfig();
    }

    public void migratePlayer(String sourcePlayer, String targetPlayer)
    {
        String groupName = playerToGroup.remove(sourcePlayer);
        removePlayerFromGroup(sourcePlayer);

        if (groupName != null)
            setPlayerGroup(targetPlayer, groupName);
    }

    public boolean hasGroups()
    {
        return !groupNames.isEmpty();
    }

    public String getPlayerGroup(String player)
    {
        return playerToGroup.get(player);
    }

    public List<String> getPlayers(String group)
    {
        return groupToPlayers.getOrDefault(group, Collections.emptyList());
    }

    public boolean hasGroup(String group)
    {
        return groupNames.stream()
                .anyMatch(g -> g.equalsIgnoreCase(group));
    }

    public FriendTab getTab(String group)
    {
        return tabByName.get(group);
    }

    public boolean playerHasGroup(String player)
    {
        return playerToGroup.containsKey(player);
    }

    public boolean isDefaultGroup(String group)
    {
        return group.equalsIgnoreCase("ungrouped");
    }
}