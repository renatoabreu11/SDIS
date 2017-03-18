package network;

import protocols.Backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements IClientPeer {

    private String mcAddress;
    private int mcPort;
    private String mdbAddress;
    private int mdbPort;
    private String mdrAddress;
    private int mdrPort;

    private String protocolVersion;
    private String serverAccessPoint;
    private int id;
    private IClientPeer stub;

    private InetAddress inetAddress;
    private MulticastSocket multicastSocket;
    private DatagramPacket datagramPacket;
    private byte[] buf;

    public Peer(String protocolVersion, int id, String serverAccessPoint, String mcAddress, int mcPort, String mdbAddress, int mdbPort, String mdrAddress, int mdrPort) throws IOException {
        this.mcAddress = mcAddress;
        this.mcPort = mcPort;
        this.mdbAddress = mdbAddress;
        this.mdbPort = mdbPort;
        this.mdrAddress = mdrAddress;
        this.mdrPort = mdrPort;
        this.protocolVersion = protocolVersion;
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;

        inetAddress = InetAddress.getByName(this.mcAddress);
        multicastSocket = new MulticastSocket(this.mcPort);
        multicastSocket.joinGroup(inetAddress);

        this.stub = (IClientPeer) UnicastRemoteObject.exportObject(this, 0);
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

    public IClientPeer getStub() {
        return stub;
    }

    public void setStub(IClientPeer stub) {
        this.stub = stub;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    @Override
    public void BackupFile(String pathname, int replicationDegree) throws RemoteException {
        System.out.print("S");

        buf = "Hey bro".getBytes();
        datagramPacket = new DatagramPacket(buf, buf.length, inetAddress, mcPort);
        try {
            multicastSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void RestoreFile(String pathname) throws RemoteException {
        // Construct message and send it to the MC channel.
    }

    @Override
    public void DeleteFile(String pathname) throws RemoteException {
        // Construct message and send it to the MC channel.
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 7) {
            System.out.println("Usage: java Initializer <protocol_version> <server_id> <service_access_point> <mc:port> <mdb:port> <mdl:port>");
            return;
        }

        String[] msgSplit = args[3].split(":");
        String multicastAddress = msgSplit[0];
        String multicastPort = msgSplit[1];

        msgSplit = args[4].split(":");
        String mdbAddress = msgSplit[0];
        String mdbPort = msgSplit[1];

        msgSplit = args[5].split(":");
        String mdlAddress = msgSplit[0];
        String mdlPort = msgSplit[1];

        Peer peer = new Peer(args[0], Integer.parseInt(args[1]), args[2], multicastAddress, Integer.parseInt(multicastPort), mdbAddress, Integer.parseInt(mdbPort), mdlAddress, Integer.parseInt(mdlPort));

        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind(peer.getServerAccessPoint(), peer.getStub());
        } catch (RemoteException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        // Create 3 Threads (MC, MDL, MDB).
        //Backup backup = new Backup(peer.mdbAddress, peer.mdbPort);
        //backup.start();

        System.out.println("Ready...");

        if(args[6].equals("0")) {
            System.out.println("Peer waiting...");
            byte[] buf = new byte[512];
            peer.datagramPacket = new DatagramPacket(buf, buf.length);
            while(true) {
                peer.multicastSocket.receive(peer.datagramPacket);
                String message = new String(peer.datagramPacket.getData());
                System.out.println("Initiator peer enviou: " + message);
            }
        }

    }
}
