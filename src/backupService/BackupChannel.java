package backupService;

import network.Peer;
import protocols.ProtocolDispatcher;

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
                // Usar dispatcher como thread? para não perder tempo aqui
                ProtocolDispatcher dispatcher = new ProtocolDispatcher(message);
                dispatcher.dispatchRequest(getParentPeer());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
