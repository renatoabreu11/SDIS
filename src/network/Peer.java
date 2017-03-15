package network;

import java.rmi.RemoteException;

public class Peer implements IClientPeer {

    private int id;

    public Peer() {
        id = 0;
    }

    @Override
    public void BackupFile(String pathname, int replicationDegree) throws RemoteException {

    }

    @Override
    public void RestoreFile(String pathname) throws RemoteException {

    }

    @Override
    public void DeleteFile(String pathname) throws RemoteException {

    }
}
