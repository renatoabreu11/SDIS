package network;

import fileSystem.Splitter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

public class Peer implements IClientPeer {

    private int id;
    private byte[] buf;

    public Peer(String multicastAddress, int multicastPort, String mdbAddress, int mdbPort, String mdlAddress, int mdlPort) {
        id = 0;

        // Joins the multicast group.
        try {
            InetAddress address = InetAddress.getByName(multicastAddress);
            MulticastSocket multicastSocket = new MulticastSocket(multicastPort);
            multicastSocket.joinGroup(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void BackupFile(String pathname, int replicationDegree) throws RemoteException {
        Splitter splitter = new Splitter(pathname);

        try {
            splitter.splitFile(replicationDegree);

            for(int i = 0; i < splitter.getChunks().size(); i++) {
                // Send the chunks replicationDegree times to the MC.
            }
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
