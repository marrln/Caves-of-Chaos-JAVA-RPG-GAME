@echo off
title Caves of Chaos - Boss Level Test
echo Starting Caves of Chaos on Boss Level (Level 5)...
echo.

REM Check if player class is provided as argument
if "%1"=="" (
    echo Usage: run_boss.bat [duelist^|wizard] [optional_name]
    echo Example: run_boss.bat wizard gandalf
    echo Example: run_boss.bat duelist conan
    echo.
    echo Defaulting to wizard...
    set PLAYER_CLASS=wizard
    set PLAYER_NAME=TestWizard
) else (
    set PLAYER_CLASS=%1
    if "%2"=="" (
        set PLAYER_NAME=BossSlayer
    ) else (
        set PLAYER_NAME=%2
    )
)

echo Compiling the game...
javac -cp "lib\gson-2.10.1.jar;src" -d bin src\*.java src\audio\*.java src\config\*.java src\core\*.java src\enemies\*.java src\graphics\*.java src\input\*.java src\items\*.java src\map\*.java src\player\*.java src\ui\*.java src\utils\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!

REM Copy assets to bin directory
xcopy /s /y "src\assets" "bin\assets" > nul 2>&1
xcopy /s /y "src\config\*.xml" "bin\config\" > nul 2>&1

echo Running the game on Boss Level...
echo Player: %PLAYER_CLASS% named %PLAYER_NAME%
echo Level: 5 (Medusa of Chaos)
echo.

REM Start on level 5 (boss level) - we'll need to modify the game to accept level parameter
java -cp "bin;lib\gson-2.10.1.jar" CavesOfChaos %PLAYER_CLASS% %PLAYER_NAME% 5

echo.
echo Game cleanup completed - goodbye!
pause