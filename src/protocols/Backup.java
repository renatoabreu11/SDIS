package protocols;

import fileSystem.Splitter;
import utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public void SendMsg(String pathname, int replicationDegree, String protocolVersion, int id) throws IOException, NoSuchAlgorithmException {
        String lastModified = Long.toString(new File(pathname).lastModified());

        // Hashing the file id.
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String fileId = pathname + lastModified;
        md.update(fileId.getBytes("UTF-8"));
        byte[] fileIdHashed = md.digest();

        // Splitting the file into chunks.
        Splitter splitter = new Splitter(pathname);
        splitter.splitFile(replicationDegree);

        String msgToSend;
        for(int i = 0; i < splitter.getChunks().size(); i++) {
            msgToSend = "PUTCHUNK " + protocolVersion + " " + id + " " + fileIdHashed + " " + (i+1) + " " + replicationDegree + Utils.CRLF + Utils.CRLF + splitter.getChunks().get(i).getChunkData();
            buf = msgToSend.getBytes();
            datagramPacket = new DatagramPacket(buf, buf.length, inetAddress, mdbPort);
            System.out.println("Sending message to MDB Channel.");
            multicastSocket.send(datagramPacket);
        }
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
