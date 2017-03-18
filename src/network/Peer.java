package network;

import backupService.*;
import fileSystem.Chunk;
import fileSystem.FileManager;
import fileSystem.Splitter;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Peer implements IClientPeer {

    private ControlChannel mc;
    private BackupChannel mdb;
    private RecoveryChannel mdr;

    private String protocolVersion;
    private String serverAccessPoint;
    private int id;
    private IClientPeer stub;

    private boolean isInitiator = false;
    private FileManager manager;

    public Peer(String protocolVersion, int id, String serverAccessPoint, String[] multicastInfo) throws IOException {

        this.protocolVersion = protocolVersion;
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;
        manager = new FileManager();

        mc = new ControlChannel(multicastInfo[0], multicastInfo[1], this);
        mdb = new BackupChannel(multicastInfo[2], multicastInfo[3], this);
        mdr = new RecoveryChannel(multicastInfo[4], multicastInfo[5], this);

        new Thread(mc).start();
        new Thread(mdb).start();
        new Thread(mdr).start();

        this.stub = (IClientPeer) UnicastRemoteObject.exportObject(this, 0);

        System.out.println("All channels online.");
    }

    @Override
    public void BackupFile(String pathname, int replicationDegree) throws NoSuchAlgorithmException, IOException, InterruptedException {
        isInitiator = true;
        int numRetransmission = 1;

        String lastModified = Long.toString(new File(pathname).lastModified());

        // Hashing the file id.
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String fileId = pathname + lastModified;
        md.update(fileId.getBytes("UTF-8"));
        byte[] fileIdHashed = md.digest();

        // Splitting the file into chunks.
        Splitter splitter = new Splitter(pathname);
        splitter.splitFile(replicationDegree, fileId);

        this.manager.addUploadingChunks(splitter.getChunks());

        boolean desiredReplicationDegree = false;
        do{

            if(numRetransmission > 5)
                break; // do something

            Map<Chunk, ArrayList<Integer>> uploadingChunks = this.manager.getUploading();
            Set<Chunk> keys = uploadingChunks.keySet();
            for(Chunk c:keys){
                MessageHeader header = new MessageHeader(Utils.MessageType.PUTCHUNK, protocolVersion, id, fileIdHashed.toString(), c.getChunkNo(), replicationDegree);
                MessageBody body = new MessageBody(c.getChunkData());
                Message message = new Message(header, body);
                byte[] buffer = message.getMessageBytes();
                mdb.sendMessage(buffer);
            }

            TimeUnit.MILLISECONDS.sleep(1000*numRetransmission);

            int chunksToUpload = this.manager.chunksToUpload();
            if(chunksToUpload == 0)
                desiredReplicationDegree = true;
            else numRetransmission++;
        } while(!desiredReplicationDegree);

        this.manager.resetUploadingChunks();

        isInitiator = false;
    }

    @Override
    public void RestoreFile(String pathname) throws RemoteException {
        // Construct message and send it to the MC channel.
    }

    @Override
    public void DeleteFile(String pathname) throws RemoteException {
        // Construct message and send it to the MC channel.
    }

    public void updateFileStorage(Message msgWrapper) {
        this.manager.updateStorage(msgWrapper);

        if(this.isInitiator)
            this.manager.updateUploadingChunks(msgWrapper);
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 6) {
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
        String mdrAddress = msgSplit[0];
        String mdrPort = msgSplit[1];

        String[] multicastInfo = {multicastAddress, multicastPort, mdbAddress, mdbPort, mdrAddress, mdrPort};

        Peer peer = new Peer(args[0], Integer.parseInt(args[1]), args[2], multicastInfo);

        try {
            // Supposedly the RMI is initialized only on one machine...
            int port = peer.id + 1098;

            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind(peer.getServerAccessPoint(), peer.getStub());
        } catch (RemoteException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        System.out.println("Server is ready.");
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

    public ControlChannel getMc() {
        return mc;
    }

    public void setMc(ControlChannel mc) {
        this.mc = mc;
    }

    public BackupChannel getMdb() {
        return mdb;
    }

    public void setMdb(BackupChannel mdb) {
        this.mdb = mdb;
    }

    public RecoveryChannel getMdr() {
        return mdr;
    }

    public void setMdr(RecoveryChannel mdr) {
        this.mdr = mdr;
    }

    public boolean isInitiator() {
        return isInitiator;
    }

    public void setInitiator(boolean initiator) {
        isInitiator = initiator;
    }

    public FileManager getManager() {
        return manager;
    }

    public void setManager(FileManager manager) {
        this.manager = manager;
    }
}
