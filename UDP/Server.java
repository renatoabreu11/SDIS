import java.net.*;
import java.util.HashMap;
import java.util.Map;

//DatagramSocket represents a socket for sending and receiving datagram packets.
//Datagram packets are used to implement a connectionless packet delivery service.
//Each message is routed from one machine to another based solely on information contained within that packet

public class Server{
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port_number>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        DatagramSocket socket = new DatagramSocket(port);
        byte[] buffer = new byte[1024];
        DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
        System.out.println("Server initialized!");

        Map<String , String> vehicles = new HashMap<>();

        while(true){
            socket.receive(dgp);
            String request = new String(dgp.getData(), 0, dgp.getLength());
            String message = request +", from address: " + dgp.getAddress() + ", through port: " + dgp.getPort();
            System.out.println(message);
            String[] splitedRequest = request.split("\\s+");

            String response = null;
            String plate_number = splitedRequest[1];
            if(splitedRequest[0].equals("REGISTER")){
                String lookup = vehicles.get(plate_number);
                if(lookup != null){
                    response = "-1";
                    System.out.println("Register request failed. The plate number " + plate_number + " already exists in the database!");
                }else{
                    String owner_name = "";
                    for(int i = 2; i < splitedRequest.length; i++){
                        if(i == 2)
                            owner_name = owner_name + splitedRequest[i];
                        else owner_name = owner_name + " " + splitedRequest[i];
                    }
                    vehicles.put(plate_number, owner_name);
                    System.out.println("Register request completed. " + owner_name + " vehicle with plate number " + plate_number + " was added to the database!");
                    response = Integer.toString(vehicles.size());
                }
            }else if(splitedRequest[0].equals("LOOKUP")){
                String lookup = vehicles.get(plate_number);
                if(lookup == null) {
                    response = "NOT_FOUND";
                    System.out.println("Lookup request failed. The plate number " + plate_number + "doesn't exists in the database!");
                }
                else{
                    response = lookup;
                    System.out.println("Lookup request completed. " + lookup  + " is the owner of the vehicle with plate number " + plate_number +".");
                }
            }

            buffer = response.getBytes();
            DatagramPacket resp = new DatagramPacket(buffer, buffer.length, dgp.getAddress(), dgp.getPort());
            socket.send(resp);
        }
    }
}
