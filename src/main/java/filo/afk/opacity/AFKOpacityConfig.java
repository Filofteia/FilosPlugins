package filo.afk.opacity;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import java.awt.event.KeyEvent;

@ConfigGroup("filoafkhider")
public interface AFKOpacityConfig extends Config
{
	//region Sections
	@ConfigSection(
			name = "Help",
			description = "Help inside. If you accidentally delete you can right-click reset the 'help' label.",
			position = 0,
			closedByDefault = true
	)
	String helpSettings = "Help";

	@ConfigSection(
			name = "Main",
			description = "Main settings for AFK Hider",
			position = 1
	)
	String generalSettings = "Main Settings";

	@ConfigSection(
			name = "Restore Settings",
			description = "Change what can restore the client",
			position = 2
	)
	String restoreSettings = "Restore Settings";

	@ConfigSection(
			name = "Input Settings",
			description = "Change how AFK mode changes inputs",
			position = 3
	)
	String inputSettings = "Input Settings";

	@ConfigSection(
			name = "Debug",
			description = "Simple debug settings",
			position = 4,
			closedByDefault = true
	)
	String debugSettings = "Debug Settings";
	//endregion

	//region help
	@ConfigItem(
			keyName = "help",
			name = "Help:",
			description = "Options that activate AFK mode. One per line, format is 'option:target'.",
			section = helpSettings,
			position = 0
	)
	default String help()
	{
		return "### Help ###\n"
				+ "This plugin will change the opacity of the client when you click an action specified in 'Triggers'\n\n"
				+ "Formatted as: 'option:target'. It is not case-sensitive and one per line\n\n"
				+ "Examples:\n"
				+ "mine:crashed star\n"
				+ "chop down:ironwood tree\n\n"
				+ "You can set the opacity to 0 to make the client invisible while AFK mode is active.\n\n"
				+ "If you wish to restore the client you must use one of the following:\n"
				+ "1. Restore Keybind - Only works when RuneLite is focused\n"
				+ "2. Restore on Notification - When RuneLite notifies you\n"
				+ "3. Restore on Hitsplat - When your player takes a hitsplat\n"
				+ "4. Restore on Focus Loss - When you tab-out if enabled.\n"
				+ "5. Idle Restore Delay - Delay in minutes before the client will restore itself\n\n"
				+ "Be careful using this plugin in dangerous locations";
	}
	//endregion

	//region General
	@ConfigItem(
			keyName = "interaction",
			name = "Triggers",
			description = "Options that activate AFK mode. One per line, format is 'option:target'.",
			section = generalSettings,
			position = 0
	)
	default String interaction()
	{
		return "";
	}

	@Range(min = 1, max = 100)
	@ConfigItem(
			keyName = "afkOpacity",
			name = "Afk Opacity",
			description = "Client opacity while AFK mode is active.",
			section = generalSettings,
			position = 1
	)
	default int afkOpacity()
	{
		return 20;
	}

	@Range(min = 1, max = 100)
	@ConfigItem(
			keyName = "afkTabbedOpacity",
			name = "Tabbed Out Opacity",
			description = "Client opacity while AFK mode is active and the game is not focused.",
			section = generalSettings,
			position = 2
	)
	default int afkTabbedOpacity()
	{
		return 50;
	}


	@ConfigItem(
			keyName = "escapeKeybind",
			name = "Restore Keybind",
			description = "Keybind used to restore from AFK mode",
			section = generalSettings,
			position = 3
	)
	default Keybind escapeKeybind()
	{
		return new Keybind(KeyEvent.VK_ESCAPE, 0);
	}
	//endregion
	//region Restore
	@ConfigItem(
			keyName = "restoreOnNotify",
			name = "Restore on Notification",
			description = "Restore the client when a RuneLite notification happens",
			section = restoreSettings,
			position = 0
	)
	default boolean restoreOnNotify()
	{
		return true;
	}

	@ConfigItem(
			keyName = "restoreOnHitsplat",
			name = "Restore on Hitsplat",
			description = "Restore the client when your player receives a hitsplat",
			section = restoreSettings,
			position = 1
	)
	default boolean restoreOnHitsplat()
	{
		return true;
	}

	@ConfigItem(
			keyName = "restoreOnFocusLoss",
			name = "Restore on Focus Loss",
			description = "Restore the client when the game loses focus",
			section = restoreSettings,
			position = 2
	)
	default boolean restoreOnFocusLoss()
	{
		return false;
	}

	@Range(min = 0, max = 30)
	@ConfigItem(
			keyName = "idleRestore",
			name = "Idle Restore Delay",
			description = "Restore the client after this many minutes in AFK mode. Set to 0 to disable.",
			section = restoreSettings,
			position = 3
	)
	@Units(Units.MINUTES)
	default int idleRestore()
	{
		return 0;
	}
	//endregion
	//region Input
	@ConfigItem(
			keyName = "preventClickInput",
			name = "Block Cursor Input",
			description = "Block all mouse input while AFK mode is active.",
			section = inputSettings,
			position = 0
	)
	default boolean preventClickInput()
	{
		return true;
	}

	@ConfigItem(
			keyName = "preventKeyInput",
			name = "Block Keyboard Input",
			description = "Block all keyboard input while AFK mode is active. The restore key still works.",
			section = inputSettings,
			position = 1
	)
	default boolean preventKeyInput()
	{
		return true;
	}

	@ConfigItem(
			keyName = "preventFullInventory",
			name = "Require Inventory Space",
			description = "Prevent entering AFK mode if your inventory is full",
			section = inputSettings,
			position = 2

	)
	default boolean preventFullInventory()
	{
		return true;
	}
	//endregion
	//region debug
	@ConfigItem(
			keyName = "logActions",
			name = "Log Actions to Chat",
			description = "Print clicked options to the chat to help configure Triggers.",
			section = debugSettings,
			position = 0
	)
	default boolean logActions()
	{
		return false;
	}

	@ConfigItem(
			keyName = "printReason",
			name = "Print Restore Reason",
			description = "Print the reason the client restored to the chat",
			section = debugSettings,
			position = 1
	)
	default boolean printReason()
	{
		return true;
	}
	//endregion
}
