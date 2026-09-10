/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2026, Filofteia
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package filo.cm.checklist;

import com.google.gson.Gson;
import com.google.inject.Provides;
import filo.cm.checklist.data.BrewContext;
import filo.cm.checklist.data.InstanceTemplate;
import filo.cm.checklist.data.PotionType;
import filo.cm.checklist.data.save.RaidSetup;
import filo.cm.checklist.data.save.RoomSetup;
import filo.cm.checklist.overlay.ItemHighlights;
import filo.cm.checklist.panel.CMChecklistPanel;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.*;
import filo.cm.checklist.ui.StorageZigzag;
import filo.cm.checklist.util.ItemBoxFactory;
import filo.cm.checklist.util.SaveManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "CM Storage Presets",
		description = "An Inventory Setup equivalent for Challenge Mode CoX",
		tags = "raids,cox,cm,inventory,qol"
)
public class CMChecklistPlugin extends Plugin {

	@Inject	private Client client;
	@Inject	private ConfigManager configManager;
	@Inject	private OverlayManager overlayManager;
	@Inject	private CMChecklistConfig config;
	@Inject	private ClientThread clientThread;
	@Inject	private ItemHighlights overlay;
	@Inject	private ItemManager itemManager;
	@Inject	private ClientToolbar toolbar;
	@Inject	private ChatboxItemSearch chatboxItemSearch;
	@Getter @Inject private Gson gson;

	private NavigationButton panelButton;
	private CMChecklistPanel pluginPanel;
	private ItemBoxFactory itemBoxFactory;
	private SaveManager saveManager;

	private StorageZigzag storageZigzag;
	@Getter	private BrewContext brewContext = null;
	@Getter	private RoomSetup activeRoomSetup = null;
	@Getter	private InstanceTemplate activeTemplate = null;

	private boolean isChallengeMode = false;
	private boolean inRaid = false;

	private final String SAVE_SETUP_TEXT = "Save Setup";
	private final String CLEAR_SETUP_TEXT = "Clear Setup";

	private final String MARK_ITEM_DEPOSIT_TEXT = "Mark-deposit";
	private final String UNMARK_ITEM_DEPOSIT_TEXT = "Unmark-deposit";
	private final String MARK_ITEM_WITHDRAW_TEXT = "Mark-withdraw";
	private final String UNMARK_ITEM_WITHDRAW_TEXT = "Unmark-withdraw";

	private final int[] CHEST_OBJ_IDS = {29769, 29770, 29779, 29780, 37978};

	@Override
	protected void startUp() throws Exception {
		storageZigzag = new StorageZigzag(client, this, config);
		overlayManager.add(overlay);

		saveManager = new SaveManager(configManager, this, client, clientThread);
		itemBoxFactory = new ItemBoxFactory(clientThread, client, saveManager, itemManager, chatboxItemSearch);
		pluginPanel = new CMChecklistPanel(this, config, saveManager, clientThread, itemBoxFactory);

		BufferedImage icon = ImageUtil.loadImageResource(CMChecklistPlugin.class, "icon.png");
		panelButton = NavigationButton.builder()
				.tooltip("CM Preset Panel")
				.panel(pluginPanel)
				.icon(icon)
				.build();

		toolbar.addNavigation(panelButton);

		clientThread.invoke(() -> {
			if (client.getGameState() != GameState.LOGGED_IN)
				return;

			isChallengeMode = client.getVarbitValue(VarbitID.RAIDS_CHALLENGE_MODE) > 0;
			inRaid = client.getVarbitValue(VarbitID.RAIDS_CLIENT_INDUNGEON) > 0;
			refreshPrivateStorage(true);
		});
	}

