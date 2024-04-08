# Cox Scouting QoL

CoX Scouting QoL allows you to define a good raid and removes the reload option from them.

The purpose of this plugin is to make it easier to scout.

## General Settings

### Notify on Raid

This will send a system notification upon finding a suitable raid.

## Layout Settings

### Layout Filter

Layout Filter has a box of common raids that you can select your preferred raid size.

#### !!! 5 Combat Currently Unsupported (Until Base Scouter Fixed) !!!

1) 3C2P = 3 Combat 2 Puzzle
2) 4C1P = 4 Combat 1 Puzzle
3) 4C2P = 4 Combat 2 Puzzle
4) None = Exception List

To select all or multiple you may shift-click or ctrl-click them.

And to reset the filter to none right-click "Layout Filter" -> "Reset"

### Exception Layout (Ignore Filter)

This allows an exception in the prior rule.

For example you may set your raid to 4C1P and add the exception 'FSCCS PCPSF' or 'FSCCSPCPSF' to bypass that specific raid (3C2P) in the filter.

To add multiple seperate by ',' and spaces are not required but are allowed.

Random Example: 'FSPCCPSCCF,SCPFCCCPSF,FSCCSPCPSF,FSCCPPCSCF,SCFCPCSCFS,SCPFCCCSSF,SCSPFCCSPF'

## Raid Settings

### Rotation Toggle

This allows you to toggle the next option without having to clear it or anything.

### Rotations

Rotations allows you to set a defined rotation (vasa,shamans,vespula)

You can create multiple rules by using creating a new line

vasa,shamans,vespula
vespula,shamans,vasa
vasa,tekton,vespula
vespula,tekton,vasa

### Blocked Rooms

Blocked Rooms just blocks rooms so list of names below

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
11) Tigthtrope
12) Thieving

Same as before this can be seperated by using a comma ','

## Overload Settings

### Overload Rooms

You can select multiple with Shift Click or Ctrl Click.

Selecting none means that no overload is required.

To reset to none Right Click 'Overload Filter' -> 'Reset' in the side panel.

# Issues:

## Reload stays on a scouted raid

Currently I believe this is caused by Menu Entry Swapper.

To fix this you may do the following:

1) Press Shift and Right Click on the Raid Entrance.
2) 'Swap left click' -> 'Reset'
3) This should fix the situtation for that scenario.

Other plugins that modify menu options may also cause issues.
