package filo.cm.checklist.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemBoxType;
import filo.cm.checklist.data.save.RaidSetup;
import filo.cm.checklist.data.save.RoomSetup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import javax.swing.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class SaveManager {
    private final String GROUP_KEY = "cmstorage";
    private final String GROUP_KEYLIST = "keylist";

    private Client client;
    private CMChecklistPlugin plugin;
    private ConfigManager configManager;
    private ClientThread clientThread;
    private Gson gson;

    private List<UUID> uuidKeyList = new ArrayList<>();
    @Getter private List<RaidSetup> raidSetupList = new ArrayList<>();
    @Setter @Getter private RaidSetup activeSetup;

    public SaveManager(ConfigManager configManager, CMChecklistPlugin plugin, Client client, ClientThread clientThread) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.client = client;
        this.clientThread = clientThread;
        this.gson = plugin.getGson();

        load();
    }

    public void load()
    {
        uuidKeyList = new ArrayList<>();
        raidSetupList = new ArrayList<>();

        String keyList = configManager.getConfiguration(GROUP_KEY, GROUP_KEYLIST);
        if (keyList == null || keyList.isEmpty())
        {
            return;
        }

        Type uuidListType = new TypeToken<List<UUID>>() {}.getType();
        List<UUID> savedUuidList = gson.fromJson(keyList, uuidListType);
        if (savedUuidList == null || savedUuidList.isEmpty())
            return;

        for (UUID uuid : savedUuidList)
        {
            if (uuid == null)
                continue;

            String uuidPreset = configManager.getConfiguration(GROUP_KEY, uuid.toString());
            if (uuidPreset == null || uuidPreset.isEmpty())
                continue;

            try
            {
                RaidSetup setup = gson.fromJson(uuidPreset, RaidSetup.class);
                if (setup == null)
                    continue;

                uuidKeyList.add(uuid);
                raidSetupList.add(setup);
            }
            catch (JsonSyntaxException ex)
            {
                log.warn("Error while loading {}: {}", uuid, ex.getMessage());
            }
        }
    }

    // call on group make / delete
    public void saveIndexList()
    {
        String uuidKeyListJson = gson.toJson(uuidKeyList);
        configManager.setConfiguration(GROUP_KEY, GROUP_KEYLIST, uuidKeyListJson);
    }

    public void saveAll()
    {
        saveIndexList();
        saveByUUIDs(uuidKeyList);
    }

    public void saveByUUIDs(List<UUID> uuidList)
    {
        uuidList.forEach(this::saveByUUID);
    }

    public void saveByUUID(RaidSetup setup)
    {
        saveByUUID(setup.getId());
    }

    public void saveByUUID(UUID uuid)
    {
        RaidSetup uuidSetup = getByUUID(uuid);
        if (uuidSetup == null)
            return;

        String uuidSetupJson = gson.toJson(uuidSetup);
        configManager.setConfiguration(GROUP_KEY, uuid.toString(), uuidSetupJson);
    }

    public boolean deleteByUUID(UUID uuid)
    {
        boolean removed = uuidKeyList.remove(uuid);
        if (removed) {
            configManager.unsetConfiguration(GROUP_KEY, uuid.toString());
            raidSetupList.removeIf(setup -> setup.getId().equals(uuid));
            saveIndexList();
        }

        return removed;
    }

    public RaidSetup getByUUID(UUID uuid)
    {
        return raidSetupList.stream()
                .filter(setup -> setup.getId().equals(uuid))
                .findFirst().orElse(null);
    }

    public RaidSetup getByName(String name)
    {
        return raidSetupList.stream()
                .filter(setup -> setup.getName().equals(name))
                .findFirst().orElse(null);
    }

    public RaidSetup getSetupById(UUID uuid)
    {
        return getByUUID(uuid);
    }

    public boolean importSetup(RaidSetup setup)
    {
        int replacementIndex = getSetupIndex(setup);
        if (replacementIndex != -1)
        {
            raidSetupList.set(replacementIndex, setup);
            saveByUUID(setup);
            return true;
        }

        uuidKeyList.add(setup.getId());
        raidSetupList.add(setup);
        saveIndexList();
        saveByUUID(setup);
        return true;
    }


    public RaidSetup createSetup(String name)
    {
        if (hasSetup(name))
            return null;

        RaidSetup setup = new RaidSetup(name);
        if (setup != null)
        {
            boolean addedRaid = raidSetupList.add(setup);
            boolean addedUUID = uuidKeyList.add(setup.getId());

            saveIndexList();
            saveByUUID(setup.getId());

            return setup;
        }

        return null;
    }

    public boolean renameSetup(UUID id, String newName)
    {
        RaidSetup setup = getSetupById(id);

        if (setup == null)
            return false;

        if (hasSetup(newName))
            return false;

        setup.setName(newName);
        saveByUUID(id);
        return true;
    }

    public void updateItem(int idx, ItemBoxType type, int itemId, InstanceTemplate template)
    {
        RoomSetup room = getRoom(template);
        if (room == null)
            return;

        room.updateItem(idx, itemId, type);
        saveByUUID(activeSetup);
    }

    public void updateItem(int idx, ItemBoxType type, int itemId, InstanceTemplate template, Consumer<Boolean> status)
    {
        RoomSetup room = getRoom(template);
        if (room != null) {
            room.updateItem(idx, itemId, type);
            saveByUUID(activeSetup);
            status.accept(true);
        }
        else
        {
            status.accept(false);
        }
    }

    public void saveAll(InstanceTemplate template, Consumer<Boolean> callback)
    {
        clientThread.invokeLater(() -> {
            boolean savedInv = saveInventory(template);
            boolean savedEquip = saveEquipment(template);
            if (callback != null)
            {
                if (savedInv && savedEquip) {
                    SwingUtilities.invokeLater(() -> callback.accept(true));
                    return;
                }

                SwingUtilities.invokeLater(() -> callback.accept(false));
            }
        });
    }


    public void saveEquipment(InstanceTemplate template, Consumer<Boolean> callback)
    {
        clientThread.invokeLater(() -> {
            boolean saved = saveEquipment(template);
            if (callback != null)
                SwingUtilities.invokeLater(() -> callback.accept(saved));
        });
    }

    public void saveInventory(InstanceTemplate template, Consumer<Boolean> callback)
    {
        clientThread.invokeLater(() -> {
            boolean saved = saveInventory(template);
            if (callback != null)
                SwingUtilities.invokeLater(() -> callback.accept(saved));
        });
    }

    // Only called on the active setup
    public boolean saveEquipment(InstanceTemplate template)
    {
        if (!loggedIn())
            return false;

        RoomSetup room = getRoom(template);
        if (room == null)
            return false;

        ItemContainer lpEquipment = client.getItemContainer(InventoryID.WORN);
        if (lpEquipment == null)
            return false;

        List<Integer> lpEquipmentIDs = Arrays.stream(lpEquipment.getItems())
                .map(Item::getId)
                .collect(Collectors.toList());

        room.setEquippedItems(lpEquipmentIDs);
        saveByUUID(activeSetup);
        return true;
    }

    // Only called on the active setup
    public boolean saveInventory(InstanceTemplate template)
    {
        if (!loggedIn())
            return false;

        RoomSetup room = getRoom(template);
        if (room == null)
            return false;

        ItemContainer lpInventory = client.getItemContainer(InventoryID.INV);
        if (lpInventory == null)
            return false;

        List<Integer> lpInventoryIDs = Arrays.stream(lpInventory.getItems())
                .map(Item::getId)
                .collect(Collectors.toList());

        room.setInventoryItems(lpInventoryIDs);
        saveByUUID(activeSetup);
        return true;
    }

    public boolean hasSetup(RaidSetup setup)
    {
        return raidSetupList.stream()
                .anyMatch(setupEntry -> setupEntry == setup);
    }

    public boolean hasSetup(String setupName)
    {
        return raidSetupList.stream()
                .anyMatch(setupEntry -> setupEntry.getName().equals(setupName));
    }

    public boolean hasSetup(UUID setupUUID)
    {
        return raidSetupList.stream()
                .anyMatch(setupEntry -> setupEntry.getId().equals(setupUUID));
    }

    private int getSetupIndex(RaidSetup setup)
    {
        return getSetupIndex(setup.getId());
    }

    private int getSetupIndex(UUID uuid)
    {
        RaidSetup rSetup = raidSetupList.stream()
                .filter(setup -> setup.getId().equals(uuid))
                .findFirst().orElse(null);

        return rSetup != null ? raidSetupList.indexOf(rSetup) : -1;
    }

    public RoomSetup getRoom(InstanceTemplate template)
    {
        if (activeSetup == null || template == null)
            return null;

        return activeSetup.getRoom(template);
    }

    public boolean isTagged(InstanceTemplate template, int itemId)
    {
        RoomSetup room = getRoom(template);
        if (room == null)
            return false;

        return room.isItemTagged(itemId);
    }

    public void moveSetup(RaidSetup setup, int indexOffset)
    {
        int presetIndex = getSetupIndex(setup);
        int targetIndex = presetIndex + indexOffset;

        if (targetIndex < 0 || targetIndex >= raidSetupList.size())
            return;

        // They usually share indexes, unless you somehow desync them. Which will fix on reload anyway.
        Collections.swap(raidSetupList, presetIndex, targetIndex);  // Needed for current view
        Collections.swap(uuidKeyList, presetIndex, targetIndex);    // Needed for future loads to respect order

        saveIndexList();
    }

    public void toggleItemTag(InstanceTemplate template, int itemId, boolean isDeposit)
    {
        RoomSetup room = getRoom(template);
        if (room == null)
            return;

        if (isDeposit)
            room.toggleDepositTag(itemId);
        else
            room.toggleWithdrawTag(itemId);

        saveAll();
    }

    public boolean loggedIn()
    {
        return client.getGameState() == GameState.LOGGED_IN;
    }
}