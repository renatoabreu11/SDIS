package network;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost";
        String remoteObjectName = "IRemoteInterface";

        try {
            Registry registry = LocateRegistry.getRegistry(hostname);
            IRemoteInterface stub = (IRemoteInterface) registry.lookup(remoteObjectName);

            String reply = stub.Register();
            System.out.println(reply);
        } catch (RemoteException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
