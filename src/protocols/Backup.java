package protocols;

import fileSystem.Chunk;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Backup implements Runnable {

    private Peer parentPeer;
    private Message request;

    public Backup(Peer parentPeer, Message request) throws IOException {
        this.parentPeer = parentPeer;
        this.request = request;

        System.out.println("Starting backup.");
    }

    @Override
    public void run() {
        MessageHeader header = request.getHeader();
        MessageBody body = request.getBody();

        String version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();
        int replicationDegree = header.getReplicationDegree();

        byte[] chunkData = body.getBody();
        Chunk chunk = new Chunk(replicationDegree, chunkNo, chunkData, fileId);

        // Only keeps the chunk if there's available space.
        long futureOccupiedSpace = 0;
        try {
            futureOccupiedSpace = chunkData.length + parentPeer.getManager().getCurrOccupiedSize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(futureOccupiedSpace > parentPeer.getMaxDiskSpace() * 1000) {
            System.out.println("WARNING: Peer discarded a chunk because it had no available space to host it.");
            return;
        }

        // Manage disk space related.
        parentPeer.addChunkBackingUp(chunk);

        // Saves the chunk's info in the file manager.
        if(!parentPeer.getManager().addChunkToStorage(fileId, chunk)) {
            // Manage disk space related.
            parentPeer.removeChunkBackingUp(chunk);
            System.out.println("Now enough disk space. Aborting.");
            return;
        }

        chunk.updateReplication(senderId);

        // Writes to file.
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("data/" + fileId + chunkNo);
            fileOutputStream.write(chunkData);
            fileOutputStream.close();

            // Creates the message to send back to the initiator peer.
            MessageHeader response = new MessageHeader(Utils.MessageType.STORED, version, parentPeer.getId(), fileId, chunkNo);
            byte[] responseBytes = response.getMessageHeaderAsString().getBytes();

            // Waits a random delay (in order for the message to be able to arrive via MC without conflicts with other peers).
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(401));

            // Sends the message to the initiator peer.
            parentPeer.getMc().sendMessage(responseBytes);

            // Updates peer's metadata.
            parentPeer.getManager().WriteMetadata();
            // Manage disk space related.
            parentPeer.removeChunkBackingUp(chunk);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
