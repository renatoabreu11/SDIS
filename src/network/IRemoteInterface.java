package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Evenilink on 15/03/2017.
 */
public interface IRemoteInterface extends Remote {
    String Register() throws RemoteException;
}
