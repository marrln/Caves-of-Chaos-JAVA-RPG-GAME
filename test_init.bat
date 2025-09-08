@echo off
REM Simple test script to verify the game initialization

echo Running CavesOfChaos initialization test...

REM Create test directory if it doesn't exist
if not exist test mkdir test

REM Compile test class
echo Compiling test classes...
javac -d test -cp src src\core\GameDebugger.java src\core\Config.java

REM Create test file
echo Creating test file...
echo public class GameInitTest { > test\GameInitTest.java
echo     public static void main(String[] args) { >> test\GameInitTest.java
echo         core.GameDebugger.printSystemInfo(); >> test\GameInitTest.java
echo         core.GameDebugger.log("TEST", "Testing config file paths..."); >> test\GameInitTest.java
echo         core.GameDebugger.checkFileExists("bin/config/settings.xml"); >> test\GameInitTest.java
echo         core.GameDebugger.checkFileExists("src/config/settings.xml"); >> test\GameInitTest.java
echo         core.GameDebugger.checkFileExists("bin/config/assets.xml"); >> test\GameInitTest.java
echo         core.GameDebugger.checkFileExists("src/config/assets.xml"); >> test\GameInitTest.java
echo         core.GameDebugger.log("TEST", "Test complete"); >> test\GameInitTest.java
echo     } >> test\GameInitTest.java
echo } >> test\GameInitTest.java

REM Compile and run test
echo Compiling test...
javac -cp test;src test\GameInitTest.java

echo Running test...
java -cp test;src GameInitTest

echo Test complete. Check the output for any issues.
pause
