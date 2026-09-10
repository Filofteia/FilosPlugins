package filo.cm.checklist.ui;

import filo.cm.checklist.CMChecklistConfig;
import filo.cm.checklist.CMChecklistPlugin;
import filo.cm.checklist.data.PotionRole;
import filo.cm.checklist.data.PotionType;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.util.PotionUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class StorageZigzag {
    private final int DEFAULT_COL_GAP = 44;
    private final int DEFAULT_ROW_GAP = 40;

    private Client client;
    private CMChecklistConfig config;
    private CMChecklistPlugin plugin;

    private Map<Integer, Integer> indexMap = new HashMap<>();
    private List<Integer> leftOvers = new ArrayList<>();

    @Setter private boolean buildStorageCache;
    public StorageZigzag(Client client, CMChecklistPlugin plugin, CMChecklistConfig config) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
    }

    public void layout(RoomSetup setup)
    {
        leftOvers.clear();

        if (setup == null)
            return;

        List<Widget> availableWidgets = getStorageWidgets();    // All widgets
        if (availableWidgets.isEmpty())
            return;

        availableWidgets.stream()   // Probably a bit of a hacky fix but these consumed clicks on actual itemms.
                .filter(w -> w.getItemId() == ItemID.BLANKOBJECT)
                .forEach(w -> w.setHidden(true));

        int rowOffset = 0;
        List<Integer> claimedIndexes = new ArrayList<>();
        if (config.zigzag() && config.zigzagEquipment())
        {
            List<Integer> matchedEquipmentIndex =
                    getEquipmentIndexes(
                            setup.getEquippedItems(),
                            availableWidgets
                    );

            if (!matchedEquipmentIndex.isEmpty())
            {
                claimedIndexes.addAll(matchedEquipmentIndex);
                layoutWidgets(availableWidgets, matchedEquipmentIndex, rowOffset, true);
                rowOffset += 2;
            }
        }

        List<Integer> matchedInventoryIndexesT =
                getInventoryIndexesTest(
                        setup,
                        availableWidgets,
                        config.zigzag() && config.zigzagInventory()
                );

       if (config.zigzag() && config.zigzagInventory() && !matchedInventoryIndexesT.isEmpty())
       {
           claimedIndexes.addAll(matchedInventoryIndexesT);
           layoutWidgets(availableWidgets, matchedInventoryIndexesT, rowOffset, true);
           rowOffset += 4;
       }
       else
       {
           plugin.sendInventoryWidgets(matchedInventoryIndexesT);
       }


        leftOvers = getLeftoverIndexes(claimedIndexes, availableWidgets);
        if (buildStorageCache)
            buildStorageMap(leftOvers);
        else
            insertToIndexMap(leftOvers, claimedIndexes);

        if (!leftOvers.isEmpty()
                && config.zigzag()
                && (config.zigzagInventory()
                || config.zigzagEquipment())
        )
            layoutWidgets(claimedIndexes, rowOffset);
    }

    private Widget getStorageParent()
    {
        return client.getWidget(InterfaceID.RaidsStoragePrivate.ITEMS);
    }

    private List<Widget> getStorageWidgets()
    {
        Widget storageContainer = getStorageParent();
        if (storageContainer == null || storageContainer.getChildren() == null)
            return new ArrayList<>();

        List<Widget> res = new ArrayList<>(Arrays.asList(storageContainer.getChildren()));
        res.sort(Comparator.comparingInt(Widget::getIndex));
        return res;
    }

    private Widget createGhostWidget(int itemId, boolean hidden)
    {
        Widget parentWidget = getStorageParent();
        if (parentWidget == null)
            return null;

        Widget fakeItem = parentWidget.createChild(WidgetType.GRAPHIC);
        fakeItem.setItemId(itemId);
        fakeItem.setOriginalWidth(36);
        fakeItem.setOriginalHeight(32);
        fakeItem.setOpacity(200);
        fakeItem.setItemQuantityMode(0);
        fakeItem.setHidden(hidden);
        return fakeItem;
    }

    // gets the index list without creating ghosts or removing from the storageItemm lists
    private List<Integer> getInventoryIndexesTest(RoomSetup setup, List<Widget> storageItems, boolean invasive)
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null)
            return new ArrayList<>();

        Item[] currentItems = inventory.getItems();
        List<Integer> requiredIDs = setup.getInventoryItems();
        List<Integer> result = new ArrayList<>();
        List<Widget> widgets = new ArrayList<>(storageItems);

        widgets.sort(Comparator.comparingInt(Widget::getIndex));

        int playerBrews = PotionUtil.getPotionQuantity(1, currentItems, PotionType.SARADOMIN_BREW, PotionType.XERICS_AID_STRONG);
        int playerRestores = PotionUtil.getPotionQuantity(1, currentItems, PotionType.SUPER_RESTORE, PotionType.REVITALISATION_STRONG);
        int brewsToGhost = Math.min(setup.getBrewCount(), playerBrews);
        int restToGhost = Math.min(setup.getRestCount(), playerRestores);
        int brewCounter = 0;
        int restCounter = 0;

        for (int i = 0; i < requiredIDs.size(); i++)
        {
            int requiredId = requiredIDs.get(i);
            if (requiredId <= 0)
                continue;

            int currentId = i < currentItems.length
                    ? currentItems[i].getId()
                    : -1;

            boolean storageExists = widgets.stream().anyMatch(w -> w.getItemId() == requiredId);
            boolean stackable = client.getItemDefinition(requiredId).isStackable();

            boolean shouldCreateGhost = false;
            int displayIndex = -1;

            if (isBrew(requiredId))
            {
                boolean shouldGhost = brewCounter < brewsToGhost;
                brewCounter++;
                if (shouldGhost)
                    shouldCreateGhost = true;
                else
                    displayIndex = findFirstInventoryMatch(widgets, requiredId, false);
            }

            else if (isRestore(requiredId))
            {
                boolean shouldGhost = restCounter < restToGhost;
                restCounter++;
                if (shouldGhost)
                    shouldCreateGhost = true;
                else
                    displayIndex = findFirstInventoryMatch(widgets, requiredId, false);
            }

            else if (storageExists && !slotMatches(requiredId, currentId))
            {
                displayIndex = findFirstInventoryMatch(widgets, requiredId, false);
            }

            else if (storageExists && slotMatches(requiredId, currentId))
            {
                if (stackable)
                    displayIndex = findFirstInventoryMatch(widgets, requiredId, false);
                else
                    shouldCreateGhost = true;
            }
            else    // Just !storageExists, so we should look for alternaatives if slot not fulfilled
            {
                if (!slotMatches(requiredId, currentId))
                {
                    displayIndex = findFirstInventoryMatch(widgets, requiredId, false);
                }
                else
                {
                    shouldCreateGhost = true;
                }
            }

            if (displayIndex == -2 || shouldCreateGhost)
            {
                if (invasive)
                    result.add(createGhostIndex(requiredId));

                continue;
            }

            if (displayIndex != -1) // can be from methods
            {
                result.add(displayIndex);
                if (invasive)
                    removeWidgetByIndex(widgets, displayIndex);
            }
        }

        return result;
    }

    private boolean removeWidgetByIndex(List<Widget> widgets, int widgetIndex)
    {
        return widgets.removeIf(w -> w.getIndex() == widgetIndex);
    }

    /**
     * Find the index of the first inventory slot matching reqId
     * @param widgets All available widgets
     * @param reqId ID you wish to find
     * @param invsaive
     * @return Item Index, or -1 if null, or -2 if createGhost
     */
    private int findFirstInventoryMatch(List<Widget> widgets, int reqId, boolean invsaive)
    {
        int bestScore = Integer.MAX_VALUE;
        int widgetIndex = -1;

        for (int i = 0; i < widgets.size(); i++)
        {
            Widget widget = widgets.get(i);

            int widgetId = widget.getItemId();
            if (widgetId == 6512)   // null thing
                continue;

            int itemScore = matchScore(reqId, widgetId);
            if (itemScore == 0) {
                widgetIndex = widget.getIndex();
                return widgetIndex;
            }

            if (itemScore < bestScore)
            {
                bestScore = itemScore;
                widgetIndex = widget.getIndex();
            }
        }

        if (widgetIndex == -1)  // non-existent, create a ghost instead (-2 = ghost req)
            widgetIndex = -2;//createGhostIndex(reqId);

        return widgetIndex;
    }

    private int createGhostIndex(int itemId)
    {
        Widget ghost = createGhostWidget(itemId, true);
        if (ghost == null)
            return -1;
        return ghost.getIndex();
    }

    private int findItemMatch(List<Widget> widgets, int itemId)
    {
        for (int i = 0; i < widgets.size(); i++)
        {
            Widget widget = widgets.get(i);
            if (widget.getItemId() == itemId)
            {
//                widgets.remove(widget);
                return widget.getIndex();
            }
        }

        return -1;
    }

    private List<Integer> getEquipmentIndexes(List<Integer> requiredIDs, List<Widget> widgets)
    {
        List<Integer> results = new ArrayList<>();
        for (int reqId : requiredIDs)
        {
            if (reqId <= 0)
                continue;

            int match = findItemMatch(widgets, reqId);
            if (match != -1)
            {
                results.add(match);
            }
            else
            {
                int ghostId = createGhostIndex(reqId);
                results.add(ghostId);
            }
        }

        return results;
    }

    private List<Integer> getLeftoverIndexes(List<Integer> usedIndexes, List<Widget> widgets)
    {
        List<Integer> results = new ArrayList<>();

        for (Widget w : widgets)
        {
            if (usedIndexes.contains(w.getIndex()) || w.getItemId() == ItemID.BLANKOBJECT)
                continue;

            results.add(w.getIndex());
        }

        return results;
    }

    // Leftovers with map support
    private void layoutWidgets(List<Integer> claimedIndexes, int rowOffset)
    {
        List<Widget> allWidgets = getStorageWidgets();

        int startX = 0;
        int startY = rowOffset * DEFAULT_ROW_GAP;
        for (Map.Entry<Integer, Integer> entrySet : indexMap.entrySet())
        {
            int widgetSlot = entrySet.getKey();
            int widgetIndex = entrySet.getValue();

            if (widgetIndex < 0 || widgetIndex >= allWidgets.size())
                continue;

            Widget widget = allWidgets.get(widgetIndex);
            if (claimedIndexes.contains(widgetIndex)
                || widget.getItemId() == ItemID.BLANKOBJECT)
                continue;

            widget.setHidden(false);

            int maxRow = config.compactZigzag() ? 9 : 8;
            int row = widgetSlot / maxRow;
            int col = widgetSlot % maxRow;

            widget.setOriginalX(startX + col * (config.compactZigzag() ? 38 : DEFAULT_COL_GAP));
            widget.setOriginalY(startY + row * (config.compactZigzag() ? 34 : DEFAULT_ROW_GAP));
            widget.setOnDragListener((Object[]) null);
            widget.revalidate();
        }
    }

    private void layoutWidgets(List<Widget> availableWidgets, List<Integer> widgetIndexes, int rowOffset, boolean zigzag)
    {
        List<Widget> allWidgets = getStorageWidgets();

        int startX = 0;
        int startY = rowOffset * DEFAULT_ROW_GAP;
        int slot = 0;

        for (int idx : widgetIndexes)
        {
            if (idx == -1 || idx >= allWidgets.size())
                continue;

            Widget widget = allWidgets.get(idx);
            if (widget == null)
                continue;

            if (widget.isHidden())
                widget.setHidden(false);

            int maxRow = config.compactZigzag() ? 9 : 8;
            int maxZigzag = maxRow * 2;
            int row = zigzag ? slot % 2 + ((slot / maxZigzag) * 2) : slot / maxRow;
            int col = zigzag ? slot / 2 % maxRow : slot % maxRow;

            widget.setOriginalX(startX + col * (config.compactZigzag() ? 38 : DEFAULT_COL_GAP));
            widget.setOriginalY(startY + row * (config.compactZigzag() ? 34 : DEFAULT_ROW_GAP));
            widget.setOnDragListener((Object[]) null);  // 1609 bug might rewrite if wanted
            widget.revalidate();

            slot++;
        }
    }

    public boolean slotMatches(int itemId, int requiredId)
    {
        if (itemId == requiredId)
            return true;

        PotionType reqType = PotionType.fromItemId(requiredId);
        return reqType != null &&
                reqType.contains(itemId);
    }

    private boolean isBrew(int reqId)
    {
        PotionType type = PotionType.fromItemId(reqId);
        return type == PotionType.SARADOMIN_BREW
                || type == PotionType.XERICS_AID_STRONG
                || type == PotionType.XERICS_AID
                || type == PotionType.XERICS_AID_WEAK;
    }

    private boolean isRestore(int reqId)
    {
        PotionType type = PotionType.fromItemId(reqId);
        return type == PotionType.SUPER_RESTORE
                || type == PotionType.REVITALISATION_STRONG
                || type == PotionType.REVITALISATION
                || type == PotionType.REVITALISATION_WEAK;
    }

    private int matchScore(int requiredId, int widgetId)
    {
        boolean standardPots = config.standardPots();
        if (requiredId == widgetId) // Same Item
            return 0;

        PotionType potionTypeRequired = PotionType.fromItemId(requiredId);
        if (potionTypeRequired == null)
            return Integer.MAX_VALUE;

        PotionType potionTypeWidget = PotionType.fromItemId(widgetId);
        if (potionTypeWidget == null)
            return Integer.MAX_VALUE;

        int potionClamp = 1;    // Was going to make this a config option but not
        int requiredDose = potionTypeRequired.getDoses(requiredId);
        int widgetDose = potionTypeWidget.getDoses(widgetId);
        if (widgetDose < requiredDose && widgetDose < potionClamp)
            return Integer.MAX_VALUE;

        int bestScore = Integer.MAX_VALUE;
        if (potionTypeRequired == potionTypeWidget)
            bestScore = requiredDose - widgetDose;

        PotionRole reqRole = potionTypeRequired.getRole();
        PotionRole widgetRole = potionTypeWidget.getRole();
        if (reqRole == widgetRole && standardPots && reqRole != PotionRole.OTHER)
            bestScore = requiredDose - widgetDose;

        return bestScore;
    }

    private void buildStorageMap(List<Integer> indexList)
    {
        buildStorageCache = false;
        indexMap.clear();
        int slotIdx = 0;
        for (int index : indexList)
        {
            indexMap.put(slotIdx, index);
            slotIdx++;
        }
    }

    private void insertToIndexMap(List<Integer> indexes, List<Integer> claimedIndexes)
    {
        for (int index : indexes)
        {

            if (claimedIndexes.contains(index))
                continue;

            if (indexMap.containsValue(index))
                continue;

            int firstSlotEmpty = Integer.MAX_VALUE;

            for (Map.Entry<Integer, Integer> entry : indexMap.entrySet())
            {
                if (!leftOvers.contains(entry.getValue()))
                {
                    if (entry.getKey() < firstSlotEmpty)
                        firstSlotEmpty = entry.getKey();
                }
            }
            if (firstSlotEmpty == Integer.MAX_VALUE)
            {
                firstSlotEmpty = indexMap.keySet().stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(-1) + 1;
            }

            indexMap.put(firstSlotEmpty, index);
        }
    }
}