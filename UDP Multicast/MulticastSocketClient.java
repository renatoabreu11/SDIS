import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSocketClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java MulticastSocketClient1 <mcast_addr> <mcast_port> <oper> <opnd>*");
            return;
        }

        String mcast_addr = args[0];
        int mcast_port = Integer.parseInt(args[1]);
        String pattern = "\\w{2}-\\w{2}-\\w{2}";
        String request = args[2];
        String plate_number = args[3];
        String message;

        if (!plate_number.matches(pattern)) {
            System.out.println("The plate number must be in the format XX-XX-XX where X is a letter or a digit.");
            return;
        }

        switch (request) {
            case "register":
                String owner_name = args[4];
                if (owner_name.length() > 256) {
                    System.out.println("The vehicle owner's name must have less than 256 characters.");
                    return;
                } else message = "REGISTER " + plate_number + " " + owner_name;
                break;
            case "lookup":
                message = "LOOKUP " + plate_number;
                break;
            default:
                System.out.print("Invalid MulticastSocketClient Request. It has to be register or lookup.");
                return;
        }
        byte[] buffer = message.getBytes();

        //One would join a multicast group by first creating a MulticastSocket with the desired port,
        //then invoking the joinGroup(InetAddress groupAddr) method
        InetAddress group = InetAddress.getByName(mcast_addr);
        try (MulticastSocket clientSocket = new MulticastSocket(mcast_port)) {
            clientSocket.joinGroup(group);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, mcast_port);
            clientSocket.send(packet);

            byte[] rbuffer = new byte[1024];
            packet = new DatagramPacket(rbuffer, rbuffer.length, group, mcast_port);
            clientSocket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Echoed Message: " + received);
            if(received.matches("^-?\\d+$"))
                if(received.equals("-1"))
                    System.out.println("Plate number " + plate_number + " has already been registered!");
                else System.out.println("Plate number " + plate_number + " registered!" + " The current number of vehicles registered is " + received + ".");
            else if(received.equals("NOT_FOUND"))
                System.out.println("The plate number " + plate_number + " isn't registered in the system!");
            else System.out.println(received + " is the owner of the vehicle with plate number " + plate_number + ".");
            clientSocket.leaveGroup(group);
            clientSocket.close();
            System.out.println("Socket closed!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
