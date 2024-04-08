/*
 * Copyright (c) 2018, Kamiel
 * Copyright (c) 2024, Filofteia <https://github.com/Filofteia>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package filo.scouter;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.client.Notifier;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import filo.scouter.config.Layout;
import filo.scouter.config.Overload;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.raids.Raid;
import net.runelite.client.plugins.raids.RaidRoom;
import net.runelite.client.plugins.raids.RoomType;
import net.runelite.client.plugins.raids.events.RaidReset;
import net.runelite.client.plugins.raids.events.RaidScouted;
import net.runelite.api.events.MenuEntryAdded;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.runelite.client.plugins.raids.solver.Room;

@Slf4j
@PluginDescriptor(
	name = "CoX Scouting QoL",
	description = "Allows you to choose a define a good raid and removes the reload option until found",
	tags = {"CoX", "Scouting", "QoL", "Chamber", "Chambers of Xeric", "Raid", "Raids", "Raids 1"}
)
public class ScoutHelperPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ScoutHelperConfig config;
	@Inject
	private Notifier notifier;

	private boolean raidFound = false;
	private boolean raidSearched = false; // If the current raid was searched prevents double alert

	@Override
	protected void startUp() throws Exception
	{
		raidFound = false;
		raidSearched = false;
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		int CoX_ENTRY_ID = 49999;

		if (e.getIdentifier() != CoX_ENTRY_ID || isRaidStarted())
		{
			return;
		}

		// Deprioritize 'Climb' for left click Reload
		if (e.getOption().equals("Climb"))
		{
			e.getMenuEntry().setDeprioritized(true);
		}

		// Deprioritize 'Reload' on accepted raids
		if (e.getOption().equals("Reload") && raidFound)
		{
			e.getMenuEntry().setDeprioritized(true);
		}
	}

	/**
	 * Code based on Raid.java getOrderedRooms()
	 *
	 * @param raid      The raid to search
	 * @param roomTypes The rooms to return
	 * @return A List of RaidRooms from the raid which have the type specified
	 */
	private List<RaidRoom> getOrderedRooms(Raid raid, RoomType... roomTypes)
	{
		List<RaidRoom> roomList = new ArrayList<>();

		for (Room r : raid.getLayout().getRooms())
		{
			final int position = r.getPosition();
			final RaidRoom room = raid.getRoom(position);

			if (room == null)
			{
				continue;
			}

			for (RoomType roomType : roomTypes)
			{
				if (roomType == room.getType())
				{
					roomList.add(room);
				}
			}
		}

		return roomList;
	}

	/**
	 * @param raid
	 * @return String: Rotation of the Raid
	 */
	private String getRaidRotation(Raid raid)
	{
		StringBuilder rotation = new StringBuilder();

		for (RaidRoom room : getOrderedRooms(raid, RoomType.COMBAT))
		{
			rotation.append(room.getName()).append(",");
		}

		return rotation.substring(0, rotation.length() - 1);
	}

	/**
	 * @return String[]: Rotations from Config
	 */
	private String[] getConfigRotations()
	{
		String rotationConfig = config.rotationList();

		if (rotationConfig.isBlank())
		{
			return null;
		}

		return rotationConfig.split("\n");
	}

	@Subscribe
	public void onRaidScouted(RaidScouted raidScouted)
	{
		Raid raid = raidScouted.getRaid();

		if (raid == null || raidSearched || isRaidStarted())
		{
			return;    // Prevent Double Alert
		}

		raidSearched = true;

		Set<Overload> overloadSet = config.overloadRooms();
		String[] blockedRooms = config.blockedRooms().split(",");

		boolean overloadFound = overloadSet.isEmpty();    // Skips the check if empty
		boolean rotationFound = !config.rotationEnabled();    // Skips the check if not enabled
		boolean layoutFound = false;

		List<RaidRoom> combatRooms = getOrderedRooms(raid, RoomType.COMBAT);
		List<RaidRoom> puzzleRooms = getOrderedRooms(raid, RoomType.PUZZLE);

		// Combat Room Flags
		for (RaidRoom room : combatRooms)
		{
			String roomName = room.getName();

			// Unknown Flag
			if (roomName.equalsIgnoreCase("unknown (combat)") && config.blockedUnknownCombat())
			{
				return;
			}

			// Blocked Room
			for (String blockedRoom : blockedRooms)
			{
				if (roomName.equalsIgnoreCase(blockedRoom))
				{
					return;
				}
			}

			// Overload Flag
			if (!overloadFound)
			{
				for (Overload overload : overloadSet)
				{
					String overloadRoomName = overload.getRoomName();
					if (roomName.equalsIgnoreCase(overloadRoomName))
					{
						overloadFound = true;
						break;
					}
				}
			}
		}

		// Puzzle Room Flags
		for (RaidRoom room : puzzleRooms)
		{
			String roomName = room.getName();

			// Unknown Flag
			if (roomName.equalsIgnoreCase("unknown (puzzle)") && config.blockedUnknownPuzzles())
			{
				return;
			}

			// Blocked Room (Return if any)
			for (String blockedRoom : blockedRooms)
			{
				if (roomName.equalsIgnoreCase(blockedRoom))
				{
					return;
				}
			}
		}

		// Rotation Flag
		if (!rotationFound)
		{
			String[] configRotations = getConfigRotations();
			String raidRotation = getRaidRotation(raid);

			if (configRotations != null)
			{
				for (String rotation : configRotations)
				{
					if (raidRotation.equalsIgnoreCase(rotation))
					{
						rotationFound = true;
						break;
					}
				}
			}
			else    // Prevent an empty rotation from detecting any raid
			{
				rotationFound = true;
			}
		}

		// Layout Flag
		for (Layout layout : config.layoutType())
		{
			if (layout.getMaxCombat() == combatRooms.size() && layout.getMaxPuzzles() == puzzleRooms.size())
			{
				layoutFound = true;
				break;
			}
		}

		// Layout Specific Flag
		String layoutKeys = config.layoutKeys().replace(" ", "");
		for (String layout : layoutKeys.split(","))
		{
			if (raid.getLayout().toCodeString().equalsIgnoreCase(layout))
			{
				layoutFound = true;
				break;
			}
		}

		// All Flags
		if (layoutFound && overloadFound && rotationFound)
		{
			raidFound = true;
			notifier.notify(String.format("Raid Found! (%s)", getRaidRotation(raid)));
		}
	}

	@Subscribe
	public void onRaidReset(RaidReset raidReset)
	{
		raidFound = false;
		raidSearched = false;
	}

	private boolean isInRaid()	// Unused Currently
	{
		return client.getVarbitValue(Varbits.IN_RAID) == 1;
	}

	private boolean isRaidStarted()
	{
		return client.getVarbitValue(Varbits.RAID_STATE) == 1;
	}

	@Provides
	ScoutHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ScoutHelperConfig.class);
	}
}