package network;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Initializer {

    public static void main(String[] args) {
        Peer peerObj = new Peer();
        String remoteObjectName = "IClientPeer";

        try {
            IClientPeer stub = (IClientPeer) UnicastRemoteObject.exportObject(peerObj, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind(remoteObjectName, stub);

            System.out.println("Initializer is ready.");
        } catch (RemoteException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
