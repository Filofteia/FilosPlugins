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

package filo.afk.opacity;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
	name = "AFK Hider",
		description = "Hides the client upon clicking specified menu options"
)
public class AFKOpacityPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private AFKOpacityConfig config;
	@Inject
	private ChatMessageManager chatMessageManager;

	private int idleRestoreTick = -1;
	private boolean afk = false;
	private boolean pendingRestore = false;
	private boolean usingTabbedOpacity = false;
	private Map<String, List<String>> interactions = new HashMap<>();

	// RuneLite Key Listener didn't work because of things like 'Press Enter to Chat...' was bugged
	private final AWTEventListener awtKeyListener = awtEvent ->
	{
		if (!afk || pendingRestore)
			return;

		if (awtEvent instanceof KeyEvent)
		{
			KeyEvent keyEvent = (KeyEvent) awtEvent;
			if (keyEvent.getID() == KeyEvent.KEY_PRESSED && keyEvent.getKeyCode() == config.escapeKeybind().getKeyCode())
			{
				keyEvent.consume();
				escapeAfkMode("due to restore key");
				return;
			}

			if (!config.preventKeyInput())
				return;

			if (keyEvent.getID() != KeyEvent.KEY_PRESSED
					&& keyEvent.getID() != KeyEvent.KEY_TYPED)	// Release is fine and prevents camera key stuck thing
			{
				return;
			}

			keyEvent.consume();
		}
	};

	private final AWTEventListener awtMouseListener = awtEvent ->
	{
		if (!afk || !config.preventClickInput() || pendingRestore)
			return;

		if (awtEvent instanceof MouseWheelEvent)
		{
			MouseWheelEvent mouseWheelEvent = (MouseWheelEvent) awtEvent;
			mouseWheelEvent.consume();
			return;
		}

		if (awtEvent instanceof MouseEvent)
		{
			MouseEvent mouseEvent = (MouseEvent) awtEvent;

			if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED
			|| mouseEvent.getID() == MouseEvent.MOUSE_PRESSED
			|| mouseEvent.getID() == MouseEvent.MOUSE_RELEASED
			)
			{
				mouseEvent.consume();
			}
		}
	};

	@Override
	protected void startUp() throws Exception
	{
		interactions = parseConfig();
		Toolkit.getDefaultToolkit().addAWTEventListener(awtKeyListener, AWTEvent.KEY_EVENT_MASK);
		Toolkit.getDefaultToolkit().addAWTEventListener(awtMouseListener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}

	@Override
	protected void shutDown() throws Exception
	{
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtKeyListener);
		Toolkit.getDefaultToolkit().removeAWTEventListener(awtMouseListener);
		escapeAfkMode(null);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("filoafkhider"))
			return;

		if (event.getKey().equals("interaction")) {
			interactions = parseConfig();
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (pendingRestore) {
			escapeAfkMode(null);
			return;
		}

		if (!afk)
			return;

		if (config.idleRestore() > 0
				&& idleRestoreTick != -1
				&& client.getTickCount() >= idleRestoreTick)
		{
			escapeAfkMode("due to Idle Timeout config");
			return;
		}

		if (!isClientFocused())
		{
			if (config.restoreOnFocusLoss())
			{
				escapeAfkMode("due to focus loss");
				return;
			}

			if (!usingTabbedOpacity)
			{
				updateAfkOpacity(config.afkTabbedOpacity() / 100f);
				usingTabbedOpacity = true;
			}

			return;
		}

		if (usingTabbedOpacity)
		{
			updateAfkOpacity(config.afkOpacity() / 100f);
			usingTabbedOpacity = false;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		String eventOption = event.getMenuOption();
		String eventTarget = event.getMenuTarget();

		if (eventOption == null || eventTarget == null || eventOption.isEmpty() || eventTarget.isEmpty())
			return;

		String option = Text.removeTags(eventOption);
		String target = Text.removeTags(eventTarget);

		if (option.isEmpty() || target.isEmpty())
			return;

		if (config.logActions()) {
			addChatMessage("AFK Hider Action: " + option + ":" + target);
		}

		// Like 100% sure AWT mouse thing prevents this based on testing, but why not be safe?
		if (afk && config.preventClickInput() && !pendingRestore)
		{
			event.consume();
			return;
		}

		String optionStandardized = option.toLowerCase();
		String targetStandardized = target.toLowerCase();

		if (interactions.containsKey(optionStandardized)
				&& interactions.get(optionStandardized).contains(targetStandardized))
		{
				enterAfkMode();
		}
	}

	private Map<String, List<String>> parseConfig() {
		Map<String, List<String>> tmpInteractions = new HashMap<>();
		String interactionConfig = config.interaction();

		for (String line : interactionConfig.split("\n")) {
			line = line.trim().toLowerCase();
			if (line.isEmpty()) {
				continue;
			}

			String[] lineParts = line.split(":", 2);
			if (lineParts.length != 2) {
				continue;
			}

			String action = lineParts[0].trim();
			String target = lineParts[1].trim();

			tmpInteractions.putIfAbsent(action, new ArrayList<>());
			tmpInteractions.get(action).add(target);
		}

		return tmpInteractions;
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!afk)
			return;

		if (event.getActor() instanceof Player)
		{
			Player targetPlayer = (Player) event.getActor();
			if (targetPlayer == client.getLocalPlayer())
			{
				if (config.restoreOnHitsplat()) {
					escapeAfkMode("due to a hitsplat");
				}
			}
		}
	}

	@Subscribe
	public void onNotificationFired(NotificationFired e)
	{
		if (config.restoreOnNotify()) {
			escapeAfkMode("due to notification");
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGIN_SCREEN) {
			escapeAfkMode(null);
		}
	}

	private boolean isClientFocused()
	{
		Frame frame = getClientFrame();
		Canvas canvas = client.getCanvas();

		if (frame == null || canvas == null)
			return false;

		return (frame.isFocused() || frame.isActive()) || canvas.hasFocus();
	}

	private Frame getClientFrame()
	{
		Window clientWindow = SwingUtilities.getWindowAncestor(client.getCanvas());

		if (clientWindow instanceof Frame)
			return (Frame) clientWindow;

		return null;
	}

	private void addChatMessage(String message)
	{
		String chatMessage = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append(message)
				.build();

		chatMessageManager.queue(QueuedMessage
				.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMessage)
				.build()
		);
	}

	private boolean checkInventorySpace()
	{
		if (!config.preventFullInventory())
			return true;

		ItemContainer invContainer = client.getItemContainer(InventoryID.INV);
        return invContainer != null && invContainer.count() < 28;
    }

	@Provides
	AFKOpacityConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(AFKOpacityConfig.class);
	}

	private boolean setFrameOpacity(float opacity)
	{
		Frame frame = getClientFrame();
		if (frame == null)
			return false;

		try
		{
			frame.setOpacity(opacity);
			return Math.abs(frame.getOpacity() - opacity) < 0.001f; // Just being safe here but realistically could just use ==
		}
		catch (RuntimeException e)
		{
			return false;
		}
	}

	private void enterAfkMode()
	{
		if (!checkInventorySpace())
			return;

		float opacity = config.afkOpacity() / 100f;
		if (!setFrameOpacity(opacity))
		{
			addChatMessage("AFK Hider: Failed to enter AFK mode.");
			return;
		}

		afk = true;
		pendingRestore = false;
		usingTabbedOpacity = false;

		if (config.idleRestore() > 0) {
			idleRestoreTick = client.getTickCount() + (config.idleRestore() * 100);
		}
	}

	private void updateAfkOpacity(float opacity)
	{
		if (!afk)
			return;

		setFrameOpacity(opacity);
	}

	private void escapeAfkMode(String reason)
	{
		if (!afk)
			return;

		if (!setFrameOpacity(1f))
		{
			if (!pendingRestore) {
				addChatMessage("AFK Hider: Input restored. Still trying to restore window opacity.");
			}

			pendingRestore = true;
			return;
		}

		afk = false;
		usingTabbedOpacity = false;
		pendingRestore = false;
		idleRestoreTick = -1;

		if (config.printReason() && reason != null) {
			addChatMessage("AFK Hider: Reason: " + reason);
		}
	}
}