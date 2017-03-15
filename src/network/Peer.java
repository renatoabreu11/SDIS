package network;

import fileSystem.Splitter;

import java.io.IOException;
import java.rmi.RemoteException;

public class Peer implements IClientPeer {

    private int id;

    public Peer(String MC, String MDB, String MDR) {
        id = 0;
    }

    @Override
    public void BackupFile(String pathname, int replicationDegree) throws RemoteException {
        Splitter splitter = new Splitter(pathname);

        try {
            splitter.splitFile(replicationDegree);
        } catch (IOException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void RestoreFile(String pathname) throws RemoteException {

    }

    @Override
    public void DeleteFile(String pathname) throws RemoteException {

    }
}
