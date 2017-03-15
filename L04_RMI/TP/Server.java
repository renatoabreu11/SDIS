package TP;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * Created by Evenilink on 15/03/2017.
 */
public class Server implements IVehicles {

    private static HashMap<String, String> vehiclesStored;

    public Server() {}

    @Override
    public String RegisterCar(String plate, String owner) throws RemoteException {
        if(vehiclesStored.containsKey(plate))
            return "ERROR";

        vehiclesStored.put(plate, owner);
        return "Plate '" + plate + "' was registered for the owner '" + owner + "'.";
    }

    @Override
    public String GetCar(String plate) throws RemoteException {
        String owner;
        if((owner = vehiclesStored.get(plate)) == null)
            return "ERROR";

        return "The owner of plate '" + plate + "' is '" + owner;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        String removeObjectName = args[0];
        vehiclesStored = new HashMap<>();
        Server server = new Server();

        try {
            IVehicles stub = (IVehicles) UnicastRemoteObject.exportObject(server, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind(removeObjectName, stub);

            System.out.println("Server is ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

}
