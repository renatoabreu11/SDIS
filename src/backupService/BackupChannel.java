package backupService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;

public class BackupChannel extends Channel {
    public BackupChannel(String mcAddress, String mcPort) throws UnknownHostException, IOException {
        super(mcAddress, mcPort);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
