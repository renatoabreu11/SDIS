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

        String version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();

        Chunk chunk = parentPeer.getManager().getChunk(fileId, chunkNo);

        // If this peer doesn't have the chunk, it simply returns.
        if(chunk == null)
            return;

        chunk.subReplicationDegree();

        // If the new replication degree is less than the desired, we need to get it back to that number.
        if(chunk.getCurrReplicationDegree() < chunk.getReplicationDegree()) {
            try {
                TimeUnit.MILLISECONDS.sleep(new Random().nextInt(401));

                if(parentPeer.getChunkBackingUp().contains(chunk.getFileId() + chunk.getChunkNo()))
                    return;

                // If this peer has already received the message to backup this exact chunk,
                // we don't send the message.
                if(parentPeer.getChunkBackingUp().contains(chunk.getFileId() + chunk.getChunkNo()))
                    return;

                // Start backup protocol.
                _File storedFile = parentPeer.getManager().getStorage().get(fileId);
                String chunkPathname = storedFile.getPathname() + chunkNo;
                Path path = Paths.get(chunkPathname);
                byte[] fileData = Files.readAllBytes(path);

                parentPeer.BackupFile(fileData, chunkPathname, 1);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}
