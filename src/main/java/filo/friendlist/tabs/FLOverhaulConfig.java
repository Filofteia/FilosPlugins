package filo.friendlist.tabs;

import filo.friendlist.tabs.config.FontConfig;
import filo.friendlist.tabs.config.RemoveType;
import filo.friendlist.tabs.config.TextAlignmentConfig;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("filofriendtab")
public interface FLOverhaulConfig extends Config
{
	@ConfigSection(
			name = "Visual Settings",
			description = "The default visual settings for all your friend groups",
			position = 0
	)
	String visualSettings = "Visual Settings";
	@ConfigSection(
			name = "General Settings",
			description = "The default general settings for all your friend groups",
			position = 1
	)
	String generalSettings = "General Settings";
	@ConfigSection(
			name = "Spacing Settings",
			description = "The default manual spacing settings for all your friend groups",
			position = 1
	)
	String spacingSettings = "Spacing Settings";

	//region General Settings
	@ConfigItem(
			keyName = "removeFriendType",
			name = "Remove Friend Block",
			description = "Changes the 'Remove' option on friends to prevent accidental deletion, but only works in groups",
			position = 0,
			section = generalSettings
	)
	default RemoveType removeFriendType()
	{
		return RemoveType.DEFAULT;
	}

	@ConfigItem(
			keyName = "ungroupedStartExpanded",
			name = "Default Group Expanded",
			description = "Change whether 'Ungrouped' starts expanded or collapsed",
			position = 1,
			section = generalSettings
	)
	default boolean ungroupedStartExpanded()
	{
		return true;
	}
	//endregion
	//region Spacing Settings
	@ConfigItem(
			keyName = "manualOverride",
			name = "Manual Override",
			description = "Use manual settings rather than predefined for the font",
			position = 0,
			section = spacingSettings
	)
	default boolean manualOverride()
	{
		return false;
	}

	@ConfigItem(
			keyName = "fontSpacing",
			name = "Font Spacing",
			description = "Configures the size of the backdrop",
			position = 1,
			section = spacingSettings
	)
	default int fontSpacing()
	{
		return 15;
	}

	@ConfigItem(
			keyName = "headerSpacing",
			name = "Header Spacing",
			description = "Configures the distance between tabs",
			position = 2,
			section = spacingSettings
	)
	default int headerSpacing()
	{
		return 4;
	}
	//endregion
	//region Visual Settings
	@ConfigItem(
			keyName = "defaultColor",
			name = "Text Colour",
			description = "Default Text Colour. Only changes for new groups or 'Ungrouped'",
			position = 0,
			section = visualSettings
	)
	default Color defaultColor()
	{
		return new Color(255, 152, 31);
	}

	@ConfigItem(
			keyName = "defaultBackdrop",
			name = "Backdrop Colour",
			description = "Default Backdrop Colour. Only changes for new groups or 'Ungrouped'",
			position = 1,
			section = visualSettings
	)
	default Color defaultBackdrop()
	{
		return new Color(62, 53, 46);
	}

	@ConfigItem(
			keyName = "preferredFont",
			name = "Font",
			description = "A list of fonts for all groups to follow",
			position = 2,
			section = visualSettings
	)
	default FontConfig preferredFont()
	{
		return FontConfig.PLAIN_11;
	}

	@ConfigItem(
			keyName = "textAlign",
			name = "Text Alignment",
			description = "Preferred position of text in the tab header",
			position = 3,
			section = visualSettings
	)
	default TextAlignmentConfig textAlign()
	{
		return TextAlignmentConfig.CENTER;
	}

	@ConfigItem(
			keyName = "drawBackdrop",
			name = "Draw Backdrop",
			description = "Toggle the visibility of the backdrop",
			position = 4,
			section = visualSettings
	)
	default boolean drawBackdrop()
	{
		return true;
	}

	@ConfigItem(
			keyName = "drawBackdropBox",
			name = "Draw Group Box",
			description = "Toggle the visibility of the backdrop box",
			position = 5,
			section = visualSettings
	)
	default boolean drawBackdropBox()
	{
		return true;
	}

	@ConfigItem(
			keyName = "drawShadow",
			name = "Draw Shadows",
			description = "Toggle the visibility of the text shadow on the tab header",
			position = 6,
			section = visualSettings
	)
	default boolean drawShadow()
	{
		return true;
	}

	@ConfigItem(
			keyName = "drawCount",
			name = "Draw Group Count",
			description = "Appends the group count to the headers name",
			position = 7,
			section = visualSettings
	)
	default boolean drawCount()
	{
		return true;
	}

	@ConfigItem(
			keyName = "colourMenuEntries",
			name = "Recolour Menu Entry",
			description = "Recolours the menu entry on the 'add to section' menu",
			position = 8,
			section = visualSettings
	)
	default boolean colourMenuEntries()
	{
		return true;
	}

	@ConfigItem(
			keyName = "groupSpacing",
			name = "Name Offset",
			description = "The inset of friends names and the world number",
			position = 9,
			section = visualSettings
	)
	default int groupSpacing()
	{
		return 2;
	}
	//endregion
}