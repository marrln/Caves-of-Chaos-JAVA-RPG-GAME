# Caves of Chaos - Java RPG Game
As a part of a JAVA project during my M.Sc. I created this RPG game. Feel free to play!

## Description
Caves of Chaos is a roguelike RPG game where you play as a noble of the Court of Chaos. Your goal is to navigate through 10 levels of the labyrinth, defeat the Serpent of Chaos, and obtain the Jewel of Judgement.

## How to Run
The game can be run in several ways:

### Windows Command Line
```
cd path\to\game
javac -d bin -sourcepath src src\CavesOfChaos.java
java -cp bin CavesOfChaos <player-class> <player-name>
```

### Using the Provided Scripts
1. Windows Command Prompt (CMD): Run the `run.bat` file
   - Default: `run.bat` (runs with wizard class and Player1 name)
   - Custom: `run.bat duelist YourName`

2. PowerShell: Run the `run.ps1` file
   - Default: `.\run.ps1` (runs with wizard class and Player1 name)
   - Custom: `.\run.ps1 duelist YourName`

## Game Controls
- **Movement**: W (up), A (left), S (down), D (right)
- **Attack**: Space (attacks nearest enemy)
- **Use Items**: H (health potion), M (mana potion)
- **Rest**: R (recover health and mana)
- **Switch Weapon**: P (when standing on a weapon tile)

## Player Classes
- **Wizard**: Lower HP, can cast spells using mana
- **Duelist**: Higher HP, melee combat focused

## Game Objective
Navigate through 10 levels of the labyrinth, defeat the Serpent of Chaos on level 10, and obtain the Jewel of Judgement to win the game.
