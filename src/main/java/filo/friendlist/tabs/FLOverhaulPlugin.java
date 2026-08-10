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

package filo.friendlist.tabs;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Runnables;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;

import filo.friendlist.tabs.config.GroupCount;
import filo.friendlist.tabs.config.RemoveType;
import filo.friendlist.tabs.data.FriendTab;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Friend;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Nameable;
import net.runelite.api.ScriptEvent;
import net.runelite.api.ScriptID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NameableNameChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Friend Tabs",
		tags = "Organisation,UI,Friends",
		description = "Re-order your Friends list with tabs / grouping"
)
public class FLOverhaulPlugin extends Plugin
{
	@Inject	private Client client;
	@Inject	private ClientThread clientThread;
	@Inject	private FLOverhaulConfig config;
	@Inject	private ChatboxPanelManager panelManager;
	@Inject	private ColorPickerManager colorPickerManager;
	@Inject	private ConfigManager configManager;
	@Inject	private Gson gson;

	private FLGroupManager groupManager;

	@Override
	protected void startUp() throws Exception
	{
		groupManager = new FLGroupManager(gson, configManager, config);
		lastTickCount = 0;
		cacheScrollY = 0;

		if (client.getGameState() == GameState.LOGIN_SCREEN)
			return;

		groupManager.loadConfig();
		clientThread.invokeLater(this::addGroupButton);
		clientThread.invokeLater(this::refreshFriendPanel);
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (client.getGameState() == GameState.LOGIN_SCREEN)
			return;

		groupManager.saveConfig();
		clientThread.invokeLater(this::removeGroupButton);
		clientThread.invokeLater(this::refreshFriendPanel);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		//TODO: Consider making this only load if state is from LOGGING_IN -> LOADING, so it doesn't load every chunk
		if (e.getGameState() == GameState.LOGGED_IN)
			groupManager.loadConfig();

		if (e.getGameState() == GameState.LOGGING_IN)
		{
			lastTickCount = 0;
			cacheScrollY = 0;
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened menuOpened)
	{
		if (config.removeFriendType() == RemoveType.DEFAULT)
			return;

		Widget friendContainer = client.getWidget(InterfaceID.FRIENDS, 11);
		if (friendContainer == null || friendContainer.isHidden())
			return;

		MenuEntry[] baseEntries = menuOpened.getMenuEntries();
		List<MenuEntry> recolourEntries = new ArrayList<>();
		for (MenuEntry menuEntry : baseEntries)
		{
			String menuTarget = formatName(menuEntry.getTarget());
			if (menuEntry.getType() == MenuAction.CC_OP && groupManager.playerHasGroup(menuTarget) && menuEntry.getOption().equalsIgnoreCase("delete"))
			{
				if (config.removeFriendType() == RemoveType.CONSUME)
					menuEntry.setOption(ColorUtil.wrapWithColorTag(menuEntry.getOption(), Color.GRAY));
				if (config.removeFriendType() == RemoveType.RECOLOUR)
					menuEntry.setOption(ColorUtil.wrapWithColorTag(menuEntry.getOption(), Color.RED));
				if (config.removeFriendType() == RemoveType.HIDE)
					continue;
			}

			recolourEntries.add(menuEntry);
		}

		client.getMenu().setMenuEntries(recolourEntries.toArray(new MenuEntry[0]));
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (config.removeFriendType() != RemoveType.CONSUME)
			return;

		String formattedName = formatName(e.getMenuTarget());
		String menuOption = Text.removeTags(e.getMenuOption());	// Added tags from the recolour above
		int widgetId = WidgetUtil.componentToInterface(e.getParam1());
		if (widgetId == InterfaceID.FRIENDS
				&& menuOption.equalsIgnoreCase("delete")
				&& groupManager.playerHasGroup(formattedName))
		{
			sendChatMessage("Friend Tabs has prevented you from deleting " + formattedName + ". You can disable this in the config.");
			e.consume();
		}
	}

	private int lastTickCount = 0;
	private int cacheScrollY = 0;
	@Subscribe
	public void onScriptPreFired(ScriptPreFired e)
	{
		int scriptId = e.getScriptId();
		if (scriptId == ScriptID.FRIENDS_UPDATE)
		{
			// first script has the correct friendContainer::getScrollY, any script after returns 0
			if (lastTickCount == client.getTickCount())
				return;

			Widget friendContainer = client.getWidget(InterfaceID.FRIENDS, 11);
			if (friendContainer == null || friendContainer.isHidden())
				return;

			lastTickCount = client.getTickCount();
			cacheScrollY = friendContainer.getScrollY();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e)
	{
		// TODO: Random Events break the layout and re-opening fixes it
		int scriptId = e.getScriptId();
		final int FRIENDS_SORT_UPDATE = 1670;
		final int FRIEND_INIT = 123;
		final int EMOTE_INIT = 699; // Script 699 is emote_init, but the FRIEND_INIT (123) didn't work

		if (scriptId == FRIEND_INIT)
			addGroupButton();

		if (scriptId == ScriptID.FRIENDS_UPDATE || scriptId == FRIENDS_SORT_UPDATE || scriptId == EMOTE_INIT)
			reorderFriendList();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded entryAdded)
	{
		if (!groupManager.hasAnyGroups())
			return;

		int groupId = WidgetUtil.componentToInterface(entryAdded.getActionParam1());
		if (groupId == InterfaceID.FRIENDS && entryAdded.getOption().equals("Message"))
		{
			if (Strings.isNullOrEmpty(entryAdded.getTarget()))
				return;

			List<String> availableGroups = new ArrayList<>(groupManager.getGroupNames());
			String playerName = formatName(entryAdded.getTarget());
			Set<String> playerGroups = groupManager.getPlayerGroups(playerName);
			if (playerGroups != null)
			{
				availableGroups.removeAll(playerGroups);
			}

			MenuEntry sectionMenu = client.getMenu().createMenuEntry(-1)
					.setOption("Section")
					.setType(MenuAction.RUNELITE)
					.setTarget(entryAdded.getTarget());

			Menu sectionSubMenu = sectionMenu.createSubMenu();
			if (playerGroups != null && !playerGroups.isEmpty())
			{
				for (String playerGroup : playerGroups) {
					Color groupColour = groupManager.getTextColour(playerGroup);
					String groupNameColour = ColorUtil.wrapWithColorTag(playerGroup, groupColour);

					sectionSubMenu.createMenuEntry(-1)
							.setOption(ColorUtil.wrapWithColorTag("Remove from", Color.RED))
							.setType(MenuAction.RUNELITE)
							.onClick((c) -> {
								groupManager.removePlayerFromGroup(playerName, playerGroup);
								refreshFriendPanel();
							})
							.setTarget(config.colourMenuEntries() ? groupNameColour : playerGroup);
				}
			}

			for (String groupName : availableGroups)
			{
				Color groupColour = groupManager.getTextColour(groupName);
				String groupNameColour = ColorUtil.wrapWithColorTag(groupName, groupColour);

				sectionSubMenu.createMenuEntry(0)
						.setOption(ColorUtil.wrapWithColorTag("Add to", Color.GREEN))
						.onClick((c) -> {
							groupManager.addPlayerToGroup(playerName, groupName);
							refreshFriendPanel();
						})
						.setTarget(config.colourMenuEntries() ? groupNameColour : groupName)
						.setType(MenuAction.RUNELITE);
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (!e.getGroup().equalsIgnoreCase("filofriendtab"))
			return;

		refreshFriendPanel();
	}

	@Subscribe
	public void onNameableNameChanged(NameableNameChanged e)
	{
		Nameable nameable = e.getNameable();
		if (nameable instanceof Friend)
		{
			if (nameable.getPrevName() == null || nameable.getPrevName().isBlank())
				return;

			String sourceName = formatName(nameable.getPrevName());
			String targetName = formatName(nameable.getName());

			if (groupManager.playerHasGroup(sourceName))
				groupManager.migratePlayer(sourceName, targetName);
		}

		refreshFriendPanel();
	}

	private void migrateNameChanges()
	{
		Friend[] friends = client.getFriendContainer().getMembers();
		for (Friend friend : friends)
		{
			if (friend.getPrevName() == null || friend.getPrevName().isBlank())	// Happens on some people for no reason
				continue;

			String sourceName = formatName(friend.getPrevName());
			String targetName = formatName(friend.getName());

			if (groupManager.playerHasGroup(sourceName))
				groupManager.migratePlayer(sourceName, targetName);
		}

		clientThread.invokeAtTickEnd(this::refreshFriendPanel);
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged e)
	{
		groupManager = new FLGroupManager(gson, configManager, config);
		lastTickCount = 0;
		cacheScrollY = 0;
	}

	private Map<String, Integer> nameToIndex;
	private Widget[] friendListWidgets;
	private void reorderFriendList()
	{
		Widget friendContainer = client.getWidget(InterfaceID.FRIENDS, 11);
		if (friendContainer == null || friendContainer.isHidden())
			return;

		nameToIndex = new LinkedHashMap<>();
		friendListWidgets = friendContainer.getDynamicChildren();

		int friendCount = client.getFriendContainer().getCount();
		for (int i = 0; i < friendCount * 3; i += 3)
		{
			Widget friendWidget = friendListWidgets[i];
			String playerName = formatName(friendWidget.getText());
			nameToIndex.put(playerName, i);
		}

		Set<String> renderedPlayers = new HashSet<>();
		List<String> ungroupedFriends = new ArrayList<>();
		boolean isExpanded;

		int widgetScrollY = 0;
		int fontSpacing = getFontSpacing();
		int headerOffset = getHeaderSpacing();
		for (String groupName : groupManager.getGroupNames())
		{
			if (widgetScrollY != 0)
				widgetScrollY += headerOffset;

			List<String> playerNames = groupManager.getPlayers(groupName);
			if (playerNames == null)
				continue;

			List<String> sortedPlayers = playerNames.stream()
					.filter(nameToIndex::containsKey)
					.sorted(Comparator.comparingInt(nameToIndex::get))
					.collect(Collectors.toList());

			if (config.hideOfflineGroups() && getGroupOnline(groupName) == 0)
			{
				widgetScrollY -= headerOffset;
				for (String player : sortedPlayers)
				{
					if (getPlayerOnlineGroups(player) > 0)
						continue;
					if (ungroupedFriends.contains(player))
						continue;

					ungroupedFriends.add(player);
				}

				continue;
			}

			isExpanded = groupManager.isExpanded(groupName);
			createFolder(friendContainer, groupName, widgetScrollY, sortedPlayers.size(), isExpanded);
			widgetScrollY += fontSpacing;

			for (String player : sortedPlayers)
			{
				if (!nameToIndex.containsKey(player))
					continue;

				if (renderedPlayers.add(player))
					widgetScrollY += layoutFriendWidget(isExpanded, widgetScrollY, player);
				else
					widgetScrollY += dupeFriendWidget(friendContainer, widgetScrollY, player, isExpanded);
			}
		}

		for (String name : nameToIndex.keySet())
		{
			if (!groupManager.playerHasGroup(name))	// Cannot dupe previous because they all have groups
				ungroupedFriends.add(name);
		}

		if (!ungroupedFriends.isEmpty())
		{
			if (widgetScrollY != 0)
				widgetScrollY += headerOffset;

			isExpanded = groupManager.isExpanded("ungrouped");
			createFolder(friendContainer, "Ungrouped", widgetScrollY, ungroupedFriends.size(), isExpanded);

			widgetScrollY += fontSpacing;

			ungroupedFriends.sort(Comparator.comparingInt(nameToIndex::get));
			for (String player : ungroupedFriends)
			{
				if (renderedPlayers.add(player))
					widgetScrollY += layoutFriendWidget(isExpanded, widgetScrollY, player);
				else
					widgetScrollY += dupeFriendWidget(friendContainer, widgetScrollY, player, isExpanded);
			}
		}

		friendContainer.revalidate();
		friendContainer.setScrollHeight(widgetScrollY + getHeaderSpacing());
		friendContainer.revalidateScroll();

		clientThread.invokeAtTickEnd(() ->
				client.runScript(ScriptID.UPDATE_SCROLLBAR,
						InterfaceID.Friends.SCROLLBAR,
						InterfaceID.Friends.LIST,
						cacheScrollY));
	}

	private int layoutFriendWidget(boolean isExpanded, int yOffset, String playerName)
	{
		int index = nameToIndex.get(playerName);
		if (index == -1)
			return 0;

		Widget name = friendListWidgets[index];
		Widget icon = friendListWidgets[index+1];
		Widget world = friendListWidgets[index+2];

		if (config.recolourFriends())
			name.setTextColor(world.getTextColor());

		name.setForcedPosition(config.groupSpacing(), yOffset);
		icon.setForcedPosition(icon.getOriginalX() + config.groupSpacing(), yOffset);
		world	// setForcedPosition worked for the yOffset, but not for the groupSpacing from originalX
				.setOriginalX(world.getOriginalX() - config.groupSpacing())
				.setOriginalY(yOffset);

		if (!isExpanded)
		{
			name.setHidden(true);
			icon.setHidden(true);
			world.setHidden(true);
			return 0;
		}

		return 15;
	}

	private int dupeFriendWidget(Widget parent, int yOffset, String playerName, boolean isExpanded)
	{
		int index = nameToIndex.get(playerName);
		if (index == -1 || !isExpanded)
			return 0;

		Friend friend = getFriend(playerName);
		if (friend == null)
			return 0;

		for (int i = 0; i <= 2; i++)	// 0 is name, 1 is icon, 2 is world
		{
			Widget sourceWidget = friendListWidgets[index+i];
			Widget widget = parent.createChild(-1, sourceWidget.getType());

			widget.setName(sourceWidget.getName())
					.setFontId(sourceWidget.getFontId())
					.setText(sourceWidget.getText())
					.setTextColor(sourceWidget.getTextColor())
					.setTextShadowed(sourceWidget.getTextShadowed())
					.setXTextAlignment(sourceWidget.getXTextAlignment());

			widget.setSize(sourceWidget.getWidth(), sourceWidget.getHeight())
					.setOriginalX(widget.getType() == WidgetType.GRAPHIC ? sourceWidget.getRelativeX() : sourceWidget.getOriginalX() + sourceWidget.getRelativeX())
					.setOriginalY(yOffset);

			widget.setSpriteId(sourceWidget.getSpriteId());
			widget.setHidden(widget.getType() == WidgetType.GRAPHIC && (friend.getPrevName() == null || friend.getPrevName().isEmpty()));

			String[] sourceActions = sourceWidget.getActions();
			Object[] sourceListeners = sourceWidget.getOnOpListener();
			if (sourceActions != null) {
				widget.setHasListener(sourceWidget.hasListener());

				for (int j = 0; j < sourceActions.length; j++) {
					widget.setAction(j, sourceActions[j]);
					widget.setOnOpListener(j, sourceListeners[j]);
				}
			}

			widget.revalidate();
		}

		parent.revalidate();
		return 15;
	}

	final int GROUP_SPRITE = SpriteID.OpenButtons._4;
	private boolean universeHasButton(Widget parent)
	{
		if (parent == null)
			return false;

		return Arrays.stream(parent.getDynamicChildren())
				.anyMatch(w -> w.getSpriteId() == GROUP_SPRITE);
	}

    private Widget groupButton = null;
	private void addGroupButton()
	{
		Widget friendUniverse = client.getWidget(InterfaceID.FRIENDS, 0);
		if (universeHasButton(friendUniverse)) {
			if (groupButton.isHidden())
				groupButton.setHidden(false);
			return;
		}

		groupButton = friendUniverse.createChild(-1, WidgetType.GRAPHIC);	// The parent is not null because I checked in universeHasButton

        int GROUP_BUTTON_SIZE = 17;
        groupButton.setSize(GROUP_BUTTON_SIZE, GROUP_BUTTON_SIZE);

        int GROUP_BUTTON_Y = 232;
        int GROUP_BUTTON_X = 86;
        groupButton.setForcedPosition(GROUP_BUTTON_X, GROUP_BUTTON_Y);

		groupButton.setName("Group");
		groupButton.setSpriteId(GROUP_SPRITE);

		groupButton.setAction(0, "Create");
		groupButton.setAction(1, "Export Groups");
		groupButton.setAction(2, "Import Groups");
		groupButton.setAction(3, "Clear");

		groupButton.setHasListener(true);
		groupButton.setOnOpListener((JavaScriptCallback) this::promptCreateGroup);

		groupButton.revalidate();
		friendUniverse.revalidate();
	}

	private void removeGroupButton()
	{
		Widget friendUniverse = client.getWidget(InterfaceID.FRIENDS, 0);
		if (!universeHasButton(friendUniverse))
			return;

		if (groupButton != null)
			groupButton.setHidden(true);
	}

	private void createFolder(Widget parent, String name, int yOffset, int groupSize, boolean isExpanded)
	{
		Color textColour = groupManager.getTextColour(name);
		Color backdropColour = groupManager.getBackdropColour(name);

		if (config.drawBackdrop())
			createHeaderWidget(parent, backdropColour, yOffset);

		if (config.drawBackdropBox())
			createHeaderRectangle(parent, backdropColour, yOffset, groupSize, isExpanded);

		createFolderText(parent, name, textColour, yOffset, groupSize);
	}

	private final int EXPAND = 1;
	private final int RENAME = 2;
	private final int REMOVE = 3;
	private final int COLOR_TEXT = 4;
	private final int COLOR_BACKDROP = 5;
	private final int MOVE_UP = 6;
	private final int MOVE_DOWN = 7;
	private void createFolderText(Widget parent, String name, Color textColor, int yOffset, int groupSize)
	{
		String formattedName = ColorUtil.wrapWithColorTag(name, textColor);
		String formattedText = formattedName;

		int groupCount = 0;
		switch (config.drawCount())
		{
			case TOTAL:
				groupCount = groupSize;
				break;
			case ONLINE:
				groupCount = getGroupOnline(name);
				break;
			case DISABLED:
				groupCount = -1;
				break;
		}
		if (config.drawCount() != GroupCount.DISABLED)
			formattedText = formattedText + (" (") + groupCount + ")";

		int textAlignment = config.textAlign().getAlignmentId();
		boolean isExpanded = groupManager.isExpanded(name);

		Widget folderWidget = parent.createChild(-1, WidgetType.TEXT);

		folderWidget
				.setText(formattedText)
				.setName(formattedName)
				.setFontId(config.preferredFont().getFontId());

		folderWidget
				.setTextColor(textColor.getRGB())
				.setXTextAlignment(textAlignment)
				.setTextShadowed(config.drawShadow());

		int folderWidth = parent.getWidth() - config.groupSpacing() * 2;
		int folderHeight = 15;
		folderWidget
				.setOriginalWidth(folderWidth)
				.setOriginalHeight(folderHeight)
				.setForcedPosition(config.groupSpacing(), yOffset);

		folderWidget.revalidate();
		folderWidget.setHasListener(true);

		if (groupSize > 0)
			folderWidget.setAction(EXPAND, isExpanded ? "Collapse" : "Expand");

		FriendTab group = groupManager.getTab(name);
		if (group == null)
		{
			if (groupManager.isDefaultGroup(name))
				folderWidget.setOnOpListener((JavaScriptCallback) this::onDefaultMenuClicked);

			return;
		}

		folderWidget.setAction(RENAME, "Rename");
		folderWidget.setAction(REMOVE, "Remove");
		folderWidget.setAction(COLOR_TEXT, "Color Text");

		int groupIndex = groupManager.getGroupNames().indexOf(name);
		if (groupIndex != 0)
			folderWidget.setAction(MOVE_UP, "Move-Up");

		if (groupIndex != groupManager.getGroupNames().size() - 1)
			folderWidget.setAction(MOVE_DOWN, "Move-Down");

		if (config.drawBackdrop())
			folderWidget.setAction(COLOR_BACKDROP, "Color Backdrop");

		folderWidget.setOnOpListener((JavaScriptCallback) this::onFriendMenuClicked);
	}

	private void createHeaderWidget(Widget parent, Color backdropColour, int yOffset)
	{
		Widget backdropWidget = parent.createChild(-1, WidgetType.RECTANGLE);

		backdropWidget
				.setTextColor(backdropColour.getRGB())
				.setFilled(true);

		backdropWidget
				.setOriginalHeight(getFontSpacing())
				.setOriginalWidth(parent.getWidth())
				.setForcedPosition(0, yOffset);

		backdropWidget.revalidate();
	}

	private void createHeaderRectangle(Widget parent, Color backdropColour, int yOffset, int groupSize, boolean isExpanded)
	{
		Widget backdropWidget = parent.createChild(-1, WidgetType.RECTANGLE);
		int widgetHeight = getFontSpacing() + (isExpanded ? (groupSize * 15) : 0);
		if (isExpanded && groupSize > 0)
			widgetHeight += getHeaderSpacing() - 1;

		backdropWidget
				.setTextColor(backdropColour.darker().getRGB())
				.setFilled(false);

		backdropWidget
				.setOriginalHeight(widgetHeight)
				.setOriginalWidth(parent.getWidth())
				.setForcedPosition(0, yOffset);

		backdropWidget.revalidate();
	}

	private void onDefaultMenuClicked(ScriptEvent event)
	{
		if (!formatGroup(event.getOpbase()).equals("Ungrouped"))	// This happens if you delete a group with the right-click menu open and press collapse
			return;

		groupManager.toggleExpanded("ungrouped");
		refreshFriendPanel();
	}

	private void onFriendMenuClicked(ScriptEvent event)
	{
		String groupName = formatGroup(event.getOpbase());
		FriendTab group = groupManager.getTab(groupName);
		if (group == null)
			return;

		int groupIndex = groupManager.getGroupNames().indexOf(groupName);
		switch (event.getOp() - 1)
		{
			case EXPAND:	// Maybe add a save prevention feature, savivng for each expand seems excessive
				group.toggleExpanded();
				break;

			case RENAME:
				promptRenameGroup(groupName);
				break;

			case REMOVE:
				promptDeleteGroup(groupName);
				break;

			case COLOR_TEXT:
				Color textColour = groupManager.getTextColour(groupName);
				RuneliteColorPicker textColourPicker = colorPickerManager.create(client, textColour, "Text Colour", true);

				textColourPicker.setOnClose(c -> {
					group.setCategoryColor(c.getRGB());
					groupManager.saveConfig();

					refreshFriendPanel();
				});

				textColourPicker.setVisible(true);
				break;

			case COLOR_BACKDROP:
				Color backdropColour = groupManager.getBackdropColour(groupName);
				RuneliteColorPicker backdropColourPicker = colorPickerManager.create(client, backdropColour, "Backdrop Colour", true);

				backdropColourPicker.setOnClose(c -> {
					group.setBackdropColor(c.getRGB());
					groupManager.saveConfig();

					refreshFriendPanel();
				});

				backdropColourPicker.setVisible(true);
				break;

			case MOVE_UP:
				if (groupIndex == 0)
					break;

				groupManager.swapGroupOrder(groupIndex, groupIndex-1);
				break;

			case MOVE_DOWN:
				if (groupIndex == groupManager.getGroupNames().size() - 1)
					break;

				groupManager.swapGroupOrder(groupIndex, groupIndex+1);
				break;
		}

		groupManager.saveConfig();
		refreshFriendPanel();
	}

	private void promptDeleteGroup(String groupName)
	{
		panelManager.openTextMenuInput("Are you sure you want to remove " + groupName + "?")
				.option("Yes", () -> {
					groupManager.deleteGroup(groupName);
					refreshFriendPanel();
				})
				.option("No", Runnables.doNothing())
				.build();
	}

	private void promptRenameGroup(String groupName)
	{
		panelManager.openTextInput("What would you like to rename '" + groupName + "' to?")
				.value(groupName)
				.onDone((input) -> {
					String cleanInput = formatGroup(input);

					// Not using the groupManager::hasGroup because you might want to rename uppercase / lowercase
					if (cleanInput.isBlank() || groupManager.getGroupNames().contains(cleanInput) || cleanInput.equalsIgnoreCase("ungrouped"))
					{
						return;
					}

					groupManager.renameGroup(groupName, cleanInput);
					refreshFriendPanel();
				})
				.build();
	}

	public void promptCreateGroup(ScriptEvent event)
	{
		if (groupManager.getGroupNames().size() >= 63)	// this many groups is unreasonable, and the submenu has a cap + 1 reserved for remove
			return;

		switch (event.getOp())
		{
			case 1:
				panelManager.openTextInput("What would you like to name your group?")
						.onDone((input) ->
						{
							String formattedInput = formatGroup(input);
							groupManager.createGroup(formattedInput);
							refreshFriendPanel();
						})
						.build();
				break;
			case 2:
				panelManager.openTextMenuInput("Would you like to export your friend groups?<br>This will overwrite your clipboard")
						.option("Yes", () -> {
							Toolkit.getDefaultToolkit()
									.getSystemClipboard()
									.setContents(new StringSelection(groupManager.generateSaveJson()), null);
						})
						.option("No", Runnables.doNothing())
						.build();
				break;
			case 3:
				panelManager.openTextMenuInput("Are you sure you would like to import from clipboard?<br>This will remove all current groups and they CANNOT be recovered.")
						.option("Yes", () -> {
							String clipboardJson = null;
							try
							{
								clipboardJson = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
							}
							catch (UnsupportedFlavorException e)
							{
								throw new RuntimeException(e);
							}
							catch (IOException e)
							{
								log.debug(e.getMessage());
							}

							if (clipboardJson == null)
								return; // This would nuke data

							log.info("Importing Friend Tab via Clipboard: {}", clipboardJson);

							if (groupManager.importSave(clipboardJson)) {
								groupManager.saveConfig();
								migrateNameChanges();
							}
						})
						.option("No", Runnables.doNothing())
						.build();
				break;
			case 4:
				panelManager.openTextMenuInput("Are you certain you would like to clear your data?")
								.option("Yes", () -> {
									groupManager.resetConfig();
									groupManager.loadConfig();
									refreshFriendPanel();
								})
						.option("No", Runnables.doNothing())
						.build();
				break;
		}
	}

	public void refreshFriendPanel()
	{
		clientThread.invoke(() -> {
			client.runScript(
					ScriptID.FRIENDS_UPDATE,
					InterfaceID.Friends.LIST_CONTAINER,
					InterfaceID.Friends.SORT_NAME,
					InterfaceID.Friends.SORT_RECENT,
					InterfaceID.Friends.SORT_WORLD,
					InterfaceID.Friends.SORT_LEGACY,
					InterfaceID.Friends.LIST,
					InterfaceID.Friends.SCROLLBAR,
					InterfaceID.Friends.LOADING,
					InterfaceID.Friends.TOOLTIP
			);
		});
	}

	public String formatName(String rsn)
	{
		if (rsn == null)
			return null;

		rsn = rsn.replaceAll("\\s<img=\\d+>", "");
		return Text.removeTags(rsn)
				.replace("\u00A0", " ");
	}

	public String formatGroup(String group)
	{
		return Text.removeTags(group)
				.replace("\u00A0", " ");
	}

	private int getFontSpacing()
	{
		return config.manualOverride() ? config.fontSpacing() : config.preferredFont().getFontSpacing();
	}

	private int getHeaderSpacing()
	{
		return config.manualOverride() ? config.headerSpacing() : config.preferredFont().getHeaderOffset();
	}

	@Inject private ChatMessageManager chatMessageManager;
	public void sendChatMessage(String message)
	{
		final String rlMesage = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(message)
				.build();

		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(rlMesage)
				.build());
	}

	private Friend getFriend(String friendName)
	{
		// FriendContainer has findByName, but that has the Jagex space and stuff
		return Arrays.stream(client.getFriendContainer().getMembers())
				.filter(friend -> formatName(friend.getName()).equals(friendName))
				.findFirst()
				.orElse(null);
	}

	private boolean isPlayerOnline(String friendName)
	{
		Friend friend = getFriend(friendName);
		if (friend == null)
			return false;

		return friend.getWorld() != 0;
	}

	private int getPlayerOnlineGroups(String player)
	{
		int onlineGroups = 0;

		for (String groupName : groupManager.getPlayerGroups(player))
		{
			if (getGroupOnline(groupName) > 0)
				onlineGroups++;
		}

		return onlineGroups;
	}

	private int getGroupOnline(String groupName)
	{
		int onlineCount = 0;

		List<String> groupPlayers = groupManager.getPlayers(groupName);
		for (String player : groupPlayers)
		{
			if (isPlayerOnline(player))
				onlineCount++;
		}

		return onlineCount;
	}

	@Provides
	FLOverhaulConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FLOverhaulConfig.class);
	}
}