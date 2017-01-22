#!/bin/sh

set -e

sh clean.sh

mkdir bin

cd src
javac -d ../bin/ Main.java
cd ../bin
java Main $@
cd ..
