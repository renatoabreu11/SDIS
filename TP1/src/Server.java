import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by Evenilink on 17/02/2017.
 */
public class Server {
    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.out.println("Usage: java Server <port_number>");
            return;
        }

        System.out.println("Server initialized!");
        HashMap<String, String> vehicles = new HashMap<String, String>();      // <plate_number, owner_name>

        int port = Integer.parseInt(args[0]);
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        DatagramSocket socket = new DatagramSocket(port);

        while(true) {
            socket.receive(packet);     // waits until it receives something

            String message = new String(packet.getData());
            String words[] = message.split(" ");
            String firstWord = words[0];

            for(int i = 0; i < words.length; i++)
                System.out.println(words[i]);

            if(firstWord.equals("register")) {
                vehicles.put(words[1], words[2]);
                buf = ("Successful added!").getBytes();
                System.out.println("Added plate '" + words[1] + "' and owner '" + words[2] + "' to the server!");
            } else if(firstWord.equals("lookup")) {
                String owner = (String) vehicles.get(words[1]);
                buf = owner.getBytes();
                System.out.println("Searched for plate '" + words[1] + "'!");
            }

            InetAddress address = packet.getAddress();
            packet = new DatagramPacket(buf, buf.length, address, packet.getPort());
            socket.send(packet);
        }

        //socket.close();
    }
}
