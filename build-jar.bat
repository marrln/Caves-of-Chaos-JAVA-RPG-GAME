@echo off
REM =========================================
REM  Caves of Chaos - UTF-8 JAR Build Script
REM  Supports Java 24+
REM =========================================

setlocal enabledelayedexpansion

REM Use UTF-8 in this console session
chcp 65001 >nul

echo Using Java:
java -version
echo.

echo.
echo ========================================
echo   Building Caves of Chaos JAR (UTF-8)
echo ========================================
echo.

REM Step 1: Clean and create directories
echo [1/5] Cleaning old build...
if exist bin rmdir /S /Q bin
if exist dist rmdir /S /Q dist
mkdir bin
mkdir dist

REM Step 2: Compile all Java sources (UTF-8 + current Java)
echo [2/5] Compiling Java sources...
javac -encoding UTF-8 -d bin -sourcepath src ^
    src\CavesOfChaos.java ^
    src\audio\*.java ^
    src\config\*.java ^
    src\core\*.java ^
    src\enemies\*.java ^
    src\graphics\*.java ^
    src\input\*.java ^
    src\items\*.java ^
    src\map\*.java ^
    src\player\*.java ^
    src\ui\*.java ^
    src\utils\*.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo      Compilation successful!

REM Step 3: Copy resources to bin directory
echo [3/5] Copying game resources...
if not exist bin\assets mkdir bin\assets
if not exist bin\config mkdir bin\config
xcopy /Y /Q /E /I src\assets bin\assets\ >nul
xcopy /Y /Q /I src\config\*.xml bin\config\ >nul
echo      Resources copied!

REM Step 4: Create the JAR file
echo [4/5] Creating JAR file...
jar cfm dist\CavesOfChaos.jar MANIFEST.MF -C bin .

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] JAR creation failed!
    pause
    exit /b 1
)

REM Step 5: Finish
echo.
echo ========================================
echo   BUILD SUCCESSFUL!
echo ========================================
echo.
echo Your game is ready: dist\CavesOfChaos.jar
echo.
echo To run it:
echo   java -jar dist\CavesOfChaos.jar wizard PlayerName
echo.
pause
