# Granblue Fantasy Relink Auto Pilot

## Background
This java program automates various aspects of the `Granblue Fantasy Relink` Game, 
enabling unlimited AFK farming of resources, even from the hardest content.

The program uses the `java.awt.Robot` library to send native mouse clicks and
key strokes, as well as detect on-screen pixels to understand game states.
The program does not hook into any of the game's processes nor modify the game's files.
Thus, the program is safe to use and conventional anti-bot programs can not detect its presence.

The program is not considered a "cheat" since it does not provide unfair advantage to players
during gameplay. Even though the program can execute most actions that a player can do, it
does not understand what's going on, so a player should be able to out perform the program
during combat. The program repeats a scripted set of actions, as seen in the `actionTimerAction()` function.

The goal of the program is to save time for players, considering the grindy nature of the game.

## How to use
// TODO

## Major Releases

### V2.4 - 2024-03-10
// TODO

### V2.2 - 2024-03-03

##### New Features
- Auto drink potions.
- Added UI to capture health bar pixel for indicating where the health bar is.
- Auto use link attacks.
- Auto use SBAs.
##### Bug Fixes
- Improved mission result screen detection. The program is less likely to fail to detect that the mission result screen is showing.
##### Misc
- Improved combat stability. Combos executions are smoother.
