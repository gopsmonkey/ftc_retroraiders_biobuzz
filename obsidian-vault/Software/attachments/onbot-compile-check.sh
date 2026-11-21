#!/bin/bash
# Compile a folder of OnBot Java files against the real FTC SDK jars at Java 8.
# Usage: JAVA_HOME=/path/to/jdk17 ./onbot-compile-check.sh <folder-with-.java> 12.0.0 11.2.1 ...
# Needs internet the first time (downloads SDK .aar files from Maven Central).
SRC=$1; shift
T=${TMPDIR:-/tmp}/onbot-check; mkdir -p "$T"
M=https://repo1.maven.org/maven2
[ -f "$T/android.jar" ] || curl -sfL -o "$T/android.jar" $M/com/google/android/android/4.1.1.4/android-4.1.1.4.jar
for V in "$@"; do
  L=$T/sdk-$V; mkdir -p "$L"; CP=$T/android.jar
  for A in RobotCore Hardware FtcCommon RobotServer Inspection Vision; do
    [ -f "$L/$A.jar" ] || { curl -sfL -o "$L/$A.aar" $M/org/firstinspires/ftc/$A/$V/$A-$V.aar && unzip -p "$L/$A.aar" classes.jar > "$L/$A.jar"; }
    [ -s "$L/$A.jar" ] && CP=$CP:$L/$A.jar
  done
  rm -rf "$T/out-$V"; mkdir -p "$T/out-$V"
  echo "=== SDK $V ==="
  "$JAVA_HOME/bin/javac" --release 8 -Xlint:all -nowarn -d "$T/out-$V" -cp "$CP" "$SRC"/*.java 2>&1 | grep -v "^Note:\|bootstrap\|value 8\|To suppress"
  echo "exit=${PIPESTATUS[0]}"
done
