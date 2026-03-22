#!/bin/bash
# Swing Ant Design Launcher (Linux / macOS)
DIR="$(cd "$(dirname "$0")/.." && pwd)"
java -jar "$DIR/lib/swing-ant-design-1.0.0-SNAPSHOT.jar" "$@"