	@Override
	protected void shutDown() throws Exception {
		toolbar.removeNavigation(panelButton);
		overlayManager.remove(overlay);

		activeRoomSetup = null;
		activeTemplate =  null;
		brewContext = null;
		panelButton = null;
		saveManager = null;
		pluginPanel = null;
		isChallengeMode = false;
		inRaid = false;

		clientThread.invokeLater(() -> refreshPrivateStorage(false));
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		if (widgetLoaded.getGroupId() != InterfaceID.RAIDS_STORAGE_PRIVATE)
			return;

		if (activeRoomSetup == null)
			return;

		storageZigzag.setBuildStorageCache(true);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == 1607) // 1606 was the original
		{
			storageZigzag.layout(activeRoomSetup);
		}
	}

	private final List<Integer> mismatchedItems = new ArrayList<>();
	@Subscribe
	public void onGameTick(GameTick gameTick) {
		overlay.clearIndexes();
		mismatchedItems.clear();

		if (!inRaid || !isChallengeMode) {
			activeTemplate = null;
			activeRoomSetup = null;
			brewContext = null;
			return;
		}


		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
			return;

		int pPosPlane = client.getTopLevelWorldView().getPlane();
		int pPosX = localPlayer.getLocalLocation().getSceneX();
		int pPosY = localPlayer.getLocalLocation().getSceneY();

		WorldView pWorldView = localPlayer.getWorldView();
		if (pWorldView == null)
			return;

		int templateId = pWorldView.getInstanceTemplateChunks()[pPosPlane][pPosX / 8][pPosY / 8];
		InstanceTemplate template = InstanceTemplate.findMatch(templateId, pPosPlane);

		if (activeTemplate != template)
			onTemplateChanged(template);

		activeRoomSetup = saveManager.getRoom(template);
		activeTemplate = template;
		if (activeTemplate != null && activeRoomSetup != null)
		{
			ItemContainer playerInventory = client.getItemContainer(InventoryID.INV);
			if (playerInventory != null) {
				brewContext = new BrewContext(activeRoomSetup, playerInventory);
			}
		}

		if (config.highlightMismatched() && activeRoomSetup != null) {
			ItemContainer playerInventoryContainer = client.getItemContainer(InventoryID.INV);
			if (playerInventoryContainer == null)
				return;

			Item[] actualItems = playerInventoryContainer.getItems();
			List<Integer> requiredItems = activeRoomSetup.getInventoryItems();
			for (int i = 0; i < requiredItems.size(); i++) {
				int requiredId = requiredItems.get(i);
				int actualId = i < actualItems.length ? actualItems[i].getId() : -1;

				if (!PotionType.isPotionMatch(requiredId, actualId) && (requiredId != actualId))
					mismatchedItems.add(i);
			}
		}

		overlay.updateMismatchedItems(mismatchedItems);
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded e) {
		if (activeTemplate == null)
			return;

		int type = e.getType();
		if (type >= MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET)
			type -= MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;

		final MenuEntry menuEntry = e.getMenuEntry();
		final MenuAction menuAction = MenuAction.of(type);

		if (menuAction == MenuAction.EXAMINE_OBJECT && config.chestMenuOptions()) {
			int objectId = e.getIdentifier();
			for (int chestId : CHEST_OBJ_IDS) {
				if (chestId == objectId) {
					client.getMenu().createMenuEntry(-1)
							.setOption(CLEAR_SETUP_TEXT)
							.setTarget(e.getTarget())
							.setIdentifier(e.getIdentifier())
							.setType(MenuAction.RUNELITE)
							.onClick(this::clearSetupData);

					client.getMenu().createMenuEntry(-1)
							.setOption(SAVE_SETUP_TEXT)
							.setTarget(e.getTarget())
							.setIdentifier(e.getIdentifier())
							.setType(MenuAction.RUNELITE)
							.onClick(this::writeSetupData);
				}
			}
		}

		if (menuAction == MenuAction.CC_OP
				&& ((menuEntry.getOption().equalsIgnoreCase("withdraw-x")
				&& e.getActionParam1() >> 16 == InterfaceID.RAIDS_STORAGE_PRIVATE)
				|| (menuEntry.getOption().equalsIgnoreCase("store-x")
				&& e.getActionParam1() >> 16 == InterfaceID.RAIDS_SIDEPANEL)))
		{
			Widget itemWidget = menuEntry.getWidget();
			if (itemWidget == null)
				return;

			int widgetItemId = itemWidget.getItemId();
			boolean isTagged = saveManager.isTagged(activeTemplate, widgetItemId);
			String markOptWithdraw = !isTagged ? MARK_ITEM_WITHDRAW_TEXT : UNMARK_ITEM_WITHDRAW_TEXT;
			String menuOptDeposit = !isTagged ? MARK_ITEM_DEPOSIT_TEXT : UNMARK_ITEM_DEPOSIT_TEXT;

			client.getMenu().createMenuEntry(-1)
					.setOption(menuEntry.getOption().equalsIgnoreCase("store-x") ? menuOptDeposit : markOptWithdraw)
					.setTarget(e.getTarget())
					.setIdentifier(e.getIdentifier())
					.setItemId(widgetItemId)
					.setType(MenuAction.RUNELITE)
					.onClick(this::toggleMarked);
		}
	}

	public void writeSetupData(MenuEntry entry) {
		if (activeTemplate == null)
			return;

		boolean setupSaved = saveEquipment(activeTemplate);
		if (setupSaved)
			SwingUtilities.invokeLater(pluginPanel::buildStorageUI);
	}

	public boolean saveEquipment(InstanceTemplate templates) {
		RoomSetup room = saveManager.getRoom(templates);
		if (room == null)
			return false;

		boolean equipment = saveManager.saveEquipment(activeTemplate);
		boolean inventory = saveManager.saveInventory(activeTemplate);
		return equipment && inventory;
	}

	public void clearSetupData(MenuEntry entry) {
		if (activeTemplate == null)// || activeRoomSetup == null)
			return;

		clearRoomSetuo(activeTemplate);
	}

	public void clearRoomSetuo(InstanceTemplate templates) {
		RoomSetup room = saveManager.getRoom(templates);
		if (room == null)
			return;

		room.clearSetup();
		saveManager.saveAll();
	}


	public boolean requestLoadPreset(InstanceTemplate template) {
		RoomSetup room = saveManager.getRoom(template);
		if (room == null)
			return false;

		SwingUtilities.invokeLater(() -> {
			pluginPanel.requestSetup(template, room);
		});

		return true;
	}

	public void requestDeletePreset(RaidSetup setup) {
		int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this preset?", "Delete Preset", JOptionPane.YES_NO_OPTION);
		if (res != JOptionPane.YES_OPTION)
			return;

		if (saveManager.deleteByUUID(setup.getId()))
			pluginPanel.buildPresetUI();
	}

	public void rebuildPresetPanel()
	{
		SwingUtilities.invokeLater(() -> pluginPanel.buildPresetUI());
	}

	public void toggleMarked(MenuEntry entry)
	{
		if (activeTemplate == null || activeRoomSetup == null)
			return;

		boolean isDeposit = entry.getOption().contains("deposit");
		saveManager.toggleItemTag(activeTemplate, entry.getItemId(), isDeposit);
	}

	private void refreshPrivateStorage(boolean runZigZag)
	{
		clientThread.invokeLater(() -> {
			Widget items = client.getWidget(InterfaceID.RAIDS_STORAGE_PRIVATE, 6);
			Widget scrollbar = client.getWidget(InterfaceID.RAIDS_STORAGE_PRIVATE, 7);
			Widget occupied = client.getWidget(InterfaceID.RAIDS_STORAGE_PRIVATE, 2);

			if (items == null || scrollbar == null || occupied == null)
				return;

			int itemScriptComp = items.getId();
			int scrollbarScriptComp = scrollbar.getId();
			int occupiedScriptComp = occupied.getId();

			client.runScript(1607, itemScriptComp, scrollbarScriptComp, occupiedScriptComp);
			if (runZigZag)
				storageZigzag.layout(saveManager.getRoom(activeTemplate));
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equalsIgnoreCase("cmchecklist"))
		{
			if (e.getKey().equalsIgnoreCase("zigzag")
			|| e.getKey().equalsIgnoreCase("zigzagEquipment")
			|| e.getKey().equalsIgnoreCase("zigzagInventory"))
				refreshPrivateStorage(true);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		final int varbitId = e.getVarbitId();
		final int varbitValue = e.getValue();

		if (varbitId == VarbitID.RAIDS_CHALLENGE_MODE)
			isChallengeMode = varbitValue >0;

		if (varbitId == VarbitID.RAIDS_CLIENT_INDUNGEON)
			inRaid = varbitValue >0;
	}

	private void onTemplateChanged(InstanceTemplate newTemplate)
	{
		if (config.autoUpdateRoom())
			requestLoadPreset(newTemplate);
	}

	public void requestStoragePanel(RaidSetup setup)
	{
		if (!saveManager.hasSetup(setup))
			return;

		saveManager.setActiveSetup(setup);
		pluginPanel.displayStoragePanel();
		refreshPrivateStorage(true);
	}

	public void requestClosePreset()
	{
		pluginPanel.displayPresetPanel();
		sendInventoryWidgets(new ArrayList<>());
	}

	public void sendInventoryWidgets(List<Integer> inventoryWidgets)
	{
		overlay.updateInventoryItems(inventoryWidgets);
	}

	@Provides
	CMChecklistConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CMChecklistConfig.class);
	}
}