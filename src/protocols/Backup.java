package protocols;

import backupService.Message;
import backupService.MessageBody;
import backupService.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Backup extends Thread {

    private Peer parentPeer;
    private Message request;

    public Backup(Peer parentPeer, Message request) throws IOException {
        this.parentPeer = parentPeer;
        this.request = request;

        System.out.println("Backup Thread is ready.");
    }

    private void BackupChunk() throws IOException, InterruptedException {
        MessageBody body = request.getBody();
        MessageHeader header = request.getHeader();

        String version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();
        int replicationDegree = header.getReplicationDegree();

        byte[] chunkData = body.getBody();

        // Writes to file.
        FileOutputStream fileOutputStream = new FileOutputStream("../");
        fileOutputStream.write(chunkData);

        // Creates the message to send back to the initiator peer.
        MessageHeader response = new MessageHeader(Utils.MessageType.STORED, version, senderId, fileId, chunkNo);
        byte[] responseBytes = response.getMessageHeaderAsString().getBytes();

        Random random = new Random();
        TimeUnit.MILLISECONDS.sleep(random.nextInt(401));

        parentPeer.getMc().sendMessage(responseBytes);
    }

    @Override
    public void run() {
        try {
            BackupChunk();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Qual é a necessidade disto?
        /*while(true) {
            try {
                multicastSocket.receive(datagramPacket);
                String msgReceived =  new String(datagramPacket.getData());
                BackupChunk(msgReceived);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
}
