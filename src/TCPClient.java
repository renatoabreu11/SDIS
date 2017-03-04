package src;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage:  java TCPClient <host_name> <port_number> <oper> <opnd> *");
            return;
        }

        String pattern = "\\w{2}-\\w{2}-\\w{2}";
        String request = args[2];
        String plate_number = args[3];
        String message;

        if(!plate_number.matches(pattern)){
            System.out.println("The plate  number must be in the format XX-XX-XX where X is a letter or a digit.");
            return;
        }

        switch(request){
            case "register":
                String owner_name = args[4];
                if(owner_name.length() > 256){
                    System.out.println("The  vehicle owner's name must have less than 256 characters.");
                    return;
                }else message = "REGISTER " + plate_number + " " + owner_name;
                break;
            case "lookup":
                message = "LOOKUP " + plate_number;
                break;
            default:
                System.out.print("Invalid TCPClient Request. It has to be register or lookup.");
                return;
        }
        System.out.println(message);

        String host_name = args[0];
        int port = Integer.parseInt(args[1]);
        InetAddress address = InetAddress.getByName(host_name);

        Socket clientSocket = null;
        try {
            clientSocket = new Socket(address, port);
            System.out.println("Client Socket initialized!");
        } catch (IOException e) {
            System.out.println("Could not establish TCP socket connection");
            System.exit(-1);
        }

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outToServer.writeBytes(message + '\n');
        String response = inFromServer.readLine();
        System.out.println("Server Message: " + response);

        if(response.matches("^-?\\d+$"))
            if(response.equals("-1"))
                System.out.println("The plate number  " + plate_number + " has already been registered!");
            else System.out.println("Plate number " + plate_number + " registered!" + " The current number of vehicles registered is " + response + ".");
        else if(response.equals("NOT_FOUND"))
            System.out.println("The plate number " + plate_number + " isn't registered in the system!");
        else System.out.println(response + " is the owner of the vehicle with plate number " + plate_number + ".");

        clientSocket.close();
        System.out.println("Socket closed!");
    }
}
