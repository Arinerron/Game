#!/bin/sh

set -e

sh clean.sh

mkdir bin

cd src
javac -d ../bin/ game/Game.java
cd ../bin
java game/Game $@
cd ..
