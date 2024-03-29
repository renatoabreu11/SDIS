package channels;

import messageSystem.Message;
import network.Peer;
import protocols.ProtocolDispatcher;
import utils.Utils;

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
    public ControlChannel(String mcAddress, String mcPort, Peer parentPeer, boolean logSystem) throws IOException {
        super(mcAddress, mcPort, parentPeer, logSystem);
        System.out.println("Control channel online.");
    }

    /**
     * This thread, while running, reads the messages sent by other peers to the multicast channel, sending them afterwards to a dispatcher.
     */
    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[Utils.HEADER_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            try {
                this.getSocket().receive(dgp);
                String message = new String(dgp.getData(), 0, dgp.getLength());
                logMessage("MC Message\n");
                logMessage(message);
                this.getParentPeer().addMessageToDispatcher(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
