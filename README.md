# Nora Tweaks

Nora Tweaks is a custom addon for the [Meteor Client](https://meteorclient.com/) that introduces quality-of-life features. If you have any recommendations, feel free to open a feature request.

## Features

*   **Custom Category Manager:** Create, manage, and sort your own custom module categories, which appear seamlessly in the module list.
(Implemented by Fractal420) Supports drag-and-drop reordering and moving of modules between custom categories, middle-click to remove modules from custom categories, and hiding categories.
*   **Meteor GUI Position Fix:** (Implemented by Fractal420) Remembers and restores Meteor Client GUI window positions across sessions (saved to `config/nora-tweaks-gui-positions.json`). Also clamps windows to the screen, resolves overlaps on drag release by finding the nearest free position, and works with both the default theme and Catppuccin.
*   **Auto Dirt Path:** Automatically creates paths from dirt-like blocks when holding a shovel.
*   **Auto Farm Land:** Automatically creates farm land from dirt-like blocks when holding a hoe.
*   **Auto Farm:** Harvests and replants configured crops automatically. (Adapted from Meteor Rejects)
*   **Auto Log Strip:** Automatically places, strips, and breaks logs for efficient processing with configurable delays and auto-refill.
*   **Mace Combo:** Auto-attacks targets and chains mace combos with wind charges for enhanced combat.
*   **Legit Mace Kill:** Makes the Mace powerful when swung by amplifying your actual fall distance (only works when falling).
*   **Attribute Swapping:** Swaps to a target slot (or smart enchant/weapon selection) when you attack.
*   **Chat Utility:** A collection of chat-related tools including keyword notifications, auto-messaging, and desktop toast notifications.
*   **Hotkey Utility:** Set up quick hotbar changes with key combination.
*   **Wind Charge Jump:** Automatically jumps when you throw a wind charge underneath yourself for enhanced mobility.
*   **Deepslate ESP:** Highlights deepslate-related blocks/entities for visibility.
*   **Pearl Checker:** Shows owner nametags on ender pearls, sends throw/land chat notifications, and predicts landing.
*   **Auto Trap Plus:** Enhanced trapping helper for faster, more reliable traps.
*   **Better Locator:** Enhances the vanilla Locator Bar HUD with additional features (player heads, directions, waypoints, etc.).
*   **Ore Sim:** Seed-aware ore position simulator with optional Baritone goal syncing. (Adapted from Meteor Rejects)

## Commands

*   **.mobchecker** — Count nearby mobs with optional range and name filter.
    *   `.mobchecker` → default range 64
    *   `.mobchecker <range>` → e.g., `.mobchecker 96`
    *   `.mobchecker <range> <filter>` → e.g., `.mobchecker 96 zombie`
*   **.calculator** — Evaluate math expressions or quick damage/survival formulas.
*   **.seed-world** — Command to store world seed. (Adapted from Meteor Rejects)
*   **.seed-locate** — Locate structures via the stored seed using Cubiomes; prints coordinates and distance. (Adapted from Meteor Rejects)
*   **.hostinfo** — Shows server host and Minecraft protocol information.

## Building (for developers)

Multi-version support uses [Stonecutter](https://stonecutter.kikugie.dev/). Shared sources live in `src/`.

| Version     | Status  | Java | Notes                                                      |
|-------------|---------|------|------------------------------------------------------------|
| **26.2**    | Full    | 25   | Default / active                                           |
| **26.1.2**  | Full    | 25   |                                                            |
| **1.21.11** | Partial | 21   | Build script ready; needs more API ports |

```bash
./gradlew build              # active version (26.2)
./gradlew :26.2:build
./gradlew :26.1.2:build
./gradlew buildAll           # builds 26.1.2 + 26.2

# 1.21.11 (experimental — may fail until more //? gates are added)
./gradlew :1.21.11:build
