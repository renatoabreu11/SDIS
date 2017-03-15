package network;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements IRemoteInterface {

    public Server() {}

    @Override
    public String Register() throws RemoteException {
        return "Ola";
    }

    public static void main(String[] args) {
        Server serverObj = new Server();
        String remoteObjectName = "IRemoteInterface";

        try {
            IRemoteInterface stub = (IRemoteInterface) UnicastRemoteObject.exportObject(serverObj, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind(remoteObjectName, stub);

            System.out.println("Server is ready.");
        } catch (RemoteException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
