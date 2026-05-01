@echo off
REM WatchyoJet — launch script (Windows)
REM Requires: Java 21+ and Maven 3.8+ on PATH

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Install Java 21 from https://adoptium.net
    pause
    exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven not found. Install Maven from https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Starting WatchyoJet...
mvn javafx:run
pause
