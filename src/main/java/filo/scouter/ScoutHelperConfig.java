package filo.scouter;

import filo.scouter.config.Crabs;
import filo.scouter.config.IncludeMode;
import filo.scouter.config.Layout;
import filo.scouter.config.Overload;
import filo.scouter.config.OverloadPosition;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import java.util.Collections;
import java.util.Set;

@ConfigGroup("coxscoutingqol")
public interface ScoutHelperConfig extends Config
{
	@ConfigSection(
		name = "Overload Settings",
		description = "Configure your overload preferences",
		position = 4,
		closedByDefault = true
	)
	String overloadSection = "Overload Section";

	@ConfigSection(
		name = "Room Settings",
		description = "Settings for Rooms",
		position = 3,
		closedByDefault = true
	)
	String raidSection = "Raid Section";

	@ConfigSection(
		name = "Rotation Settings",
		description = "Filter Rotations",
		position = 2,
		closedByDefault = true
	)
	String rotationSection = "Rotation Section";

	@ConfigSection(
		name = "Layout Settings",
		description = "Filter Layouts (Combat : Puzzle Ratios)",
		position = 1,
		closedByDefault = true
	)
	String layoutSection = "Layout Section";

	@ConfigSection(
		name = "General Settings",
		description = "General Settings",
		position = 0
	)
	String generalSection = "General Settings";

	@ConfigItem(
			keyName = "notifyRaid",
			name = "Notify on Raid",
			description = "Send a notification when a raid is scouted",
			section = generalSection,
			position = 0
	)
	default Notification notifyRaid()
	{
		return Notification.ON;
	}

	@ConfigItem(
			keyName = "showUpdateMessage",
			name = "Update Message",
			description = "Prints an update message upon scouting your first raid of the update",
			section = generalSection,
			position = 1
	)
	default boolean showUpdateMessage()
	{
		return true;
	}

	@ConfigItem(
		keyName = "layoutType",
		name = "Layout Filter",
		description = "3C2P (3 Combat, 2 Puzzle) <br> 4C1P (4 Combat, 1 Puzzle) <br> 4C2P (4 Combat, 2 Puzzle) <br> None (Exception List) <br><br> To reset right-click 'Layout Filter' -> 'Reset'",
		section = layoutSection,
		position = 1
	)
	default Set<Layout>layoutType()
	{
		return Collections.emptySet();
	}

	@ConfigItem(
			keyName = "layoutKeys",
			name = "Layout Exceptions",
			description = "Specific layouts that are used by 'Exception Mode'",
			section = layoutSection,
			position = 3
	)
	default String layoutKeys()
	{
		return "";
	}

	@ConfigItem(
			keyName = "layoutMode",
			name = "Layout Mode",
			description = "Sets the exception to inclusive, or exclusive.<br>Inclusive: alongside your settings<br>Exclusive: only search for these layouts",
			section = layoutSection,
			position = 2
	)
	default IncludeMode layoutMode()
	{
		return IncludeMode.INCLUSIVE;
	}

	@ConfigItem(
		keyName = "rotationEnabled",
		name = "Rotation Toggle",
		description = "Toggles rotations and only searches for the specified rotations.",
		section = rotationSection,
		position = 0
	)
	default boolean rotationEnabled()
	{
		return false;
	}

	@ConfigItem(
			keyName = "rotationList",
			name = "Rotations",
			description = "Specify a rotation, and search based on the 'Rotation Mode' above.<br><br>Examples:<br>muttadiles,shamans,mystics<br>muttadiles,tightrope,shamans,mystics,ice demon<br>Separate by new-line!",
			section = rotationSection,
			position = 1
	)
	default String rotationList()
	{
		return "";
	}

	@ConfigItem(
		keyName = "blockedRooms",
		name = "Blocked Rooms",
		description = "Enter the name of rooms you want to filter out<br><br>Example:<br>ice demon,vanguards,muttadiles",
		section = raidSection,
		position = 0
	)
	default String blockedRooms()
	{
		return "";
	}

	@ConfigItem(
		keyName = "blockedUnknownCombat",
		name = "Block Unknown Combat",
		description = "Block unknown combat rooms",
		section = raidSection,
		position = 1
	)
	default boolean blockedUnknownCombat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "blockedUnknownPuzzles",
		name = "Block Unknown Puzzles",
		description = "Block unknown puzzle rooms",
		section = raidSection,
		position = 2
	)
	default boolean blockedUnknownPuzzles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "preferredCrabs",
		name = "Preferred Crabs",
		description = "Choose your preferred crab type:<br><br>Any: any crab rotation is good<br>Rare: only Rare or Good crabs<br>Good: Good crabs only",
		section = raidSection,
		position = 3
	)
	default Crabs preferredCrabs()
	{
		return Crabs.ANY;
	}

	@ConfigItem(
		keyName = "overloadRooms",
		name = "Overload Filter",
		description = "A list of overload rooms you can filter<br>To select multiple use Ctrl-Click or Shift-Click <br>If you want none required Right-Click 'Overload Filter' -> 'Reset'",
		section = overloadSection,
		position = 0
	)
	default Set<Overload> overloadRooms()
	{
		return Collections.emptySet();
	}

	@ConfigItem(
		keyName = "ovlPos",
		name = "Preferred Location",
		description = "Define a preferred location for the Overload to be found",
		section = overloadSection,
		position = 1
	)
	default OverloadPosition ovlPos()
	{
		return OverloadPosition.ANY_ROOM;
	}

	@ConfigItem(
			keyName = "incPuzzleCombat",
			name = "Include Puzzle Combat",
			description = "Includes both Ice Demon and Tightrope in the 'First Combat' check.",
			section = overloadSection,
			position = 2
	)
	default boolean incPuzzleCombat()
	{
		return false;
	}

	@ConfigItem(
			keyName = "configVer",
			name = "configVersion",
			description = "The current config version",
			position = 1000,
			hidden = true
	)
	default int configVer()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "lastUpdateMessageVer",
			name = "messageVersion",
			description = "The config version of the previous update message",
			position = 1001,
			hidden = true
	)
	default int lastUpdateMessageVer()
	{
		return 0;
	}
}
