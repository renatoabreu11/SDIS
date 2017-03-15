package Oracle_Tutorial;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Evenilink on 15/03/2017.
 */
public class Server implements Hello {
    public Server() {}

    public String SayHello() {
        return "Hello, world!";
    }

    public static void main(String[] args) throws AlreadyBoundException {
        try {
            Server obj = new Server();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry.
            Registry registry = LocateRegistry.createRegistry(1099);        // Use port '1099' as the default port.
            registry.bind("Hello", stub);

            System.out.println("Server is ready.");
        } catch (RemoteException e) {
            System.out.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
