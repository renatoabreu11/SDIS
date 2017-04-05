package network;

import channels.*;
import fileSystem.*;
import messageSystem.*;
import protocols.ProtocolDispatcher;
import protocols.initiator.*;
import utils.Utils;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Peer implements IClientPeer {

    private ControlChannel mc;
    private BackupChannel mdb;
    private RestoreChannel mdr;
    private ProtocolDispatcher dispatcher;
    private ProtocolInitiator protocol = null;
    private FileManager manager;

    private int id;
    private String protocolVersion;
    private String serverAccessPoint;
    private IClientPeer stub;

    //Restore protocol auxiliar variables.
    private ArrayList<String> chunkRestoring = new ArrayList<>();        // Hosts the chunks who have already sent by other peers.

    // Manage disk space auxiliar variables.
    private long maxDiskSpace = 740;
    private ArrayList<String> chunkBackingUp = new ArrayList<>();

    public Peer(String protocolVersion, int id, String serverAccessPoint, String[] multicastInfo) throws IOException {
        this.protocolVersion = protocolVersion;
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;
        manager = new FileManager();

        manager.LoadMetadata();
        mc = new ControlChannel(multicastInfo[0], multicastInfo[1], this, true);
        mdb = new BackupChannel(multicastInfo[2], multicastInfo[3], this, true);
        mdr = new RestoreChannel(multicastInfo[4], multicastInfo[5], this, true);
        dispatcher = new ProtocolDispatcher(this, true);

        new Thread(mc).start();
        new Thread(mdb).start();
        new Thread(mdr).start();
        new Thread(dispatcher).start();

        if(id == 1)
            this.stub = (IClientPeer) UnicastRemoteObject.exportObject(this, 0);

        /*if(protocolVersion.equals("1.3")) {
            SendBornMessage();
        }*/

        System.out.println("All channels online.");
    }

    private void SendBornMessage() throws IOException {
        MessageHeader header = new MessageHeader(Utils.MessageType.AWOKE, protocolVersion, id);
        Message message = new Message(header);
        byte[] buffer = message.getMessageBytes();
        mc.sendMessage(buffer);
    }

    @Override
    public String BackupFile(byte[] fileData, String pathname, int replicationDegree) throws NoSuchAlgorithmException, IOException, InterruptedException {
        protocol = new BackupInitiator(protocolVersion, true, this, fileData, pathname, replicationDegree);
        protocol.startProtocol();
        String message = protocol.endProtocol();
        protocol = null;
        manager.WriteMetadata();
        return message;
    }

    public void updateFileStorage(Message msgWrapper) throws IOException {
        this.manager.updateStorage(msgWrapper);
        this.manager.WriteMetadata();
        if(this.protocol instanceof BackupInitiator)
            ((BackupInitiator) this.protocol).updateUploadingChunks(msgWrapper);

    }

    public void deleteFileStorage(String fileId) throws IOException {
        this.manager.deleteStoredChunks(fileId, id);
    }

    @Override
    public byte[] RestoreFile(String pathname) throws IOException, InterruptedException {
        protocol = new RestoreInitiator(protocolVersion, true, this, pathname);
        protocol.startProtocol();
        byte[] fileData = ((RestoreInitiator) this.protocol).getFile();
        protocol.endProtocol();
        protocol = null;
        return fileData;
    }

    /**
     * Restore protocol callable.
     * Called every time the MDR received a message.
     * Adds a chunk to the file system (if initiator-peer) or stops others from sending the message.
     * @param msgWrapper
     */
    public void receiveChunk(Message msgWrapper) {
        if(this.protocol instanceof RestoreInitiator)
            ((RestoreInitiator) this.protocol).addChunkToRestoring(msgWrapper);
    }

    @Override
    public void DeleteFile(String pathname) throws IOException, NoSuchAlgorithmException, InterruptedException {
        protocol = new DeleteInitiator(protocolVersion, true, this, pathname);
        protocol.startProtocol();
        protocol.endProtocol();
        protocol = null;
    }

    /**
     * Client callable.
     * Handles the disk space.
     * @param client_maxDiskSpace
     * @throws IOException
     */
    @Override
    public String ManageDiskSpace(long client_maxDiskSpace) throws IOException, InterruptedException {
        protocol = new ManageDiskInitiator(protocolVersion, true, this, client_maxDiskSpace);
        protocol.startProtocol();
        protocol.endProtocol();
        String msgReply = ((ManageDiskInitiator)this.protocol).getSuccessMessage();
        protocol = null;
        return msgReply;
    }

    @Override
    public String RetrieveInformation() throws IOException, InterruptedException {
        protocol = new RetrieveInfoInitiator(protocolVersion, true, this);
        protocol.startProtocol();
        protocol.endProtocol();
        String msgReply = ((RetrieveInfoInitiator)this.protocol).getOut();
        protocol = null;
        return msgReply;
    }

    public static void main(String[] args) throws IOException, AlreadyBoundException {
        if(args.length != 6) {
            System.out.println("Usage: java Peer <protocol_version> <server_id> <service_access_point> <mc:port> <mdb:port> <mdl:port>");
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

        Registry registry;
        if(peer.id == 1){
            String IPV4 = Utils.getIPV4address();
            System.out.println("My address: " + IPV4);
            System.setProperty("java.rmi.server.hostname", IPV4);
            registry = LocateRegistry.createRegistry(Utils.RMI_PORT);
            registry.bind(peer.serverAccessPoint, peer.getStub());
        }

        System.out.println("Server is ready.");
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public int getId() {
        return id;
    }

    public IClientPeer getStub() {
        return stub;
    }

    public ControlChannel getMc() {
        return mc;
    }

    public FileManager getManager() {
        return manager;
    }

    public long getMaxDiskSpace() {
        return maxDiskSpace;
    }

    public void setMaxDiskSpace(long maxDiskSpace) {
        this.maxDiskSpace = maxDiskSpace;
    }

    public ArrayList<String> getChunkBackingUp() {
        return chunkBackingUp;
    }

    public void addChunkBackingUp(Chunk chunk) {
        this.chunkBackingUp.add(chunk.getFileId() + chunk.getChunkNo());
    }

    public void removeChunkBackingUp(Chunk chunk) {
        this.chunkBackingUp.remove(chunk);
    }

    public void addMessageToDispatcher(String message) {
        dispatcher.addMessage(message);
    }

    public void saveFileToStorage(_File f) {
        manager.addFileToStorage(f);
    }

    public void sendMessageMDB(byte[] buffer) {
        mdb.sendMessage(buffer);
    }

    public void sendMessageMDR(byte[] buffer) {
        mdr.sendMessage(buffer);
    }

    public void sendMessageMC(byte[] buffer) {
        mc.sendMessage(buffer);
    }

    public _File getFileFromManager(String pathname) {
        return manager.getFile(pathname);
    }

    public ArrayList<String> getChunkRestoring() {
        return chunkRestoring;
    }

    public synchronized boolean freeDisposableSpace(long spaceNeeded) {
        ArrayList<Chunk> storedChunksWithHighRD = manager.getChunksWithHighRD(id);
        
        if(storedChunksWithHighRD.size() == 0)
            return false;

        long disposableSpace = manager.countDisposableSpace(storedChunksWithHighRD);

        if(disposableSpace > spaceNeeded){
            long spaceToRemove = disposableSpace - spaceNeeded;
            long spaceRemoved = 0;
            for(Chunk c : storedChunksWithHighRD){
                long aux = 0;
                try {
                   aux = manager.deleteStoredChunk(c.getFileId(), c.getChunkNo(), id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                spaceRemoved += aux;
                if(spaceRemoved >= spaceToRemove)
                    break;
            }
        }else return false;

        return true;
    }

    public synchronized long getDiskUsage() {
        try {
            return manager.getCurrOccupiedSize(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
