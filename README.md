# Aton Addons

A 1.8.9 Minecraft Forge mod for Hypixel Skyblock.

***

<p align="center">
  <a href="https://github.com/FloppaCoding/AtonAddons/releases" target="_blank">
    <img alt="downloads" src="https://img.shields.io/github/v/release/FloppaCoding/AtonAddons?color=ff3f0b&style=flat&logo=GitHub" />
  </a>
  <a href="https://github.com/FloppaCoding/AtonAddons/blob/main/LICENSE" target="_blank">
    <img alt="license" src="https://img.shields.io/github/license/FloppaCoding/AtonAddons?color=ff3f0b&style=flat&logo=GitHub" />
  </a>
<!--
  <a href="https://github.com/FloppaCoding/AtonAddons/releases" target="_blank">
    <img alt="downloads" src="https://img.shields.io/github/downloads/FloppaCoding/AtonAddons/total?color=ff3f0b&style=flat&logo=GitHub" />
  </a>
  <a href="https://discord.gg/KhWE9HspKM" target="_blank">
    <img src="https://img.shields.io/discord/1020124307231358977?label=discord&style=flat&color=informational&logo=Discord&logoColor=FFFFFF" alt="discord">
  </a>
-->
</p>


## Usage
Open the Gui with /atonaddons (or /aa). You can then set a keybind for it in the Click Gui Module.

By default all features will be disabled. 
You can toggle features by left clicking on their corresponding module button in the click gui. 
Right clicking that button will extend the settings for that module.
All modules have a key bind setting, which for most of them will toggle the module. 
But there are a few exceptions where that key bind is used to perform the modules action if it is enabled.

**If you need information on what the module and its settings do middle click the module button and an
advanced settings menu will open.**

Below you can see a preview of the gui.
![Gui Preview](./resources/GuiPreview.png "Gui Preview")

## Module List

<details>
  <summary>Dungeon</summary>

### DUNGEON
* Secret Chimes -- Play a sound whenever you get a secret.
* Extra Stats -- Automatically shows extras stats at the end of a dungeon run.
* Leap Highlights -- Highlights chosen target in the Spirit Leap menu.
* Party Tracker -- Shows an overview of what your party members did, at the end of a Dungeon run.

<details>
  <summary>Screenshots</summary>

![Party Tracker](./resources/partyTracker/PartyTracker__Simplified.png)
![Rooms Hover](./resources/partyTracker/PartyTracker_ClearedHover.png)
![Puzzle Hover](./resources/partyTracker/PartyTracker_PuzzleHover.png)
![Room Times](./resources/partyTracker/PartyTracker_RoomTimeHover.png)

</details>

</details>

<details>
  <summary>Render</summary>

### RENDER
* Click Gui
* Edit Hud
* Dungeon Warp Timer -- A HUD element that shows you the cooldown on dungeon warps.
* Dungeon Map -- A reliable dungeon map.
* Dungeon  Wither/Blood Door ESP.
* Coordinate HUD
* Item Animations -- Change the appearance of held items.
* No Fire Overlay
</details>

<details>
  <summary>Misc</summary>

### MISC
* Toggle Sprint
* Remove Front View -- Skips the front view in the toggle perspective rotation.
</details>

<details>
  <summary>Custom Keybindings</summary>

### Custom Keybindings
* Command keybindings
* Chat message keybindings
</details>

<details>
  <summary>Commands</summary>

### Commands
* /aa -- Open the gui.
* /aa resetgui -- Reset the position of panels in the gui.
</details>

## About the compliance with Hypixel Rules.
<details> 
  <summary>Dungeon Map</summary>

Statement in the [Hypixel Server Rules](https://support.hypixel.net/hc/en-us/articles/6472550754962-Allowed-Modifications) regarding Cosmetic HUD (Head-Up Display) Modifications:
> Modifications that alter the look and feel of the in-game head-up display (HUD), without adding extra information which would normally be unavailable to the player. For example, HUDs adding armor and status effects, which are available to the player in their inventory screen, are permitted, while mini-maps, other player health/armor indicators, player distance/range, etc. are not.

While the Dungeon Map module offers an option that allows you to scan the dungeon, it will not display any information which would be unavailiable otherwise. 
It is only used to show you information about rooms you already visited. 
These rooms can not only be visually identified easiely, but the scoreboard also contains an ID that can be mapped to the room you are in.

In this way it behaves similar to popular Dungeon Puzzle solvers like found in [Skytils](https://github.com/Skytils/SkytilsMod) and [DSM](https://github.com/bowser0000/SkyblockMod).
</details>

## Making your own Modules
To get started with coding on your own, check out the [wiki](https://github.com/FloppaCoding/AtonAddons/wiki). 

## Credit to other Projects
A special thank goes to [Harry282](https://github.com/Harry282), his projects allowed me to get started with making my own 
forge mods.
<br>
[For the list of sources click here.](./USEFUL_SOURCES.md "Credits")
