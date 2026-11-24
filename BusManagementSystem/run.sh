#!/bin/sh
cd "$(dirname "$0")"
mkdir -p out
javac -d out src/bms/*.java && java -cp out bms.Main "$@"
