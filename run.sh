#!/bin/sh

set -e

sh clean.sh

mkdir bin

cd src
javac -d ../bin/ Game.java
cd ../bin
java Game $@
cd ..
