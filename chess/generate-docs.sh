#!/bin/bash
# ============================================================
#  Chess Game - Javadoc Generation Script
#  Usage: ./generate-docs.sh
#  Output: docs/index.html
# ============================================================

SRC_DIR="src"
DOC_DIR="docs"

echo "Generating Javadoc..."
mkdir -p "$DOC_DIR"

javadoc -d "$DOC_DIR" \
        -sourcepath "$SRC_DIR" \
        -subpackages pieces:board:player:game:utils \
        -windowtitle "Console Chess Game" \
        -doctitle "Console Chess Game - Phase 1" \
        -author \
        -version

if [ $? -eq 0 ]; then
    echo "Javadoc generated successfully! Open docs/index.html in a browser."
else
    echo "Javadoc generation failed."
    exit 1
fi
