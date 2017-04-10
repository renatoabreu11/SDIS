#!/bin/sh
rm -rf bin
rm -f *.jar
mkdir -p bin
javac -Xlint:unchecked -d bin -sourcepath src src/network/TestApp.java src/network/Peer.java src/network/Client.java

mkdir -p bin/data
mkdir -p bin/data/chunks
mkdir -p bin/META-INF
touch bin/data/chunks/metadata.txt
touch bin/data/Protocol.log
touch bin/data/peers_to_delete.txt
touch bin/data/Multicast.log
touch bin/data/Dispatcher.log

cat <<EOF >bin/META-INF/Peer.mf
Main-Class: network.Peer

EOF

cat <<EOF >bin/META-INF/TestApp.mf
Main-Class: network.TestApp

EOF

cat <<EOF >bin/META-INF/Client.mf
Main-Class: network.Client

EOF

jar cfm TestApp.jar bin/META-INF/TestApp.mf -C bin .
jar cfm Client.jar bin/META-INF/Client.mf -C bin .
jar cfm Peer.jar bin/META-INF/Peer.mf -C bin .
