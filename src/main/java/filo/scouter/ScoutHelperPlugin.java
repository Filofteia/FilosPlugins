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
import filo.scouter.config.Crabs;
import filo.scouter.config.OverloadPosition;
import filo.scouter.data.PuzzleLayout;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarbitChanged;
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
import net.runelite.client.util.Text;

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

	private boolean isStarted = false;
	private boolean isChallengeMode = false;

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

		if (e.getIdentifier() != CoX_ENTRY_ID)
		{
			return;
		}

		if (isStarted)
		{
			return;
		}

		if (isChallengeMode)
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
	 * @return List<String>: Rotations from Config
	 */
	private List<String> getConfigRotations()
	{
		List<String> rotations = new ArrayList<>();

		if (config.rotationList().isBlank())
			return rotations;

		for (String line : config.rotationList().split("\\n"))
			rotations.add(line.replaceAll("(\\s*,\\s*)", ","));    // Spaces before or after comma allowed

		return rotations;
	}

	private boolean verifyCrabs(String raidLayout, List<RaidRoom> puzzles)
	{
		if (config.preferredCrabs() == Crabs.ANY)
			return true;    // Always good
		if (!puzzles.contains(RaidRoom.CRABS))
			return true;	// Default because you can block other puzzles

		int crabIndex = puzzles.indexOf(RaidRoom.CRABS);

		PuzzleLayout puzzleLayout = PuzzleLayout.getByLayout(raidLayout);
		if (puzzleLayout == null)
			return false;	// This is not a layout my plugin supports so return false

		String crabType = puzzleLayout.getPuzzleType(crabIndex);
		switch (crabType)
		{
			case "N/a": // Only occurs when you are out of index or no crabs exist, shouldn't occur but will stay in line of defaulting true
			case "C":
				return true;
			case "A":
				return false;
			case "B":
				return config.preferredCrabs() == Crabs.RARE;
		}

		return false;
	}

	@Subscribe
	public void onRaidScouted(RaidScouted raidScouted)
	{
		if (isChallengeMode)
		{
			raidSearched = true;
			return;
		}

		Raid raid = raidScouted.getRaid();
		if (raid == null || raidSearched || isStarted)
		{
			return; // Prevent Double Alert
		}

		String raidLayoutCode = raid.getLayout().toCodeString();
		raidSearched = true;

		List<RaidRoom> allRooms = getOrderedRooms(raid, RoomType.COMBAT, RoomType.PUZZLE);
		List<RaidRoom> combatRooms = allRooms.stream().filter(raidRoom -> raidRoom.getType() == RoomType.COMBAT).collect(Collectors.toList());
		List<RaidRoom> puzzleRooms = allRooms.stream().filter(raidRoom -> raidRoom.getType() == RoomType.PUZZLE).collect(Collectors.toList());
		Layout raidLayout = Layout.findLayout(combatRooms.size(), puzzleRooms.size());

		Set<Layout> layoutFilter = config.layoutType();;
		Set<Overload> overloadFilter = config.overloadRooms();
		List<String> roomFilter = Text.fromCSV(config.blockedRooms());

		boolean crabPuzzleFlag = verifyCrabs(raidLayoutCode, puzzleRooms);
		boolean layoutFound = layoutFilter.stream().anyMatch(layout -> raidLayout == layout);	// If current match fits the filter list if not we'll check exception later
		boolean overloadFound = overloadFilter.isEmpty();    	// Skips the check if empty
		boolean rotationFound = !config.rotationEnabled();		// Skips the check if not enabled

		// overloadFound means selected none so instantly know the result
		if (config.ovlPos() == OverloadPosition.COMBAT_FIRST && !overloadFound)
		{
			RaidRoom firstTrueCombat = allRooms.stream()
				.filter(r ->
					!r.getName().equalsIgnoreCase("thieving") &&
						!r.getName().equalsIgnoreCase("crabs")
				)
				.findFirst().orElse(null);

			if (firstTrueCombat == null || firstTrueCombat.getName().isEmpty())
			{
				return; // I'm sure this is impossible but that yellow line annoys me. Shouldn't be able to return even with the config change.
			}

			String firstRoomName = config.incPuzzleCombat() ? firstTrueCombat.getName() : combatRooms.get(0).getName();

			overloadFound = overloadFilter.stream()
				.anyMatch(overload -> firstRoomName.equalsIgnoreCase(overload.getRoomName()));

			if (!overloadFound)
			{
				return; // Failed condition
			}
		}

		// Combat Room Flags
		for (RaidRoom room : combatRooms)
		{
			String roomName = room.getName();

			if (roomName.equalsIgnoreCase("unknown (combat)") && config.blockedUnknownCombat())
				return;

			for (String blockedRoom : roomFilter)
			{
				if (roomName.equalsIgnoreCase(blockedRoom))
					return;
			}

			if (!overloadFound)
				overloadFound = overloadFilter.stream().anyMatch(overload -> roomName.equalsIgnoreCase(overload.getRoomName()));
		}

		for (RaidRoom room : puzzleRooms)
		{
			String roomName = room.getName();

			if (roomName.equalsIgnoreCase("unknown (puzzle)") && config.blockedUnknownPuzzles())
				return;

			for (String blockedRoom : roomFilter)
			{
				if (roomName.equalsIgnoreCase(blockedRoom))
					return;
			}
		}

		if (!rotationFound)
		{
			List<String> rotationList = getConfigRotations();
			String activeRotation = getRaidRotation(raid);

			for (String rotation : rotationList)
			{
				if (activeRotation.equalsIgnoreCase(rotation))
				{    //  Doesn't include puzzles, but it should skip Overload, Layout and Rotation checks because the user specified the combat rooms
					layoutFound = true; // Look into skipping this because of 4c2p 4c1p overlap
					rotationFound = true;
					overloadFound = true;
					break;
				}
			}

			if (rotationList.isEmpty())
				rotationFound = true;
		}

		if (!layoutFound)
		{
			String exceptionList = config.layoutKeys().replaceAll("(\\s*)", "");
			for (String layout : exceptionList.split(","))
			{
				if (!raidLayoutCode.equalsIgnoreCase(layout))
					continue;

				layoutFound = true;
				break;
			}
		}

		// Overall Checker
		if (!crabPuzzleFlag)
			return;
		if (!layoutFound)
			return;
		if (!overloadFound)
			return;
		if (!rotationFound)
			return;

		raidFound = true;
		notifier.notify(String.format("Raid Found! (%s)", getRaidRotation(raid)));
	}

	@Subscribe
	public void onRaidReset(RaidReset raidReset)
	{
		raidFound = false;
		raidSearched = false;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		final int varbitId = varbitChanged.getVarbitId();
		final int varbitValue = varbitChanged.getValue();

		final int VARBIT_CM_FLAG = 6385;

		if (varbitId == VARBIT_CM_FLAG)    // Update isChallengeMode
		{
			isChallengeMode = varbitValue == 1;
		}

		if (varbitId == Varbits.RAID_STATE) // Update isStarted
		{
			isStarted = varbitValue == 1;
		}
	}

	@Provides
	ScoutHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ScoutHelperConfig.class);
	}
}