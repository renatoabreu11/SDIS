# SDIS
Repository to host SDIS first project

SDIS is a course given at FEUP in the third year of the Master in Informatics and Computing Engineering.
 
## Goal
The goal of this project was to implement a distributed backup service for a local area network (LAN). The idea is to use the free disk space of the computers in a LAN for backing up files in other computers in the same LAN. The service is provided by servers in an environment that is assumed cooperative (rather than hostile). Nevertheless, each server retains control over its own disks and, if needed, may reclaim the space it made available for backing up other computers' files.

[Specification](https://github.com/renatoabreu11/SDIS/blob/master/docs/Distributed%20Backup%20Service.pdf)

## Features

## Technologies
* Java as programming language.
* MCastSnopper to analyse the messages sent through the multicast channels.

## How To Run 
1. Run the script in the shell -> sh compile.sh 
2. Change directory to the bin folder 
3. Run one of the jars that was created by the script
- java -jar Peer.jar protocol_version server_id service_access_point mc:port mdb:port mdl:port
- java -jar Client.jar
- java -jar TestApp.jar peer_ap sub_protocol opnd_1 opnd_2

## Team 
[José Carlos](https://github.com/Evenilink)

[Renato Abreu](https://github.com/renatoabreu11)
