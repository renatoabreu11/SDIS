package protocols;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import protocols.initiator.BackupInitiator;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

        Chunk chunk = parentPeer.getManager().getChunk(fileId, chunkNo, parentPeer.getId());

        // If this peer doesn't have the chunk, it simply returns.
        if(chunk == null) {
            System.out.println("Peer" + parentPeer.getId() + " does not have chunk number " + chunkNo);
            return;
        }

        chunk.removePeer(senderId);
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

                System.out.println("**********This peer is starting backup protocol based on manage!**********");

                // Start backup protocol.
                _File storedFile = parentPeer.getManager().getStorage().get(fileId);
                String chunkPathname = storedFile.getPathname() + chunkNo;

                System.out.println("Pathname: " + chunkPathname);

                String pathname = Utils.CHUNKS_DIR + chunk.getFileId() + chunk.getChunkNo();
                Path path = Paths.get(pathname);
                byte[] fileData = Files.readAllBytes(path);

                parentPeer.BackupFile(fileData, fileId, 1);
            } catch (InterruptedException | IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}
