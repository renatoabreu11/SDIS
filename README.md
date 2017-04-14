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
2. Run the "peer.sh" where it's possible to choose the peer's id, version and if it has an interface or not. (The interface is selected by the "true" or "false" tokens)
3. On the other hand, the following command: "java -jar Client.jar <server_address:remote_interface>" allows a communication between a client and the initiator peer.

## Team 
[José Carlos](https://github.com/Evenilink)

[Renato Abreu](https://github.com/renatoabreu11)
