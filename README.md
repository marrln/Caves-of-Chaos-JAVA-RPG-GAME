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

- Java 17 or higher

### Running

**Command Line:**
```sh
javac -d bin -sourcepath src src/CavesOfChaos.java
java -cp bin CavesOfChaos <player-class> <player-name> [starting-level]
```
Example:
```sh
java -cp bin CavesOfChaos wizard Gandalf 1
```

**Batch Script (Windows):**
Run `run.bat` for the default wizard class and name, or pass arguments:
```sh
run.bat duelist Conan
```

## Features

- Procedurally generated cave levels for every run
- Turn-based combat with class-specific abilities
- Item collection and inventory management
- Boss level with persistent boss health
- Save and load functionality
- Modular and extensible codebase

## Technologies

- Java (99%)
- Batchfile (1%) for Windows automation

## Project Structure

```
src/
 ├── core/          # Game logic and state
 ├── player/        # Player classes and abilities
 ├── enemies/       # Enemy types and AI
 ├── map/           # Map generation and navigation
 ├── ui/            # Swing-based user interface
 └── config/        # Game configuration files
```

## License

Look under License for more details.

## Acknowledgments

Developed during an M.Sc. program, this project aims to demonstrate core Java programming concepts, game design, and procedural generation.

---