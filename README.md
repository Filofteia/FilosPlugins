# Cox Scouting QoL

CoX Scouting QoL allows you to define a good raid and allows removes the 'Reload' and 'Climb' option when found.

## Configuration Explained

To select multiple you may use ctrl-click or shift-click.

And to reset you can right-click the option (Overload Filter / Layout Filter) -> 'Reset'

General Settings:
- Notify on Raid: Sends a Notification upon finding a suitable raid

Layout Settings:
- [Layout Filter](#1-layout-filter): Allows you to choose a preferred raid type (3C2p, 4C1P, 4C2P) (None = Exception List)
- [Exception Layout](#2-exception-layout-ignore-filter): Allows a specified exception to the previous option

Raid Settings:
- Rotation Toggle: Toggles the next option
- [Rotations](#1-rotations): List rotations such as 'vasa,shamans,vespula' and split with making a new line
- [Blocked Rooms](#2-blocked-rooms): Block specific rooms
- Block Unknown Combat: Specifies if you want to block unknown combat rooms
- Block Unknown Puzzles: Specifies if you want to block unknown puzzle rooms

Overload Settings:
- [Overload Filter](#1-overload-filter): Choose the overloads you are willing to accept (None = Prep/Ignore)
- [Preferred Location](#2-preferred-location): Set a location in the raid that you would like the overload to be.
- [Include Puzzle Combat](#3-include-puzzle-combe): Include 'Tightrope' and 'Ice Demon' inside of [Preferred Location's](#2-preferred-location) 'First Combat'
## Layout Settings

### 1) Layout Filter

Layout Filter has a set of common raids so that you can select your preferred raid type as listed below.

1) 3C2P = 3 Combat 2 Puzzle
2) 4C1P = 4 Combat 1 Puzzle
3) 4C2P = 4 Combat 2 Puzzle
4) None = Only Exception Layouts

### 2) Exception Layout (Ignore Filter)

An exception to the Layout Filter

Example: With the filter 4C1P you could add 'FSCCS PCPSF' or 'FSCCSPCPSF' and that would allow this specific 3C2P through the filter.

Random Example: 'FSPCCPSCCF,SCPFCCCPSF,FSCCSPCPSF,FSCCPPCSCF,SCFCPCSCFS,SCPFCCCSSF,SCSPFCCSPF' (This is just an example do not use this)

## Raid Settings

### 1) Rotations

Rotations allows you to set a defined rotation (vasa,shamans,vespula)

You can create multiple rules by putting them on the next line:

vasa,shamans,vespula

vespula,shamans,vasa

vasa,tekton,vespula

vespula,tekton,vasa

### 2) Blocked Rooms

A list of possible rooms below (exact text and case-insensitive):

1) Tekton
2) Muttadiles
3) Guardians
4) Vespula
5) Shamans
6) Vasa
7) Vanguards
8) Mystics
9) Crabs
10) Ice Demon
11) Tightrope
12) Thieving

## Overload Settings

### 1) Overload Filter

1) Muttadile
2) Tekton
3) Vanguard
4) Vespula
5) Vasa
6) None (No Overload Required)

### 2) Preferred Location

1) Any Room : Any room may have an overload to meet the condition.
2) First Combat : The first combat room must have an overload to meet the condition.

### 3) Include Puzzle Combat
This will include 'Tightrope' and 'Ice Demon' as Combat rooms to not meet the conditions if they are before the Overload for 'First Combat' option.

# Known Issues and potential fixes:

## Reload Option remains on a good raid

I believe the main cause of this is Menu Entry Swapper.

To fix this you may do the following:

1) Shift Right-click the Entry Door inside the raid
2) 'Swap left click' -> 'Reset'

Other plugins that modify menu options may also cause this issue.;
