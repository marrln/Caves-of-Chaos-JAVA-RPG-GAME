@echo off
REM ============================================
REM  Caves of Chaos - UTF-8 Compile & Run Script
REM  Updated for Java 25+
REM ============================================

REM Set console to UTF-8 (for proper symbol display)
chcp 65001 >nul

REM Set Java 25 path (update if your JDK 25 is in a different folder)
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java:
java -version
echo.

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

echo Compiling the game (UTF-8 encoding, Java 25)...

REM Compile the game with UTF-8 encoding and Java 25 compatibility
javac -encoding UTF-8 --release 25 -d bin -sourcepath src ^
    src\CavesOfChaos.java src\core\*.java src\player\*.java src\ui\*.java

REM Check if compilation was successful
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed. Please fix errors and try again.
    pause
    goto :eof
)

echo.
echo [INFO] Compilation successful!

REM Copy configuration and asset files to bin directory
if not exist bin\assets mkdir bin\assets
if not exist bin\config mkdir bin\config
xcopy /Y /Q /E src\assets bin\assets\ >nul
xcopy /Y /Q src\config\*.xml bin\config\ >nul

echo.
echo [INFO] Running the game...

REM Run the game with wizard class by default or use command-line arguments
if "%~1"=="" (
    echo Running with default wizard class...
    java -cp bin CavesOfChaos wizard Player1
) else (
    java -cp bin CavesOfChaos %*
)

echo.
pause
