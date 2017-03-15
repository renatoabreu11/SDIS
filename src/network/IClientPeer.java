package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientPeer extends Remote {

    /**
     * @param pathname the system path for the file to backup.
     * @param replicationDegree number of servers to distribute the chunks.
     * @throws RemoteException
     */
    void BackupFile(String pathname, int replicationDegree) throws RemoteException;

    /**
     * @param pathname the system path for the file to backup.
     * @throws RemoteException
     */
    void RestoreFile(String pathname) throws RemoteException;

    /**
     * @param pathname the system path for the file to backup.
     * @throws RemoteException
     */
    void DeleteFile(String pathname) throws RemoteException;
}
