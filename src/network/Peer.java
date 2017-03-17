package network;

import protocols.Backup;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements IClientPeer {

    private String protocolVersion;
    private String serverAccessPoint;
    private int id;
    private byte[] buf;

    public Peer(String protocolVersion, int id, String serverAccessPoint, String multicastAddress, int multicastPort, String mdbAddress, int mdbPort, String mdlAddress, int mdlPort) {
        this.protocolVersion = protocolVersion;
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;

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

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getServerAccessPoint() {
        return serverAccessPoint;
    }

    public void setServerAccessPoint(String serverAccessPoint) {
        this.serverAccessPoint = serverAccessPoint;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    @Override
    public void BackupFile(String pathname, int replicationDegree) throws RemoteException {
        // Construct message and send it to the MC channel.
    }

    @Override
    public void RestoreFile(String pathname) throws RemoteException {
        // Construct message and send it to the MC channel.
    }

    @Override
    public void DeleteFile(String pathname) throws RemoteException {
        // Construct message and send it to the MC channel.
    }

    public static void main(String[] args) {
        if(args.length != 4) {
            System.out.println("Usage: java Initializer <protocol_version> <server_id> <service_access_point> <mc:port> <mdb:port> <mdl:port>");
            return;
        }

        String[] msgSplit = args[3].split(":");
        String multicastAddress = msgSplit[0];
        String multicastPort = msgSplit[1];

        //msgSplit = args[4].split(":");
        String mdbAddress = "1";//msgSplit[0];
        String mdbPort = "1";//msgSplit[1];

        //msgSplit = args[5].split(":");
        String mdlAddress = "1";//msgSplit[0];
        String mdlPort = "1";//msgSplit[1];

        Peer peer = new Peer(args[0], Integer.parseInt(args[1]), args[2], multicastAddress, Integer.parseInt(multicastPort), mdbAddress, Integer.parseInt(mdbPort), mdlAddress, Integer.parseInt(mdlPort));
        peer.setServerAccessPoint(args[2]);

        IClientPeer stub = null;
        try {
            stub = (IClientPeer) UnicastRemoteObject.exportObject(peer, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind(peer.getServerAccessPoint(), stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }

        // Create 3 Threads (MC, MDL, MDB).
    }
}
