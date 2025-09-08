@echo off
REM Batch file to compile and run the Caves of Chaos game

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

echo Compiling the game...
REM Compile the game with all necessary dependencies
javac -d bin -sourcepath src src\CavesOfChaos.java src\core\*.java src\player\*.java src\ui\*.java

REM Check if compilation was successful
if %errorlevel% neq 0 (
    echo Compilation failed. Please fix errors and try again.
    goto :eof
)

echo Compilation successful!

REM Copy configuration and asset files to bin directory to ensure they're accessible
if not exist bin\config mkdir bin\config
if not exist bin\assets mkdir bin\assets
xcopy /Y /Q /E src\config bin\config\
xcopy /Y /Q /E src\assets bin\assets\

echo Running the game...
REM Run the game with wizard class by default or use command-line arguments
if "%~1"=="" (
    echo Running with default wizard class...
    java -cp bin CavesOfChaos wizard Player1
) else (
    java -cp bin CavesOfChaos %*
)
