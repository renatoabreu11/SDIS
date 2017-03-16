package src;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Operation extends Remote {
    String lookup(String plateNumber) throws RemoteException;

    String register(String plateNumber, String ownerName) throws RemoteException;
}
