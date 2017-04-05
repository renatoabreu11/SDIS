#!/bin/bash
echo -n "Peer identifier\n> "
read peer_id
echo -n "Peer version\n> "
read version

java -jar Peer.jar $version $peer_id IClientPeer 224.0.0.69:4445 224.0.0.69:4446 224.0.0.69:4447
