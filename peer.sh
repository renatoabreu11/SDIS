#!/bin/bash
echo -n "Peer identifier\n> "
read peer_id
echo -n "Peer version\n> "
read version
echo -n "Interface available?\n> "
read useInterface

java -jar Peer.jar $version $peer_id IClientPeer 224.0.0.69:4448 224.0.0.69:4449 224.0.0.69:4450 $useInterface