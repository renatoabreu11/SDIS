#!/bin/bash
echo Peer id?
read peer_id
echo Version?
read version

java -jar bin/Peer.jar version 1 IClientPeer 224.0.0.69:4445 224.0.0.69:4446 224.0.0.69:4447
