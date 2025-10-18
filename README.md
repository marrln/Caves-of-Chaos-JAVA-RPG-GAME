# Caves of Chaos - Java RPG Game

## Overview

Caves of Chaos is a roguelike role-playing game written in Java. Originally created as part of an M.Sc. project, it offers a classic dungeon-crawling experience where players select a character class and attempt to navigate a labyrinth filled with monsters, traps, and treasures. The objective is to reach the deepest level, defeat the final boss, and secure victory.

## Game Description

Upon launching Caves of Chaos, players choose between two classes: Wizard or Duelist. Each class has unique abilities and playstyles. The game features multiple levels, each procedurally generated using cellular automata to create a new cave layout for every run. Players fight enemies, collect items, and manage resources as they progress.

The ultimate goal is to descend through all cave floors, culminating in a battle against the Medusa of Chaos, the final boss. The boss is persistent and maintains its health across level transitions, offering a challenging endgame encounter. Victory is achieved by defeating the Medusa and surviving the cave's dangers.

### Main Function and Structure

The entry point of the game is the `main` method in `src/CavesOfChaos.java`. Its responsibilities include:

- **Loading Configuration:** Reads game settings, assets, and styling from XML files.
- **Parsing Arguments:** Requires the player to specify a class and name, with an optional starting level.
- **Player Initialization:** Instantiates the chosen player class (`Wizard` or `Duelist`) and sets up initial parameters.
- **Game State Creation:** Builds the core `GameState` object, which manages the player, map generation, enemies, and progression.
- **User Interface:** Sets up the main game window using Java Swing, including panels for gameplay, status, and logs.
- **Cleanup:** Handles resource management and cleanup on game exit.

The game map is generated with each new level, ensuring replayability and dynamic challenges. The game state tracks progression, enemy encounters, and player inventory.

## How to Run

### Requirements

Before running the game, ensure you have the following installed:
- Java 24 or higher

### Method 1: Quick Play (Windows - Recommended for Development)

The easiest way to compile and run the game on Windows:

```sh
run.bat
```

This will:
1. Compile all source files
2. Copy assets and configuration files
3. Run the game with default settings (Wizard class, Player1 name)

**With custom arguments:**
```sh
run.bat wizard Gandalf
run.bat duelist Conan
```

### Method 2: Build Distributable JAR 

To create a standalone JAR file that can be shared and run on any system:

```sh
build-jar.bat
```

This will:
1. Clean previous builds
2. Compile all source files
3. Package everything into `dist/CavesOfChaos.jar`

**Run the JAR:**
```sh
java -jar dist/CavesOfChaos.jar wizard PlayerName
```

Example:
```sh
java -jar dist/CavesOfChaos.jar wizard Gandalf
java -jar dist/CavesOfChaos.jar duelist Conan
```

### Available Player Classes

- **wizard** - Ranged magic user with powerful spells
- **duelist** - Melee fighter with balanced combat abilities

## Features

- Procedurally generated cave levels for every run
- Real-time combat with class-specific abilities and cooldowns
- Item collection and inventory management
- Class-specific weapons with special abilities
- Boss level with persistent health
- Modular and extensible codebase

## Technologies

- Java (99%)
- Batchfile (1%) for Windows automation

## Project Structure

```
src/
 ├── CavesOfChaos.java  # Main entry point
 ├── audio/             # Music and sound effects management
 ├── config/            # Game configuration and XML settings
 ├── core/              # Game logic, state, and combat system
 ├── enemies/           # Enemy types, factories, and AI
 ├── graphics/          # Rendering, animations, and sprite management
 ├── input/             # Keyboard and game input handling
 ├── items/             # Items, weapons, potions, and inventory
 ├── map/               # Procedural map generation and navigation
 ├── player/            # Player classes and abilities (Wizard, Duelist)
 ├── ui/                # Swing-based user interface components
 ├── utils/             # Utility classes and helper functions
 └── assets/            # Game resources (sprites, fonts, music, SFX)
```

## License

Caves of Chaos is not to be used commercially. You are free to modify and distribute the code for personal or educational purposes, provided that you give appropriate credit to the original author.
Look under License for more details.

## Assets from itch.io

Some game assets were sourced from [itch.io](https://itch.io), a platform for indie game developers. Please refer to the `assets/` directory for specific asset credits and licenses, where you will find a README file listing all third-party assets used in this project.

---
