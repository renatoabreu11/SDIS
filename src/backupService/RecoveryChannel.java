package backupService;

import network.Peer;
import protocols.ProtocolDispatcher;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;

public class RecoveryChannel extends Channel{
    public RecoveryChannel(String mcAddress, String mcPort,  Peer parentPeer) throws UnknownHostException, IOException {
        super(mcAddress, mcPort, parentPeer);
    }

    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[HEADER_SIZE + BODY_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            String message = new String(dgp.getData(), 0, dgp.getLength());
            System.out.println("MDR message: " + message);
            ProtocolDispatcher dispatcher = new ProtocolDispatcher(message);
            dispatcher.dispatchRequest(getParentPeer());
            try {
                this.getSocket().receive(dgp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
