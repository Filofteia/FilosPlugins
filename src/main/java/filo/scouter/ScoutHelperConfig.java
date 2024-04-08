package filo.scouter;

import filo.scouter.config.Layout;
import filo.scouter.config.Overload;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.util.Collections;
import java.util.Set;

@ConfigGroup("coxscoutingqol")
public interface ScoutHelperConfig extends Config
{
	@ConfigSection(
		name = "Overload Settings",
		description = "Select your Overload Rooms (Ctrl and Shift)",
		position = 3,
		closedByDefault = true
	)
	String overloadSection = "Overload Section";

	@ConfigSection(
		name = "Raid Settings",
		description = "Settings for common raid options",
		position = 2,
		closedByDefault = true
	)
	String raidSection = "Raid Section";

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
		section = generalSection
	)
	default boolean notifyRaid()
	{
		return true;
	}

	@ConfigItem(
		keyName = "layoutType",
		name = "Layout Filter",
		description = "Filter Layout Types: <br><br> 3C2P (3 Combat, 2 Puzzle) <br> 4C1P (4 Combat, 1 Puzzle) <br> 4C2P (4 Combat, 2 Puzzle) <br> None (Exception List) <br><br> To reset right-click 'Layout Filter' -> 'Reset'",
		section = layoutSection,
		position = 1
	)
	default Set<Layout> layoutType()
	{
		return Collections.emptySet();
	}

	@ConfigItem(
		keyName = "layoutKeys",
		name = "Exception Layouts",
		description = "Allows certain layouts to bypass the filter <br> Separate by ',' and spaces are allowed",
		section = layoutSection,
		position = 2
	)
	default String layoutKeys()
	{
		return "";
	}

	@ConfigItem(
		keyName = "rotationEnabled",
		name = "Rotation Toggle",
		description = "Toggles 'Rotations'",
		section = raidSection,
		position = 0
	)
	default boolean rotationEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "rotationList",
		name = "Rotations",
		description = "Allows you to require a set rotation: <br><br>Vasa,Shamans,Vespula<br>Vasa,Tekton,Vespula<br><br>for example",
		section = raidSection,
		position = 1
	)
	default String rotationList()
	{
		return "";
	}

	@ConfigItem(
		keyName = "blockedRooms",
		name = "Blocked Rooms",
		description = "Block specific rooms (Combat or Puzzle) <br><br>Ice Demon,Vanguards<br><br>Separate by Comma ','",
		section = raidSection,
		position = 2
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
		position = 3
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
		position = 4
	)
	default boolean blockedUnknownPuzzles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "overloadRooms",
		name = "Overload Filter",
		description = "A list of overload rooms you can filter <br><br>To select multiple use Ctrl-Click or Shift-Click <br><br>If you want none required Right-Click 'Overload Filter' -> 'Reset'",
		section = overloadSection
	)
	default Set<Overload> overloadRooms()
	{
		return Collections.emptySet();
	}
}
