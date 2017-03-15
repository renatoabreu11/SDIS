package Oracle_Tutorial;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Evenilink on 15/03/2017.
 */
public interface Hello extends Remote {
    String SayHello() throws RemoteException;
}
