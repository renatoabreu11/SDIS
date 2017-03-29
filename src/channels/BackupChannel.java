package channels;

import messageSystem.Message;
import network.Peer;
import protocols.ProtocolDispatcher;
import sun.rmi.server.Dispatcher;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;

public class BackupChannel extends Channel {

    /**
     * Backup channel constructor - Joins the multicast group defined in the parameters
     * @param mcAddress
     * @param mcPort
     * @param parentPeer
     * @throws UnknownHostException
     * @throws IOException
     */
    public BackupChannel(String mcAddress, String mcPort, Peer parentPeer) throws UnknownHostException, IOException {
        super(mcAddress, mcPort, parentPeer);
        System.out.println("Backup channel online.");
    }

    /**
     * This thread, while running, reads the messages sent by other peers to the multicast channel, sending them afterwards to a dispatcher.
     */
    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[HEADER_SIZE + BODY_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            try {
                this.getSocket().receive(dgp);
                String message = new String(dgp.getData());
                System.out.println("MDB message: " + message);

                this.getParentPeer().addMessageToDispatcher(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
