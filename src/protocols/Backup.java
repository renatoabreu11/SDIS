package protocols;

import utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Backup extends Thread {

    private String mdbAddress;
    private int mdbPort;

    private InetAddress inetAddress;
    private MulticastSocket multicastSocket;
    private DatagramPacket datagramPacket;
    private byte[] buf;

    public Backup(String mdbAddress, int mdbPort) throws IOException {
        this.mdbAddress = mdbAddress;
        this.mdbPort = mdbPort;

        inetAddress = InetAddress.getByName(mdbAddress);
        multicastSocket = new MulticastSocket(mdbPort);
        buf = new byte[64000];
        datagramPacket = new DatagramPacket(buf, buf.length);

        System.out.println("Backup Thread is ready.");
    }

    private void BackupChunk(String msgReceived) throws IOException, InterruptedException {
        String[] msgSplit = msgReceived.split("\\s+", 6);

        String type = msgSplit[0];
        if(!type.equals("PUTCHUNK"))
            return;
        String version = msgSplit[1];
        String senderId = msgSplit[2];
        String fileId = msgSplit[3];
        String chunkNo = msgSplit[4];
        String replicationDegree = msgSplit[5];

        // Writes to file.
        FileOutputStream fileOutputStream = new FileOutputStream("../");
        fileOutputStream.write(msgSplit[6].getBytes());

        // Creates the message to send back to the initiator peer.
        String msgReply = "STORED " + version + " " + senderId + " " + fileId + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;

        Random random = new Random();
        TimeUnit.MILLISECONDS.sleep(random.nextInt(401));

        // Need to send message back.
    }

    @Override
    public void run() {
        while(true) {
            try {
                multicastSocket.receive(datagramPacket);
                String msgReceived =  new String(datagramPacket.getData());
                BackupChunk(msgReceived);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
