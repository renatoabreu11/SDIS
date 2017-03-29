package protocols;

import fileSystem.Chunk;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
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

                // If this peer has already received the message to backup this exact chunk,
                // we don't send the message.
                if(parentPeer.getChunkBackingUp().contains(chunk))
                    return;

                header = new MessageHeader(Utils.MessageType.PUTCHUNK, version, senderId, fileId, chunkNo, chunk.getReplicationDegree());
                MessageBody body = new MessageBody(chunk.getChunkData());
                Message message = new Message(header, body);

                byte[] buffer = message.getMessageBytes();
                parentPeer.sendMessageMDB(buffer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
