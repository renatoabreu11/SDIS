package protocols;

import fileSystem.Chunk;
import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Manage implements Runnable {

    private Peer parentPeer;
    private Message request;

    public Manage(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;
        System.out.println("Starting Managing Disk Space.");
    }

    @Override
    public void run() {
        MessageHeader header = request.getHeader();

        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();

        Chunk chunk = parentPeer.getManager().getChunk(fileId, chunkNo);
        chunk.removePeer(senderId);

        // If this peer doesn't have the chunk, it simply updates the current RD and returns.
        if(!chunk.peerHasChunk(parentPeer.getId())) {
            //System.out.println("Peer " + parentPeer.getId() + " does not have chunk number " + chunkNo);
            return;
        }

        try {
            parentPeer.getManager().WriteMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If the new replication degree is less than the desired, we need to get it back to that number.
        if(chunk.getCurrReplicationDegree() < chunk.getReplicationDegree()) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(401));

                // If this peer has already received the message to backup this exact chunk,
                // we don't send the message.
                if(parentPeer.getChunkBackingUp().contains(chunk.getFileId() + chunk.getChunkNo()))
                    return;

                // Start backup protocol.
                String pathname = Utils.CHUNKS_DIR + chunk.getFileId() + chunk.getChunkNo();
                Path path = Paths.get(pathname);
                byte[] fileData = Files.readAllBytes(path);

                parentPeer.BackupFile(fileData, chunk);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
