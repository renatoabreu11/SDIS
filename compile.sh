#!/bin/sh
mkdir -p bin
javac -Xlint:unchecked -d bin -sourcepath src src/network/TestApp.java src/network/Peer.java src/network/Client.java
