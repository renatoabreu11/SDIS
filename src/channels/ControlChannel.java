package channels;

import network.Peer;

import java.io.IOException;
import java.net.*;

public class ControlChannel extends Channel {

    public ControlChannel(String mcAddress, String mcPort, Peer parentPeer) throws IOException {
        super(mcAddress, mcPort, parentPeer);
        System.out.println("Control channel online.");
    }

    @Override
    public void run() {

        while (true) {
            byte[] buffer = new byte[HEADER_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            try {
                this.getSocket().receive(dgp);
                String message = new String(dgp.getData());
                System.out.println("MC message: " + message);

                // Initiator peer received the reply message.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
