#!/bin/sh

BIN_DIR="bin"
SRC_DIR="src"

rm -rf "$BIN_DIR"
mkdir -p "$BIN_DIR"

cd "$SRC_DIR"

javac -d "../$BIN_DIR" game/Game.java

