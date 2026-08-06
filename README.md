# AFK Hider Plugin
This plugin changes the RuneLite client's opacity after you click a configured menu option.

**Interaction Format:**<br>
Menu Option:Target

**Interactions example:**

mine:crashed star<br>
chop down:ironwood tree<br>
chop down:redwood tree<br>

Interactions are not case-sensitive. Use the 'Log Actions' config to print these both to chat.

## Configuration:

#### Restore Options:
- Restore Keybind: Restores the client when pressed. You must be tabbed into the game.
- Restore on Notification: Restores the client when a notification is received.
- Restore on Hitsplat: Restores the client when your player receives a hitsplat.
- Idle Restore: Restores the client after a set duration.

#### Input protection:
- Block Cursor Input: Blocks all mouse clicks while the client is hidden.
- Block Keyboard Input: Blocks all keyboard input while the client is hidden.

These settings are very recommended to prevent accidental clicks or typing into the wrong window.

This plugin does not restore the client automatically. It depends on other plugins that send notifications such as 'Idle Notifier' or things like 'Watchdog'.