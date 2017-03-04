package src;

import java.net.*;

public class SocketClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java SocketClient <host_name> <port_number> <oper> <opnd>*");
            return;
        }

        String pattern = "\\w{2}-\\w{2}-\\w{2}";
        String request = args[2];
        String plate_number = args[3];
        String message;

        if(!plate_number.matches(pattern)){
            System.out.println("The plate number must be in the format XX-XX-XX where X is a letter or a digit.");
            return;
        }

        switch(request){
            case "register":
                String owner_name = args[4];
                if(owner_name.length() > 256){
                    System.out.println("The vehicle owner's name must have less than 256 characters.");
                    return;
                }else message = "REGISTER " + plate_number + " " + owner_name;
                break;
            case "lookup":
                message = "LOOKUP " + plate_number;
                break;
            default:
                System.out.print("Invalid SocketClient Request. It has to be register or lookup.");
                return;
        }
        System.out.println(message);
        byte[] buffer = message.getBytes();

        DatagramSocket socket = new DatagramSocket();
        String host_name = args[0];
        int port = Integer.parseInt(args[1]);
        InetAddress address = InetAddress.getByName(host_name);

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);

        byte[] rbuffer = new byte[1024];
        packet = new DatagramPacket(rbuffer, rbuffer.length, address, port);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Echoed Message: " + received);
        if(received.matches("^-?\\d+$"))
            if(received.equals("-1"))
                System.out.println("The plate number " + plate_number + " has already been registered!");
            else System.out.println("Plate number " + plate_number + " registered!" + " The current number of vehicles registered is " + received + ".");
        else if(received.equals("NOT_FOUND"))
            System.out.println("The plate number " + plate_number + " isn't registered in the system!");
        else System.out.println(received + " is the owner of the vehicle with plate number " + plate_number + ".");
        socket.close();
        System.out.println("Socket closed!");
    }
}
