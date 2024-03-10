# Granblue Fantasy Relink Auto Pilot

## Background
This java program automates various aspects of the [Granblue Fantasy Relink](https://store.steampowered.com/app/881020/Granblue_Fantasy_Relink/) Game, 
enabling unlimited AFK farming of resources, even from the hardest content.

The program uses the `java.awt.Robot` library to send native mouse clicks and
key strokes, as well as detect on-screen pixels to understand game states.
The program does not hook into any of the game's processes nor modify the game's files.
Thus, the program is safe to use and conventional anti-bot programs can not detect its presence.

The program is not considered a "cheat" since it does not provide unfair advantage to players
during gameplay. Even though the program can execute most actions that a player can do, it
does not understand what's going on, so a player should be able to out perform the program
during combat. The program repeats a scripted set of actions, as seen in the `actionTimerAction()` function.

*The goal of the program is to save time for players, considering the grindy nature of the game.*

## How To Use
1. Download, unzip, and run the latest `gbfr_auto_pilot_vX.X.jar` program. It can be found under the "**releases**" section on the right, along with release notes.
2. Run the game and press the "**Start AFK Farm**" button on the program's user interface to engage the program. Move your mouse while the program is running to disengage.
   - **[Recommended]** Capture the `mission result pixel` and the `health bar pixel`, following the instructions on the user interface.
     - The `mission result pixel` allows the program to detect the game state (in-combat vs in-mission-result) which is used to reliably remove the 10-mission auto repeat limit.
     - The `health bar pixel` allows the program to locate the health bar as well as to determine the state of the main character's health, in order to perform actions such as drinking potions. When set, this also improves the accuracy of the mission result screen detection.

> [!NOTE]
> If you encounter the "***A java exception has occurred***" error when trying to open the *.jar file, please download and install the latest JDK from the official Oracle website:
> [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/#jdk21-windows)
>
> After that, if you still have trouble executing the *.jar file, download the raw source code and compile it using your IDE, then you should be able to run the program. Recommended free light-weight java IDE: [BlueJ](https://www.bluej.org/).

> [!CAUTION]
> When the program is running, do not focus a window that is not the intended game, without first disengaging the program. Doing so may cause unwanted results, since the program continuously sends native mouse clicks and keyboard strokes to your operating system while engaged.



