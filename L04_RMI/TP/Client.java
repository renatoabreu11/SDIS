package TP;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Evenilink on 15/03/2017.
 */
public class Client {
    public static void main(String[] args) {
        if(args.length != 4) {
            System.out.println("Usage: java Client <host_name> <remote_object_name> <oper> <opnd> *");
            return;
        }

        String hostName = null;
        String removeObjectName = args[1];

        try {
            Registry registry = LocateRegistry.getRegistry(hostName);
            IVehicles stub = (IVehicles) registry.lookup(removeObjectName);

            String reply = null;
            if(args[2].equals("register")) {
                String[] split = args[3].split(" ");
                reply = stub.RegisterCar(split[0], split[1]);
            } else if(args[2].equals("lookup"))
                reply = stub.GetCar(args[3]);

            String infoMessage = args[2] + " " + args[3] + ":: " + reply;
            System.out.println(infoMessage);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
