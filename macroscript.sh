#!/bin/sh
# Starts MacroScript IDE on Linux and macOS. Build it first with: ./mvnw package
# Arguments are passed on, for example: ./macroscript.sh compile examples
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java -jar "$DIR/target/macroscript-ide-1.0.0.jar" "$@"
