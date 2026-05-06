#!/bin/bash
# ============================================================
#  Chess Game - Build Script
#  Usage: ./build.sh
# ============================================================

SRC_DIR="src"
OUT_DIR="out"
MAIN_CLASS="Main"

echo "Compiling Chess Game..."
mkdir -p "$OUT_DIR"

javac -d "$OUT_DIR" -sourcepath "$SRC_DIR" $(find "$SRC_DIR" -name "*.java")

if [ $? -eq 0 ]; then
    echo "Build successful! Run with: java -cp out Main"
else
    echo "Build failed. Check errors above."
    exit 1
fi
