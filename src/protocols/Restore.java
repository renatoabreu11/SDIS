package protocols;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import protocols.initiator.RestoreInitiator;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Restore implements Runnable {

    private Peer parentPeer;
    private Message request;

    public Restore(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        System.out.println("Starting restore.");
    }

    @Override
    public void run() {
        String version = request.getHeader().getVersion();
        String fileId = request.getHeader().getFileId();
        int chunkNo = request.getHeader().getChunkNo();

        _File file = parentPeer.getManager().getFileStorage(fileId);
        if(file == null)
            return;

        ArrayList<Chunk> chunks = file.getStoredChunks(parentPeer.getId());
        for(int i = 0; i < chunks.size(); i++) {
            if(chunks.get(i).getChunkNo() == chunkNo) {
                MessageHeader header = new MessageHeader(Utils.MessageType.CHUNK, version, parentPeer.getId(), fileId, chunkNo);
                Path path = Paths.get("data/chunks/" + fileId + chunkNo);
                byte[] data = new byte[0];
                try {
                    data = Files.readAllBytes(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MessageBody body = new MessageBody(data);
                Message message = new Message(header, body);
                try {
                    byte[] buffer = message.getMessageBytes();

                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(401));

                    if(parentPeer.getChunkRestoring().contains(fileId + chunkNo))
                        return;

                    parentPeer.sendMessageMDR(buffer);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
