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
import filo.scouter.config.IncludeMode;
import filo.scouter.config.Layout;
import filo.scouter.config.Overload;
import filo.scouter.config.OverloadPosition;
import filo.scouter.data.PuzzleLayout;
import java.awt.Color;
import java.util.stream.Collectors;
import javax.inject.Inject;
import filo.scouter.data.RoomEnum;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
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
	@Inject
	private ConfigManager configManager;
	@Inject
	private ChatMessageManager chatMessageManager;

	// COnfig stuff
	private final int CURRENT_CONFIG_VERSION = 1;
	private final int CURRENT_MESSAGE_VERSION = 2;

	// Varbit
	private boolean inRaid = false;
	private boolean isStarted = false;
	private boolean isChallengeMode = false;

	private boolean raidFound = false;
	private boolean raidSearched = false;
	private RaidScouted scoutedEvent;

	@Override
	protected void startUp() throws Exception
	{
		migrateConfig();	// If I have another migration I'll improve it
		scoutedEvent = null;
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

	private List<RaidRoom> getOrderedRooms(Raid raid, boolean includePuzzle)
	{
		if (includePuzzle)
		{
			return getOrderedRooms(raid, RoomType.COMBAT, RoomType.PUZZLE);
		}

		return getOrderedRooms(raid, RoomType.COMBAT);
	}

	private String getRaidRotation(Raid raid, boolean includePuzzles)
	{
		StringBuilder rotation = new StringBuilder();

		for (RaidRoom room : getOrderedRooms(raid, includePuzzles))
		{
			rotation.append(room.getName()).append(",");
		}

		return rotation.substring(0, rotation.length() - 1);
	}

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

		if (config.showUpdateMessage())
			sendUpdateMessage();

		raidSearched = true;
		scoutedEvent = raidScouted;

		List<RaidRoom> allRooms = getOrderedRooms(raid, RoomType.COMBAT, RoomType.PUZZLE);
		List<RaidRoom> combatRooms = allRooms.stream()
				.filter(raidRoom -> raidRoom.getType() == RoomType.COMBAT)
				.collect(Collectors.toList());
		List<RaidRoom> puzzleRooms = allRooms.stream()
				.filter(raidRoom -> raidRoom.getType() == RoomType.PUZZLE)
				.collect(Collectors.toList());

		Set<Layout> layoutFilter = config.layoutType();;
		Set<Overload> overloadFilter = config.overloadRooms();
		List<String> roomFilter = Text.fromCSV(config.blockedRooms());
		Set<String> requiredRooms = RoomEnum.validateRequiredRooms(config.requiredRooms().toLowerCase());

		String raidLayoutCode = raid.getLayout().toCodeString();
		Layout raidLayout = Layout.findLayout(combatRooms.size(), puzzleRooms.size());

		boolean crabPuzzleFlag = verifyCrabs(raidLayoutCode, puzzleRooms);
		boolean layoutFound =
				config.layoutMode() != IncludeMode.EXCLUSIVE
				&& (layoutFilter.isEmpty() || layoutFilter.contains(raidLayout));

		boolean overloadFound =	overloadFilter.isEmpty();
		boolean rotationFound = !config.rotationEnabled();

		if (config.ovlPos() == OverloadPosition.FIRST_COMBAT && !overloadFound)
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
				return;
			}
		}

		for (RaidRoom room : combatRooms)
		{
			String roomName = room.getName();
			requiredRooms.remove(roomName.toLowerCase());

			if (roomName.equalsIgnoreCase("unknown (combat)") && config.blockedUnknownCombat())
				return;

			for (String blockedRoom : roomFilter)
			{
				if (roomName.equalsIgnoreCase(blockedRoom))
					return;
			}

			if (!overloadFound)
				overloadFound = overloadFilter
						.stream()
						.anyMatch(overload -> roomName.equalsIgnoreCase(overload.getRoomName()));
		}

		for (RaidRoom room : puzzleRooms)
		{
			String roomName = room.getName();
			requiredRooms.remove(roomName.toLowerCase());

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
			String activeCombatRotation = getRaidRotation(raid, false);
			String activePuzzleRotation = getRaidRotation(raid, true);

			for (String rotation : rotationList)
			{
				boolean hasPuzzleRooms = RoomEnum.hasPuzzleRoom(rotation);

				if (
						(!hasPuzzleRooms && activeCombatRotation.equalsIgnoreCase(rotation)) ||
						(hasPuzzleRooms && activePuzzleRotation.equalsIgnoreCase(rotation)))
				{
					rotationFound = true;
					overloadFound = true;
					break;
				}
			}

			if (rotationList.isEmpty())
				rotationFound = true;
		}

		if (!layoutFound && config.layoutMode() != IncludeMode.DISABLED)
		{
			String exceptionList = config.layoutKeys().replaceAll("(\\s*)", "");
			for (String layout : exceptionList.split(","))
			{
				if (!raidLayoutCode.equalsIgnoreCase(layout))
					continue;

				layoutFound = true;
				break;
			}

			// Should include this iin the initial check
			if (config.layoutMode() == IncludeMode.EXCLUSIVE
					&& exceptionList.isBlank()
					&& layoutFilter.contains(raidLayout))
			{
				layoutFound = true;
			}
		}

		if (!requiredRooms.isEmpty())
			return;
		if (!crabPuzzleFlag)
			return;
		if (!layoutFound)
			return;
		if (!overloadFound)
			return;
		if (!rotationFound)
			return;

		raidFound = true;

		if (config.notifyRaid().isEnabled())
			notifier.notify(config.notifyRaid(), String.format("Raid Found! (%s)", getRaidRotation(raid, true)));
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

		if (varbitId == VarbitID.RAIDS_CHALLENGE_MODE)
		{
			isChallengeMode = varbitValue == 1;
		}

		if (varbitId == VarbitID.RAIDS_CLIENT_PROGRESS)
		{
			isStarted = varbitValue == 1;
		}

		if (varbitId == VarbitID.RAIDS_CLIENT_INDUNGEON)
		{
			inRaid = varbitValue == 1;
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (!e.getGroup().equalsIgnoreCase("coxscoutingqol"))
			return;
		if (e.getKey().equalsIgnoreCase("lastUpdateMessageVer"))
			return;	// caused infinite recursion during testing

		if (inRaid && scoutedEvent != null)
		{
			raidFound = false;
			raidSearched = false;
			onRaidScouted(scoutedEvent);
		}
	}

	private void migrateConfig()
	{
		if (config.configVer() == 0)
		{
			String group = "coxscoutingqol";
			String key_ver = "configVer";

			String key_ovlPos = "overloadPosition";
			String ovlPos = configManager.getConfiguration(group, key_ovlPos);
			if (ovlPos != null)
			{
				if (ovlPos.equals("COMBAT_FIRST"))
				{
					configManager.setConfiguration(group, key_ovlPos, OverloadPosition.FIRST_COMBAT);
				}
			}

			configManager.setConfiguration(group, key_ver, 1);
		}
	}

	private void sendUpdateMessage()
	{
		if (config.lastUpdateMessageVer() == CURRENT_MESSAGE_VERSION)
			return;

		String group = "coxscoutingqol";
		String key_ver = "lastUpdateMessageVer";
		Color pluginColour = new Color(64, 51, 255);

		final ChatMessageBuilder messageBuilder = new ChatMessageBuilder();

		messageBuilder.append(pluginColour, "Cox Scouting QoL updated!").append("\n");

		if (config.lastUpdateMessageVer() < 2)
		{
			messageBuilder.append("- Added 'Required Room' filter to the config")
					.append("\n");
		}

		if (config.lastUpdateMessageVer() < 1)
		{
			messageBuilder.append("- Added Puzzle support for Rotations")
					.append("\n")
					.append("- Behaviour Changes: When no 'Layout Filter' is selected, all layouts are accepted. Restore old behaviour by setting 'Layout Mode' to 'Exclusive'")
					.append("\n");
		}

		messageBuilder.append(pluginColour, "This message will only appear once. You can disable it in the config");

		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(messageBuilder.build())
				.build());

		configManager.setConfiguration(group, key_ver, CURRENT_MESSAGE_VERSION);
	}

	@Provides
	ScoutHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ScoutHelperConfig.class);
	}
}