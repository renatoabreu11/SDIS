package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientPeer extends Remote {

    void BackupFile(String pathname, int replicationDegree) throws RemoteException;
    void RestoreFile(String pathname) throws RemoteException;
    void DeleteFile(String pathname) throws RemoteException;
}
