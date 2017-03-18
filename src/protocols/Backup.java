package protocols;

import backupService.Message;
import backupService.MessageBody;
import backupService.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.FileNotFoundException;
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

        System.out.println("Starting backup.");
    }

    @Override
    public void run() {
        MessageBody body = request.getBody();
        MessageHeader header = request.getHeader();

        String version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();
        int replicationDegree = header.getReplicationDegree();

        byte[] chunkData = body.getBody();

        // Writes to file.
        FileOutputStream fileOutputStream = null;
        try {
            // We need to save the file extension!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            fileOutputStream = new FileOutputStream(fileId + ".txt");
            fileOutputStream.write(chunkData);

            // Creates the message to send back to the initiator peer.
            MessageHeader response = new MessageHeader(Utils.MessageType.STORED, version, senderId, fileId, chunkNo);
            byte[] responseBytes = response.getMessageHeaderAsString().getBytes();

            // Waits a random delay (in order for the message to be able to arrive via MC without conflicts with other peers).
            Random random = new Random();
            TimeUnit.MILLISECONDS.sleep(random.nextInt(401));

            // Sends the message to the initiator peer.
            parentPeer.getMc().sendMessage(responseBytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
