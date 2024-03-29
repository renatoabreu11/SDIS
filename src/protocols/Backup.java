package protocols;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
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

        if(senderId == parentPeer.getId()) // a peer never stores the chunks of it own files
            return;

        byte[] chunkData = body.getBody();
        Chunk chunk = new Chunk(replicationDegree, chunkNo, fileId);

        // Only keeps the chunk if there's available space.
        long futureOccupiedSpace = chunkData.length + parentPeer.getDiskUsage();
        if(futureOccupiedSpace > parentPeer.getMaxDiskSpace() * 1000) {
            long spaceNeeded = (futureOccupiedSpace / 1000) - parentPeer.getMaxDiskSpace();
            boolean hasSpace = parentPeer.freeDisposableSpace(spaceNeeded);
            if(!hasSpace){
                System.out.println("WARNING: Peer discarded a chunk because it had no available space to host it.");
                return;
            }
        }

        // Manage disk space related.
        parentPeer.addChunkBackingUp(chunk);

        // Saves the chunk's info in the file manager.
        boolean isStored = parentPeer.getManager().checkStoredChunk(fileId, chunk, parentPeer.getId());
        if(isStored){
            // Manage disk space related.
            parentPeer.removeChunkBackingUp(chunk);
            System.out.println("Chunk discarded because it is already stored.");
            try {
                SendStoredMessage(chunk, version, fileId, chunkNo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            // Waits a random delay (in order for the message to be able to arrive via MC without conflicts with other peers).
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(401));

            if(version.equals(Utils.ENHANCEMENT_BACKUP) || version.equals(Utils.ENHANCEMENT_ALL)) {
                _File file = parentPeer.getManager().getFileStorage(fileId);
                if(file != null) {
                    Chunk chunkStored = file.getChunk(chunkNo);
                    if(chunkStored != null && chunkStored.getCurrReplicationDegree() >= replicationDegree) {
                        parentPeer.removeChunkBackingUp(chunk);
                        return;
                    }
                }
            }

            parentPeer.getManager().storeChunk(fileId, chunk, parentPeer.getId(), chunkData);
            SendStoredMessage(chunk, version, fileId, chunkNo);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        parentPeer.removeChunkBackingUp(chunk);
    }

    public void SendStoredMessage(Chunk chunk, String version, String fileId, int chunkNo) throws IOException {
        // Creates the message to send back to the initiator peer.
        MessageHeader response = new MessageHeader(Utils.MessageType.STORED, version, parentPeer.getId(), fileId, chunkNo);
        byte[] responseBytes = response.getMessageHeaderAsString().getBytes();
        // Sends the message to the initiator peer.
        parentPeer.getMc().sendMessage(responseBytes);

        // Updates peer's metadata.
        parentPeer.getManager().WriteMetadata();
        // Manage disk space related.
        parentPeer.removeChunkBackingUp(chunk);
    }
}
