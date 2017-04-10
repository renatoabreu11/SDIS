#!/bin/sh
rm -rf bin
rm -f *.jar
mkdir -p bin
javac -Xlint:unchecked -d bin -sourcepath src src/network/TestApp.java src/network/Peer.java src/network/Client.java

mkdir -p bin/data
mkdir -p bin/data/chunks
cat <<EOF >bin/data/chunks/metadata.txt
EOF
cat <<EOF >bin/data/Protocol.log

EOF
cat <<EOF >bin/data/Multicast.log

EOF
cat <<EOF >bin/data/Dispatcher.log

EOF
mkdir -p bin/META-INF
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
