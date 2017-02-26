import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Evenilink on 24/02/2017.
 */
public class Client {

    public static void main(String args[]) throws IOException {
        if(args.length != 4) {
            System.out.println("Usage: java client <mcast_addr> <mcast_port> <oper> <opnd> *");
            return;
        }

        System.out.println("Waiting for server's advertisement.\n");

        String mcast_addr = args[0];
        int mcast_port = Integer.parseInt(args[1]);

        byte[] buf = new byte[256];

        InetAddress address = InetAddress.getByName(mcast_addr);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        MulticastSocket clientSocket = new MulticastSocket(mcast_port);
        clientSocket.joinGroup(address);

        // Client waits for the server's advertisement
        clientSocket.receive(packet);

        String msgAdvertisement = new String(packet.getData());
        String[] msgSplit = msgAdvertisement.split(" ");
        System.out.println("Received server's advertisement: \"" + msgAdvertisement + "\"");

        String srvc_addr = msgSplit[2];
        int srvc_port = 4446;//Integer.parseInt(msgSplit[3]);

        String msgToSend = args[2] + " " + args[3];
        buf = msgToSend.getBytes();
        System.out.println("Sending message to server: \"" + msgToSend + "\"\n");

        InetAddress inetAddressServer = InetAddress.getByName(srvc_addr);
        packet = new DatagramPacket(buf, buf.length, inetAddressServer, srvc_port);
        clientSocket.send(packet);

        buf = new byte[512];
        packet = new DatagramPacket(buf, buf.length);
        clientSocket.receive(packet);

        String msgServerReply = new String(packet.getData());
        System.out.println("Received server's reply: \"" + msgServerReply + "\"");

        clientSocket.leaveGroup(address);
        clientSocket.close();
    }
}
