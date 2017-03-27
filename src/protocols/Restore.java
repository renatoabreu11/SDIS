package protocols;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
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
        int senderId = request.getHeader().getSenderId();
        String fileId = request.getHeader().getFileId();
        int chunkNo = request.getHeader().getChunkNo();

        _File file = parentPeer.getManager().getFileStorage(fileId);
        if(file == null)
            return;

        ArrayList<Chunk> chunks = file.getChunks();
        for(int i = 0; i < chunks.size(); i++) {
            if(chunks.get(i).getChunkNo() == chunkNo) {
                MessageHeader header = new MessageHeader(Utils.MessageType.CHUNK, version, senderId, fileId, chunkNo);
                //ir ao ficheiro de backup e ler
                //alterar isto
                MessageBody body = new MessageBody(null);
                Message message = new Message(header, body);
                try {
                    byte[] buffer = message.getMessageBytes();

                    TimeUnit.MILLISECONDS.sleep(new Random().nextInt(401));
                    SendMessage(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void SendMessage(byte[] buffer) {
        if(parentPeer.isCanSendRestoreMessages())
            parentPeer.getMdr().sendMessage(buffer);
    }
}
