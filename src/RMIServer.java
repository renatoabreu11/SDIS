package src;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class RMIServer implements Operation {

    private Map<String, String> vehicles = new HashMap<>();

    public RMIServer() {}

    public static void main(String args[]) throws UnknownHostException {

        if(args.length < 1){
            System.out.println("Usage: java RMIServer <remote_object_name>");
            return;
        }
        String remoteObject = args[0];

        try {
            RMIServer obj = new RMIServer();
            Operation stub = (Operation) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind(remoteObject, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String lookup(String plateNumber) throws RemoteException {
        String response;
        String lookup = vehicles.get(plateNumber);
        if (lookup == null) {
            response = "NOT_FOUND";
            System.out.println("<Lookup><" + plateNumber + ">::NOT_FOUND");
        } else {
            response = lookup;
            System.out.println("<Lookup><" + plateNumber + ">::" + lookup);
        }
        return response;
    }

    @Override
    public String register(String plateNumber, String ownerName) throws RemoteException {
        String response;
        String lookup = vehicles.get(plateNumber);
        if (lookup != null)
            response = "-1";
        else {
            vehicles.put(plateNumber, ownerName);
            response = Integer.toString(vehicles.size());
        }
        System.out.println("<Register><" + plateNumber + "><" + ownerName + ">::"+response);
        return response;
    }
}
