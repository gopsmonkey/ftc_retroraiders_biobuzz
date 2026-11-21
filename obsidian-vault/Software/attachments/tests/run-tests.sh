#!/bin/bash
# Laptop tests for ShooterController's intake gate (no robot needed).
# Usage: JAVA_HOME=/path/to/jdk17 ./run-tests.sh <folder with ShooterConfig.java + ShooterController.java>
# GateTest uses the real ShooterConfig; PulseTest uses a copy with FEED_TICKS_PER_BALL = 100.
set -e
SRC=$(cd "$1" && pwd)
HERE=$(cd "$(dirname "$0")" && pwd)
T=${TMPDIR:-/tmp}/onbot-check
bash "$HERE/../onbot-compile-check.sh" "$SRC" 12.0.0 > /dev/null   # downloads the SDK jars on first run
[ -f "$T/threetenbp.jar" ] || curl -sfL -o "$T/threetenbp.jar" https://repo1.maven.org/maven2/org/threeten/threetenbp/1.6.8/threetenbp-1.6.8.jar
L=$T/sdk-12.0.0
CP=$T/threetenbp.jar:$T/android.jar:$L/RobotCore.jar:$L/Hardware.jar

run() {  # $1 = test class, $2 = sed rule for ShooterConfig (or empty)
  W=$T/test-$1; rm -rf "$W"; mkdir -p "$W/src" "$W/out"
  cp "$SRC/ShooterController.java" "$W/src/"
  if [ -n "$2" ]; then sed "$2" "$SRC/ShooterConfig.java" > "$W/src/ShooterConfig.java"; else cp "$SRC/ShooterConfig.java" "$W/src/"; fi
  "$JAVA_HOME/bin/javac" -nowarn -d "$W/out" -cp "$CP" "$W"/src/*.java "$HERE/$1.java" 2>&1 | grep -v "^Note:" || true
  echo "=== $1 ==="; "$JAVA_HOME/bin/java" -cp "$W/out:$CP" org.firstinspires.ftc.teamcode.$1
}
run GateTest ""
run PulseTest 's/FEED_TICKS_PER_BALL = [0-9]*;/FEED_TICKS_PER_BALL = 100;/'
