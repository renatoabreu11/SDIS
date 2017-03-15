package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Evenilink on 15/03/2017.
 */
public interface IClientPeer extends Remote {

    void BackupFile(String pathname, int replicationDegree) throws RemoteException;
    void RestoreFile(String pathname) throws RemoteException;
    void DeleteFile(String pathname) throws RemoteException;
}
