package src;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {

    private RMIClient() {}

    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Usage: java RMIClient <host_name> <remote_object_name> <oper> <opnd> *");
            return;
        }

        String hostName = args[0];
        String remoteObject = args[1];
        String pattern = "\\w{2}-\\w{2}-\\w{2}";
        String request = args[2];
        String plateNumber = args[3];
        String message;

        if (!plateNumber.matches(pattern)) {
            System.out.println("The plate number must be in the format XX-XX-XX where X is a letter or a digit.");
            return;
        }

        String ownerName = "";
        switch (request) {
            case "register":
                ownerName = args[4];
                if (ownerName.length() > 256) {
                    System.out.println("The vehicle owner's name must have less than 256 characters.");
                    return;
                } else message = "REGISTER " + plateNumber + " " + ownerName;
                break;
            case "lookup":
                message = "LOOKUP " + plateNumber;
                break;
            default:
                System.out.print("Invalid RMIClient Request. It has to be register or lookup.");
                return;
        }

        System.out.println(message);

        try {
            Registry registry = LocateRegistry.getRegistry(hostName);
            Operation stub = (Operation) registry.lookup(remoteObject);
            String response;
            if(request == "register")
                response = stub.register(plateNumber, ownerName);
            else response = stub.lookup(plateNumber);
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
