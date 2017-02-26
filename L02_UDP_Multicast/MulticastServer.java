import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Evenilink on 24/02/2017.
 */
public class MulticastServer {

    final static String srvc_addr = "127.0.0.1";

    public static void main(String args[]) throws IOException {
        if(args.length != 3) {
            System.out.println("Usage: java MulticastServer <srvc_port> <mcast_addr> <mcast_port>");
            return;
        }

        System.out.println("Server is online.");

        int srvc_port = Integer.parseInt(args[0]);
        int mcast_port = Integer.parseInt(args[2]);
        String mcast_addr = args[1];

        HashMap<String, String> vehicles = new HashMap<>();
        byte[] buf = new byte[256];

        InetAddress address = InetAddress.getByName(mcast_addr);
        DatagramSocket serverSocket = new DatagramSocket(srvc_port);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        // String to advertise to all clients
        String msgToAdvertise = mcast_addr + " " + mcast_port + " " + srvc_addr + " " + srvc_port;

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                byte[] buf2 = msgToAdvertise.getBytes();
                DatagramPacket advertisementPacket = new DatagramPacket(buf2, buf2.length, address, mcast_port);
                try {
                    serverSocket.send(advertisementPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("multicast: <" + mcast_addr + "> <" + mcast_port + ">: <" + srvc_addr + "> <" + srvc_port + ">");
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 1000);

        while(true) {
            serverSocket.receive(packet);

            String msgReceived = new String(packet.getData());
            String[] msgSplit = msgReceived.split(" ");
            String reply = "";

            // Analyses the first word of the message sent by the client
            switch(msgSplit[0]) {
                case "register":
                    if(vehicles.get(msgSplit[1]) != null) {
                        reply = "Vehicle \"" + msgSplit[1] + "\" is already here.";
                        break;
                    }
                    vehicles.put(msgSplit[1], msgSplit[2]);
                    reply = "Vehicle \"" + msgSplit[1] + "\" whose owner is \"" + msgSplit[2] + "\" added successfully!";
                    break;
                case "lookup":
                    String owner = vehicles.get(msgSplit[1]);
                    if(owner == null) {
                        reply = "Vehicle \"" + msgSplit[1] + "\" is not here.";
                        break;
                    }
                    reply = "The owner of the \"" + msgSplit[1] + "\" is \"" + owner + "\"";
                    vehicles.remove(msgSplit[1]);
                    break;
                default: break;
            }

            buf = reply.getBytes();
            packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
            serverSocket.send(packet);

            System.out.println("<" + msgSplit[0] + "> <" + msgSplit[1] + "> :: <" + reply + ">");

            buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
        }
    }
}

