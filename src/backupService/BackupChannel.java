package backupService;

import network.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;

public class BackupChannel extends Channel {
    public BackupChannel(String mcAddress, String mcPort, Peer parentPeer) throws UnknownHostException, IOException {
        super(mcAddress, mcPort, parentPeer);
    }

    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[HEADER_SIZE + BODY_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            try {
                this.getSocket().receive(dgp);
                String message = new String(dgp.getData(), 0, dgp.getLength());
                System.out.println("MDB message: " + message);
                //Send message to peer where it will be parsed or to a message dispatcher thread maybe? It could use a queue, if the number of messages received is to high
                // This new function/thread will create the respective thread to backup/restore/whatever.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
