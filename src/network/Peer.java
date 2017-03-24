package network;

import channels.*;
import fileSystem.*;
import messageSystem.*;
import utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static utils.Utils.BackupRetransmissions;

public class Peer implements IClientPeer {

    private ControlChannel mc;
    private BackupChannel mdb;
    private RestoreChannel mdr;

    private String protocolVersion;
    private String serverAccessPoint;
    private int id;
    private IClientPeer stub;

    private boolean isInitiator = false;
    private boolean logSystem = true;
    private FileManager manager;

    // Restore protocol auxiliar variables.
    private boolean canSendRestoreMessages = true;

    public Peer(String protocolVersion, int id, String serverAccessPoint, String[] multicastInfo) throws IOException {
        this.protocolVersion = protocolVersion;
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;
        manager = new FileManager();

        mc = new ControlChannel(multicastInfo[0], multicastInfo[1], this);
        mdb = new BackupChannel(multicastInfo[2], multicastInfo[3], this);
        mdr = new RestoreChannel(multicastInfo[4], multicastInfo[5], this);

        new Thread(mc).start();
        new Thread(mdb).start();
        new Thread(mdr).start();

        this.stub = (IClientPeer) UnicastRemoteObject.exportObject(this, 0);

        System.out.println("All channels online.");
    }

    @Override
    public void BackupFile(byte[] fileData, String pathname, int replicationDegree) throws NoSuchAlgorithmException, IOException, InterruptedException {

        if(logSystem)
            System.out.println("Remote interface Backup requested!");

        isInitiator = true;
        int numTransmission = 1;

        // CAN A REMOTE PEER ACCESS THE LAST MODIFICATION TIME, OR DO WE NEED TO GET THIS FROM THE fileData??????????????
        String lastModified = Long.toString(new java.io.File(pathname).lastModified());

        // Hashing the file id.
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String fileId = pathname + lastModified;
        md.update(fileId.getBytes(StandardCharsets.UTF_8));
        byte[] fileIdHashed = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : fileIdHashed) {
            sb.append(String.format("%02X", b));
        }
        String fileIdHashedStr = sb.toString();

        // Splitting the file into chunks.
        Splitter splitter = new Splitter(fileData);
        splitter.splitFile(replicationDegree);

        File file = new File(pathname, fileIdHashedStr, splitter.getChunks().size());
        manager.addFileToStorage(file);

        this.manager.addUploadingChunks(splitter.getChunks());

        if(logSystem)
            System.out.println("Starting to send chunks to the data channel!");

        boolean desiredReplicationDegree = false;
        do{
            if(numTransmission > BackupRetransmissions)
                break; // do something

            ArrayList<Chunk> uploadingChunks = this.manager.getUploading();
            Iterator<Chunk> it = uploadingChunks.iterator();
            while(it.hasNext()){
                Chunk c = it.next();
                MessageHeader header = new MessageHeader(Utils.MessageType.PUTCHUNK, protocolVersion, id, fileIdHashedStr, c.getChunkNo(), replicationDegree);
                MessageBody body = new MessageBody(c.getChunkData());
                Message message = new Message(header, body);
                byte[] buffer = message.getMessageBytes();
                mdb.sendMessage(buffer);
            }

            TimeUnit.MILLISECONDS.sleep(1000*numTransmission);

            int chunksToUpload = this.manager.chunksToUpload();
            if(chunksToUpload == 0)
                desiredReplicationDegree = true;
            else numTransmission++;
        } while(!desiredReplicationDegree);

        this.manager.resetUploadingChunks();

        isInitiator = false;
    }

    public void updateFileStorage(Message msgWrapper) {
        this.manager.updateStorage(msgWrapper);
        if(this.isInitiator)
            this.manager.updateUploadingChunks(msgWrapper);
    }

    public void deleteFileStorage(String fileId) throws IOException {
        this.manager.deleteStoredChunk(fileId);
    }

    @Override
    public void RestoreFile(String pathname) throws IOException, InterruptedException {
        isInitiator = true;

        File file = manager.getFile(pathname);
        if(file == null)
            return;

        String fileId = file.getFileId();
        int numChunks = file.getNumChunks();
        
        for(int i = 0; i < numChunks; i++) {
            MessageHeader header = new MessageHeader(Utils.MessageType.GETCHUNK, protocolVersion, id, fileId, (i+1));
            Message message = new Message(header);
            byte[] buf = message.getMessageBytes();
            mc.sendMessage(buf);
            TimeUnit.MILLISECONDS.sleep(1000);      // SHOULD IT WAIT??????????????????????????????????????
        }

        // Send file to client or restore the file in the curr peer????????????????????????????'

        isInitiator = false;
        canSendRestoreMessages = true;
    }

    /**
     * Restore protocol callable.
     * Called every time the MDR received a message.
     * Adds a chunk to the file system (if initiator-peer) or stops others from sending the message.
     * @param msgWrapper
     */
    public void receiveChunk(Message msgWrapper) {
        if(this.isInitiator)
            manager.addChunkToRestoring(msgWrapper);        // Saves the received chunk.
        else
            canSendRestoreMessages = false;     // If a non-initiator peer receives a 'CHUNK' message, this peer doesn't sends his message.
    }

    @Override
    public void DeleteFile(String pathname) throws IOException, NoSuchAlgorithmException {
        // CAN A REMOTE PEER ACCESS THE LAST MODIFICATION TIME, OR DO WE NEED TO GET THIS FROM THE fileData??????????????
        String lastModified = Long.toString(new java.io.File(pathname).lastModified());

        // Hashing the file id.
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String fileId = pathname + lastModified;
        md.update(fileId.getBytes("UTF-8"));
        byte[] fileIdHashed = md.digest();

        MessageHeader header = new MessageHeader(Utils.MessageType.DELETE, protocolVersion, id, fileIdHashed.toString());
        Message message = new Message(header);
        byte[] buffer = message.getMessageBytes();

        // We send 'Utils.DeleteRetransmissions' messages to make sure every chunk is properly deleted.
        for(int i = 0; i < Utils.DeleteRetransmissions; i++)
            mc.sendMessage(buffer);
    }

    @Override
    public void TestMessage(String message) throws IOException {
        byte[] buffer = message.getBytes();
        mc.sendMessage(buffer);
    }

    public static void main(String[] args) throws IOException, AlreadyBoundException {
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

        // Overrides the RMI connection to the actual server, instead of the localhost address.
        System.setProperty("java.rmi.server.hostname",Utils.IPV4_ADDRESS);

        Peer peer = new Peer(args[0], Integer.parseInt(args[1]), args[2], multicastInfo);

        if(peer.id == 1) {
            Registry registry;
            registry = LocateRegistry.createRegistry(Utils.RMI_PORT);
            registry.bind("IClientPeer", peer.getStub());
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

    public RestoreChannel getMdr() {
        return mdr;
    }

    public void setMdr(RestoreChannel mdr) {
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

    public boolean isCanSendRestoreMessages() {
        return canSendRestoreMessages;
    }

    public void setCanSendRestoreMessages(boolean canSendMessages) {
        this.canSendRestoreMessages = canSendMessages;
    }
}
