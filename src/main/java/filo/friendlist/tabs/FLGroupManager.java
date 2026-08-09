package filo.friendlist.tabs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import filo.friendlist.tabs.data.FriendTab;
import filo.friendlist.tabs.data.SaveData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<String, Set<String>> playerToGroups;
    private Map<String, List<String>> groupToPlayers;

    private boolean configLoaded = false;
    private boolean isExpanded;	// Ungrouped

    int CONFIG_VERSION = 0;
    private final String CONFIG_KEY = "filo.friendtab";
    private final String CONFIG_VALUE = "savedata";

    public void addPlayerToGroup(String playerName, String targetGroup)
    {
        if (!groupNames.contains(targetGroup))
            return;

        boolean addedPlayer = playerToGroups
                .computeIfAbsent(playerName, p -> new HashSet<>())
                .add(targetGroup);

        if (addedPlayer)
            groupToPlayers.computeIfAbsent(targetGroup, g -> new ArrayList<>())
                    .add(playerName);

        saveConfig();
    }

    public void removePlayerFromGroup(String playerName, String targetGroup)
    {
        if (!playerToGroups.containsKey(playerName) || !groupToPlayers.containsKey(targetGroup))
            return;

        Set<String> playerGroups = playerToGroups.get(playerName);
        playerGroups.remove(targetGroup);
        groupToPlayers.get(targetGroup).remove(playerName);

        cleanGroups();
        saveConfig();
    }

    private void cleanGroups()
    {
        playerToGroups.entrySet().removeIf(
                entry -> entry.getKey() == null
                        || entry.getValue() == null
                        || entry.getValue().isEmpty());
    }

    private Set<String> removePlayerFromGroups(String playerName)
    {
        Set<String> playerGroups = playerToGroups.remove(playerName);
        if (playerGroups == null)
            return Collections.emptySet();

        for (String group : playerGroups)
        {
            List<String> players = groupToPlayers.get(group);
            if (players != null) {
                players.remove(playerName);
            }
        }

        return playerGroups;
    }

    public void saveConfig()
    {
        if (!configLoaded)
            return;

        configManager.setRSProfileConfiguration(CONFIG_KEY, CONFIG_VALUE, generateSaveJson());
    }

    public String generateSaveJson()
    {
        SaveData saveData = new SaveData(CONFIG_VERSION, groupNames, tabByName, playerToGroups);
        return gson.toJson(saveData);
    }

    public void resetConfig()
    {
        configManager.setRSProfileConfiguration(CONFIG_KEY, CONFIG_VALUE, "");
        configLoaded = false;
        loadConfig();
    }

    public void loadConfig()
    {
        if (configManager.getRSProfileKey() == null)
            return;

        if (configLoaded) // Prevent Duplicate loading
            return;

        String saveJson = configManager.getRSProfileConfiguration(CONFIG_KEY, CONFIG_VALUE);
        configLoaded = importSave(saveJson);
    }


    //TODO: Multigroup Support
    public boolean importSave(String saveJson)
    {
        SaveData saveData;
        try
        {
            saveData = gson.fromJson(saveJson, SaveData.class);
        }
        catch (JsonSyntaxException e)
        {
            return false;
        }

        if (saveData == null)
        {
            CONFIG_VERSION = 1;
            tabByName = new LinkedHashMap<>();
            groupNames = new ArrayList<>();
            playerToGroups = new HashMap<>();
            groupToPlayers = new HashMap<>();
            return true;
        }

        if (saveData.getVersion() == 0)
        {
            migrateToV1(saveData, saveJson);
        }

        CONFIG_VERSION = saveData.getVersion();
        this.tabByName = saveData.getTabByName() != null
                ? new LinkedHashMap<>(saveData.getTabByName())
                : new LinkedHashMap<>();

        this.playerToGroups = saveData.getPlayerToGroups() != null
                ? new HashMap<>(saveData.getPlayerToGroups())
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

        cleanGroups();
        for (Map.Entry<String, Set<String>> entry : new HashMap<>(playerToGroups).entrySet()) // Could use iterator instead
        {
            String player = entry.getKey();
            Set<String> groups = entry.getValue();
            groups.removeIf(group -> group == null || !groupNames.contains(group));

            for (String group : groups)
            {
                groupToPlayers.computeIfAbsent(group, g -> new ArrayList<>()).add(player);
            }
        }

        return true;
    }

    private void migrateToV1(SaveData saveData, String saveJson)
    {
        JsonObject saveRoot = new JsonParser().parse(saveJson).getAsJsonObject();
        int ver = saveRoot.has("version") ? saveRoot.get("version").getAsInt() : 0;
        if (ver != 0)
            return;

        if (!saveRoot.has("playerToGroup"))
        {
            saveData.setVersion(1);
            CONFIG_VERSION = 1;
            return;
        }

        log.debug("Migrating save to V1");
        JsonElement element = saveRoot.get("playerToGroup");

        Type groupToken = new TypeToken<HashMap<String, String>>() {}.getType();
        HashMap<String, String> playerToGroup = gson.fromJson(element, groupToken);
        HashMap<String, Set<String>> playerToGroups = new HashMap<>();
        for (String player : playerToGroup.keySet())
        {
            playerToGroups.putIfAbsent(player, new HashSet<>());
            playerToGroups.get(player).add(playerToGroup.get(player));
        }

        saveData.setPlayerToGroups(playerToGroups);
        saveData.setVersion(1);
        log.debug("Migration complete");
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

    public void renameGroup(String sourceGroup, String targetGroup)
    {
        //TODO: This does work as tabByName, groupNames, and playerToGroup are synced. However I should re-write this if I add more.
        int groupIndex = groupNames.indexOf(sourceGroup);
        if (groupIndex == -1)
            return;
        if (tabByName.containsKey(targetGroup))
            return;

        if (!tabByName.containsKey(sourceGroup) || !groupToPlayers.containsKey(sourceGroup))
            return;

        groupNames.set(groupIndex, targetGroup);

        FriendTab tab = tabByName.remove(sourceGroup);
        tabByName.put(targetGroup, tab);

        replacePlayerSet(sourceGroup, targetGroup);

        List<String> playerList = groupToPlayers.remove(sourceGroup);
        groupToPlayers.put(targetGroup, playerList);

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
        playerToGroups.values().forEach(groupSet -> groupSet.remove(group));
        groupToPlayers.remove(group);
        cleanGroups();
        saveConfig();
    }

    public void swapGroupOrder(int groupSource, int groupTarget)
    {
        Collections.swap(groupNames, groupSource, groupTarget);
        saveConfig();
    }

    //TODO: Multigroup Support
    public void migratePlayer(String sourcePlayer, String targetPlayer)
    {
        Set<String> playerGroups = removePlayerFromGroups(sourcePlayer);
        playerGroups.forEach(group -> addPlayerToGroup(targetPlayer, group));
        saveConfig();
    }

    public boolean hasAnyGroups()
    {
        return !groupNames.isEmpty();
    }

    public Set<String> getPlayerGroups(String player) { return playerToGroups.get(player); }

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
        return !playerToGroups.getOrDefault(player, Collections.emptySet()).isEmpty();
    }

    public boolean isDefaultGroup(String group)
    {
        return group.equalsIgnoreCase("ungrouped");
    }

    private void replacePlayerSet(String sourceGroup, String targetGroup)
    {
        for (String player : playerToGroups.keySet())
        {
            Set<String> groupSet = playerToGroups.get(player);
            if (groupSet == null)
                continue;

            boolean removed = groupSet.remove(sourceGroup);
            if (removed)
                groupSet.add(targetGroup);
        }
    }
}