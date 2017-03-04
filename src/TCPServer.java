package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage : java TCPServer <srvc_port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket srvSocket = null;
        Socket requestSocket = null;
        try {
            srvSocket = new ServerSocket(port);
            System.out.println("Server Socket initialized!");
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        Map<String , String> vehicles = new HashMap<>();

        while(true){
            try {
                requestSocket = srvSocket.accept();
                System.out.println("Server accepting requests!");
            } catch (IOException e) {
                System.err.println("Accept failed: " + port);
                System.exit(1);
            }
            BufferedReader requests = new BufferedReader(new InputStreamReader(
                    requestSocket.getInputStream()));
            DataOutputStream responses = new DataOutputStream(requestSocket.getOutputStream());
            String request = requests.readLine();
            System.out.println("Request received: " + request);
            String[] splitedRequest = request.split("\\s+");

            String response = null;
            String plate_number = splitedRequest[1];
            if(splitedRequest[0].equals("REGISTER")){
                String lookup = vehicles.get(plate_number);
                if(lookup != null){
                    response = "-1";
                    System.out.println("Register  request failed. The plate number " + plate_number + " already exists in the database!");
                }else{
                    String owner_name = "";
                    for(int i = 2; i < splitedRequest.length; i++){
                        if(i == 2)
                            owner_name = owner_name + splitedRequest[i];
                        else owner_name = owner_name + " " + splitedRequest[i];
                    }
                    vehicles.put(plate_number, owner_name);
                    System.out.println("Register  request completed. " + owner_name + " vehicle with plate number " + plate_number + " was added to the database!");
                    response = Integer.toString(vehicles.size());
                }
            }else if(splitedRequest[0].equals("LOOKUP")){
                String lookup = vehicles.get(plate_number);
                if(lookup == null) {
                    response = "NOT_FOUND";
                    System.out.println("Lookup request  failed. The plate number " + plate_number + "doesn't exists in the database!");
                }
                else{
                    response = lookup;
                    System.out.println("Lookup request  completed. " + lookup  + " is the owner of the vehicle with plate number " + plate_number +".");
                }
            }
            responses.writeBytes(response + '\n');
        }
    }
}
