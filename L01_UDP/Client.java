import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Evenilink on 17/02/2017.
 */
public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Usage: java Client <host_name> <port_number> <oper> <opnd>");
            return;
        }

        // send request
        String infoToSend = args[2] + " " + args[3];
        byte[] sbuf;        // countains the information to send
        sbuf = infoToSend.getBytes();

        int port = Integer.parseInt(args[1]);
        InetAddress address = InetAddress.getByName(args[0]);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);

        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);

        System.out.println("Message sent: " + infoToSend);

        // waiting to receive a reply
        byte[] rbuf = new byte[sbuf.length];
        packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);

        //display the response
        System.out.println("Server's response: " + new String(packet.getData()));

        socket.close();
    }
}
