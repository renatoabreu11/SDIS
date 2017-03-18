package backupService;

import java.io.IOException;
import java.net.*;

public class ControlChannel extends Channel {

    public ControlChannel(String mcAddress, String mcPort) throws UnknownHostException, IOException {
        super(mcAddress, mcPort);
    }

    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[HEADER_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            try {
                this.getSocket().receive(dgp);
                String message = new String(dgp.getData(), 0, dgp.getLength());
                System.out.println("MC message: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
