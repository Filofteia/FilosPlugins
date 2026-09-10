package filo.cm.checklist;

import java.awt.Color;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.ui.ColorScheme;

@ConfigGroup("cmchecklist")
public interface CMChecklistConfig extends Config
{
	@ConfigSection(
			name="General Settings",
			description = "General settings unrelated to the interface",
			position = 0
	)
	String generalSettings = "Generral Settings";

	@ConfigSection(
			name="Highlight Settings",
			description = "Colours to edit / enable",
			position = 1
	)
	String highlightSettings = "Highlight Settings";

	@ConfigSection(
			name="ZigZag Settings",
			description = "The settings of colours",
			position = 2
	)
	String zigzagSettings = "ZigZag Settings";

	@ConfigSection(
			name="Panel Settings",
			description = "The settings for the sidepane",
			position = 1
	)
	String panelSettings = "Panel Settings";

	//region ZigZag
	@ConfigItem(
			keyName = "zigzag",
			name = "Enable ZigZag",
			description = "Arranges your items in a zigzag pattern in private storage",
			section = zigzagSettings,
			position = 0
	)
	default boolean zigzag()
	{
		return true;
	}

	@ConfigItem(
			keyName = "zigzagEquipment",
			name = "ZigZag Equipment",
			description = "Includes your equipment in the zigzag layout",
			section = zigzagSettings,
			position = 1
	)
	default boolean zigzagEquipment()
	{
		return true;
	}

	@ConfigItem(
			keyName = "zigzagInventory",
			name = "ZigZag Inventory",
			description = "Includes your items in the zigzag layout",
			section = zigzagSettings,
			position = 2
	)
	default boolean zigzagInventory()
	{
		return true;
	}

	@ConfigItem(
			keyName = "compactZigzag",
			name = "Compact ZigZag",
			description = "Display nine items per line rather than eight",
			section = zigzagSettings,
			position = 3
	)
	default boolean compactZigzag()
	{
		return false;
	}

	@ConfigItem(
			keyName = "standardPots",
			name = "Standardise Potions",
			description = "Makes potions use alternatives if possible",
			section = zigzagSettings,
			position = 4
	)
	default boolean standardPots()
	{
		return false;
	}
	//endregion
	//region Highlights
	@ConfigItem(
			keyName = "highlightMismatched",
			name = "Highlight Mismatched Items",
			description = "Places a box around items that do not match your setup",
			section = highlightSettings,
			position = 0
	)
	default boolean highlightMismatched()
	{
		return true;
	}

	@ConfigItem(
			keyName = "highlightMismatchedColour",
			name = "Mismatched Colour",
			description = "Colour for the Mismatched Items",
			section = highlightSettings,
			position = 1
	)
	default Color highlightMismatchedColour()
	{
		return new Color(104, 58, 58);
	}

	@ConfigItem(
			keyName = "highlightEquipment",
			name = "Highlight Equipment",
			description = "Highlights equipment from your preset that you haven't equipped",
			section = highlightSettings,
			position = 2
	)
	default boolean highlightEquipment()
	{
		return true;
	}
	@ConfigItem(
			keyName = "equipmentColour",
			name = "Equipment Colour",
			description = "Colour for your equipment highlight",
			section = highlightSettings,
			position = 3
	)
	default Color equipmentColour()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
			keyName = "highlightInventory",
			name = "Highlight Inventory",
			description = "Highlights items from your preset that are in your private storage",
			section = highlightSettings,
			position = 4
	)
	default boolean highlightInventory()
	{
		return true;
	}
	@ConfigItem(
			keyName = "inventoryColour",
			name = "Inventory Colour",
			description = "Colour for your inventory highlight",
			section = highlightSettings,
			position = 5
	)
	default Color inventoryColour()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
			keyName = "highlightWithdraw",
			name = "Highlight Withdraws",
			description = "Highlights items that you have marked for withdraw in your setup",
			section = highlightSettings,
			position = 6
	)
	default boolean highlightWithdraw()
	{
		return true;
	}
	@ConfigItem(
			keyName = "withdrawColour",
			name = "Withdraw Colour",
			description = "Colour for your withdraw highlight",
			section = highlightSettings,
			position = 7
	)
	default Color withdrawColour()
	{
		return Color.GREEN;
	}
	@ConfigItem(
			keyName = "highlightDeposit",
			name = "Highlight Deposit",
			description = "Highlights items that you have marked for deposit in your setup",
			section = highlightSettings,
			position = 8
	)
	default boolean highlightDeposit()
	{
		return true;
	}
	@ConfigItem(
			keyName = "depositColour",
			name = "Deposit Colour",
			description = "Colour for your deposit highlight",
			section = highlightSettings,
			position = 9
	)
	default Color depositColour()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "ghostOutline",
			name = "Ghost Outline",
			description = "Toggles outlines on ghost items in your private storage",
			section = highlightSettings,
			position = 10
	)
	default boolean ghostOutline()
	{
		return true;
	}
	@Alpha
	@ConfigItem(
			keyName = "ghostColour",
			name = "Ghost Colour",
			description = "Colour for your ghost highlight",
			section = highlightSettings,
			position = 11
	)
	default Color ghostColour()
	{
		return new Color(255, 255, 255 ,20);
	}
	//endregion
	//region Panel
	@ConfigItem(
			keyName = "autoUpdateRoom",
			name = "Auto Load Rooms",
			description = "Automatically displays the correct side-panel when entering rooms",
			section = panelSettings,
			position = 0
	)
	default boolean autoUpdateRoom()
	{
		return true;
	}

	@ConfigItem(
			keyName = "entryRoomColour",
			name = "Entry Room Colour",
			description = "Colour for the Entry Room in the side-panel",
			section = panelSettings,
			position = 1
	)
	default Color entryRoomColour()
	{
		return ColorScheme.GRAND_EXCHANGE_LIMIT;
	}

	@ConfigItem(
			keyName = "endRoomColour",
			name = "End Room Colour",
			description = "Colour for the End Room in the side-panel",
			section = panelSettings,
			position = 2
	)
	default Color endRoomColour()
	{
		return ColorScheme.PROGRESS_ERROR_COLOR;
	}

	@ConfigItem(
			keyName = "storageRoomColour",
			name = "Storage Room Colour",
			description = "Colour for storage rooms in the side-panel",
			section = panelSettings,
			position = 4
	)
	default Color storageRoomColour()
	{
		return ColorScheme.BRAND_ORANGE;
	}
	//endregion
	@ConfigItem(
			keyName = "chestMenuOptions",
			name = "Storage Options",
			description = "Adds the 'Save Setup' and 'Clear Setup' to every storage chest in the raid",
			position = 0,
			section = generalSettings
	)
	default boolean chestMenuOptions()
	{
		return false;
	}
}
