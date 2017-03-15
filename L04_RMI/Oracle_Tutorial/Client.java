package Oracle_Tutorial;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Evenilink on 15/03/2017.
 */
public class Client {
    private Client() {}

    public static void main(String[] args) throws NotBoundException {
        String host = (args.length != 1) ? null : args[0];

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            Hello stub = (Hello) registry.lookup("Hello");
            String response = stub.SayHello();
            System.out.println("response = " + response);
        } catch (RemoteException e) {
            System.out.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
