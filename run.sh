#!/bin/bash
# WatchyoJet — launch script (macOS / Linux)
# Requires: Java 21+ and Maven 3.8+ on PATH

if ! command -v java &>/dev/null; then
    echo "ERROR: Java not found. Install Java 21 from https://adoptium.net"
    exit 1
fi

if ! command -v mvn &>/dev/null; then
    echo "ERROR: Maven not found. Install Maven from https://maven.apache.org/download.cgi"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [ "$JAVA_VER" -lt 21 ] 2>/dev/null; then
    echo "ERROR: Java 21 or higher required (found Java $JAVA_VER). Install from https://adoptium.net"
    exit 1
fi

echo "Starting WatchyoJet..."
mvn javafx:run
