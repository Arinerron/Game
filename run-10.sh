#!/bin/sh

set -e

sh clean.sh

mkdir bin

cd src
/usr/lib/jvm/java-10-openjdk/bin/javac -d ../bin/ game/Game.java
cd ../bin
/usr/lib/jvm/java-10-openjdk/bin/java game/Game $@
cd ..
