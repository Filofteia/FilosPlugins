package filo.cm.checklist.util;

import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.ItemBoxType;
import filo.cm.checklist.panel.comp.storage.ItemBox;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;

public class ItemBoxFactory {
    private final ClientThread clientThread;
    private final Client client;
    private final SaveManager saveManager;
    private final ItemManager itemManager;
    private final ChatboxItemSearch chatboxItemSearch;

    public ItemBoxFactory(ClientThread clientThread, Client client, SaveManager saveManager, ItemManager itemManager, ChatboxItemSearch chatboxitemSearch) {
        this.clientThread = clientThread;
        this.client = client;
        this.saveManager = saveManager;
        this.itemManager = itemManager;
        this.chatboxItemSearch = chatboxitemSearch;
    }

    public ItemBox createItemBox(
            int idx,
            ItemBoxType type,
            InstanceTemplate instanceTemplate
    )
    {
        return new ItemBox(clientThread, client, saveManager, itemManager, idx, type, chatboxItemSearch, instanceTemplate, null);
    }

    public ItemBox createItemBox(
            int idx,
            ItemBoxType type,
            InstanceTemplate instanceTemplate,
            Runnable refresh
    )
    {
        return new ItemBox(clientThread, client, saveManager, itemManager, idx, type, chatboxItemSearch, instanceTemplate, refresh);
    }
}
