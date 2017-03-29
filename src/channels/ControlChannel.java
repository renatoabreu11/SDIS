package channels;

import messageSystem.Message;
import network.Peer;
import protocols.ProtocolDispatcher;

import java.io.IOException;
import java.net.*;

public class ControlChannel extends Channel {

    /**
     * Control channel constructor - Joins the multicast group defined in the parameters
     * @param mcAddress
     * @param mcPort
     * @param parentPeer
     * @throws IOException
     */
    public ControlChannel(String mcAddress, String mcPort, Peer parentPeer) throws IOException {
        super(mcAddress, mcPort, parentPeer);
        System.out.println("Control channel online.");
    }

    /**
     * This thread, while running, reads the messages sent by other peers to the multicast channel, sending them afterwards to a dispatcher.
     */
    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[HEADER_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            try {
                this.getSocket().receive(dgp);
                String message = new String(dgp.getData());
                System.out.println("MC message: " + message);

                this.getParentPeer().addMessageToDispatcher(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
