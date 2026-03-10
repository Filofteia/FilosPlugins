# Cox Scouting QoL

CoX Scouting QoL allows you to filter a good raid and allows removes the 'Reload' and 'Climb' option when found.

## Update Log:
An update log for a plugin is kind of useless, however it might help if you've not raided in a while and want to see some of the changes.
Because some of these are logic based I guess.
### 0.9.1 (10/03/2026)
- Added 'Required Rooms' field
### 0.9 (03/02/2026)
- Added Puzzle Support to the 'Rotations' option
- Added 'Exception Mode' to allow for more control over the 'Exception List'
- Added a 'Scout Reload' upon changing config
- Prevent Rotations from bypassing the Layout Filter

## Known Issues
### Issues:
- Menu Entry Swapper overrides the door options (Solution with the Error Logger)
- Specific Interactions between settings (The Side Panel should help with this)
- Large Raid Support

## Configuration Explained

To select multiple you may use ctrl-click or shift-click.

And to reset you can right-click the option (Overload Filter / Layout Filter) -> 'Reset'

General Settings:
- Notify on Raid: Sends a Notification upon finding a suitable raid
- Update Message: Notify the user of changes to the plugin upon scouting.

Layout Settings:
- [Layout Filter](#1-layout-filter): Allows you to choose a preferred raid type (3C2P, 4C1P, 4C2P) (None = Exception List)
- [Exception Mode](#2-exception-layout-mode): Change the mode for the exception layout to be applied
- [Exception Layout](#3-exception-layout): Allows a specified exception to the previous option


Rotation Settings
- Rotation Toggle: Toggles the next option
- [Rotations](#1-rotations): List rotations such as 'vasa,shamans,vespula' and split with making a new line

Room Settings:
- [Blocked Rooms](#1-blocked-rooms): Block specific rooms
- Block Unknown Combat: Specifies if you want to block unknown combat rooms
- Block Unknown Puzzles: Specifies if you want to block unknown puzzle rooms
- [Preferred Crabs](#2-preferred-crabs): Specify a certain crab layout you prefer (Any, Rare, Good).

Overload Settings:
- [Overload Filter](#1-overload-filter): Choose the overloads you are willing to accept (None = Prep/Ignore)
- [Preferred Location](#2-preferred-location): Set a location in the raid that you would like the overload to be.
- [Include Puzzle Combat](#3-include-puzzle-combat): Include 'Tightrope' and 'Ice Demon' inside [Preferred Location's](#2-preferred-location) 'First Combat'
## Layout Settings

### 1) Layout Filter

Layout Filter has a set of common raids so that you can select your preferred raid type as listed below.

1) 3C2P = 3 Combat 2 Puzzle
2) 4C1P = 4 Combat 1 Puzzle
3) 4C2P = 4 Combat 2 Puzzle
4) None = Only Exception Layouts

### 2) Exception Layout mode
Allows you to choose the method:

- Inclusive: Includes the list alongside your scout filter

- Exclusive: Only search for the specified layouts

For example, you could add a specific 4C2P and have the filter set to 3C2P.

- Inclusive: The 4C2P would be accepted alongside the 3C2P Filter.
- Exclusive: The 4C2P would be the ONLY layout allowed, even the 3C2P would be filtered out.

Be careful, as this could make your scouting time very long.

### 3) Exception Layout

List of possible Layout Codes

3C2P
- FSCCSPCPSF
- SCPFCCSPSF
- SFCCSPCPSF
- SPSFPCCCSF
- SCSPFCCSPF


4C1P
- SCPFCCCSSF
- SCCFCPSCSF
- SCFCPCSCFS

4C2P
- SCFPCCSPCF
- SCFPCSCPCF
- SFCCPCSCPF
- SCFCPCCSPF
- SPCFCCSPCF
- FSCCPPCSCF
- SCFPCPCCSF
- FSCPCCSCPF
- SCFCPCSCPF
- SCPFCCSPCF
- SPCFCSCCPF
- SCPFCCCPSF
- FSPCCPSCCF
- SCPFCPCSCF
- SCCFPCCSPF

## Rotation Settings

### 1) Rotations

Rotations allows you to set a defined rotation (vasa,shamans,vespula)

You can add multiple rotations by adding them on new lines. Also you can now add puzzles!

vasa,thieving,shamans,vespula,crabs

muttadiles,shamans,mystics

## Room Settings

### 1) Blocked Rooms

A list of possible rooms below (exact text and case-insensitive):

Combats:
- Tekton
- Muttadiles
- Guardians
- Vespula
- Shamans
- Vasa
- Vanguards
- Mystics

Puzzles:
- Crabs
- Ice Demon
- Tightrope
- Thieving

### 2) Preferred Crabs
Allows you to filter a preferred crab room:

- Any - Any crab variation
- Rare - Rare or Good crab variation
- Good - Only Good Crab variation

## Overload Settings

### 1) Overload Filter

1) Muttadile
2) Tekton
3) Vanguard
4) Vespula
5) Vasa

Select your 'Overload' rooms, the raid will check for one of these.

If you wish to clear the list you can right-click 'Overload Filter' and press 'reset'.

### 2) Preferred Location

- Any Room
- First Combat

This is for the position of the Overload.

### 3) Include Puzzle Combat
If selected both Tightrope and Ice Demon are included in the 'First Combat' filter.

# Current Fixes

## Reload Option remains on a good raid

I believe the main cause of this is Menu Entry Swapper.

To fix this you may do the following:

1) Shift Right-click the Entry Door inside the raid
2) 'Swap left click' -> 'Reset'

Other plugins that modify menu options may also cause this issue.;
