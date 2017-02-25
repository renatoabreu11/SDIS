import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MulticastSocketServer {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: java MulticastSocketServer1 <srvc_port> <mcast_addr> <mcast_port>");
            return;
        }

        int srvc_port = Integer.parseInt(args[0]);
        int mcast_port = Integer.parseInt(args[2]);
        String mcast_addr = args[1];
        InetAddress addr = InetAddress.getByName(mcast_addr);
        DatagramSocket serverSocket = new DatagramSocket();

        String advertisement = "<" + mcast_addr + "><" + args[2] + ">:<" + "localhost><" + args[0] + ">";
        byte[] adMessage = advertisement.getBytes();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    DatagramPacket resp = new DatagramPacket(adMessage, adMessage.length, addr, mcast_port);
                    serverSocket.send(resp);
                    System.out.println("multicast:"+advertisement);
                } catch (IOException e) {

                }
            }
        };

        Timer timer = new Timer("MyTimer");//create a new Timer
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);

        byte[] buffer = new byte[1024];
        DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
        System.out.println("SocketServer initialized!");
        Map<String, String> vehicles = new HashMap<>();

        while (true) {
            serverSocket.receive(dgp);
            String request = new String(dgp.getData(), 0, dgp.getLength());
            String message = request + ", from address: " + dgp.getAddress() + ", through port: " + dgp.getPort();
            System.out.println(message);
            String[] splitedRequest = request.split("\\s+");

            String response = null;
            String plate_number = splitedRequest[1];
            if (splitedRequest[0].equals("REGISTER")) {
                String lookup = vehicles.get(plate_number);
                String owner_name = "";
                for (int i = 2; i < splitedRequest.length; i++) {
                    if (i == 2)
                        owner_name = owner_name + splitedRequest[i];
                    else owner_name = owner_name + " " + splitedRequest[i];
                }
                if (lookup != null) {
                    response = "-1";
                    System.out.println("<Register><" + plate_number + "><" + owner_name + ">::-1");
                } else {
                    vehicles.put(plate_number, owner_name);
                    response = Integer.toString(vehicles.size());
                    System.out.println("<Register><" + plate_number + "><" + owner_name + ">::"+response);
                }
            } else if (splitedRequest[0].equals("LOOKUP")) {
                String lookup = vehicles.get(plate_number);
                if (lookup == null) {
                    response = "NOT_FOUND";
                    System.out.println("<Lookup><" + plate_number + ">::NOT_FOUND");
                } else {
                    response = lookup;
                    System.out.println("<Lookup><" + plate_number + ">::" + lookup);
                }
            }

            buffer = response.getBytes();
            DatagramPacket resp = new DatagramPacket(buffer, buffer.length, addr, mcast_port);
            serverSocket.send(resp);
        }
    }
}
