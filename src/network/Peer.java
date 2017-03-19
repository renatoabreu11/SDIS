package network;

import channels.*;
import fileSystem.*;
import messageSystem.*;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Peer implements IClientPeer {

    private ControlChannel mc;
    private BackupChannel mdb;
    private RestoreChannel mdr;

    private String protocolVersion;
    private String serverAccessPoint;
    private int id;
    private IClientPeer stub;

    private boolean isInitiator = false;
    private FileManager manager;

    // Delete protocol auxiliar variables.
    private int numDeleteMessages = 3;

    // Restore protocol auxiliar variables.
    private ArrayList<FileAssociation> fileAssociations;
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
        fileAssociations = new ArrayList<>();

        System.out.println("All channels online.");
    }

    @Override
    public void BackupFile(byte[] fileData, String pathname, int replicationDegree) throws NoSuchAlgorithmException, IOException, InterruptedException {
        isInitiator = true;
        int numTransmission = 1;

        // CAN A REMOTE PEER ACCESS THE LAST MODIFICATION TIME, OR DO WE NEED TO GET THIS FROM THE fileData??????????????
        String lastModified = Long.toString(new File(pathname).lastModified());

        // Hashing the file id.
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String fileId = pathname + lastModified;
        md.update(fileId.getBytes("UTF-8"));
        byte[] fileIdHashed = md.digest();

        // Splitting the file into chunks.
        Splitter splitter = new Splitter(fileData);
        splitter.splitFile(replicationDegree, fileIdHashed.toString());

        // Adds a mapping between the 'pathname', the file id and the number of chunks.
        if(!HasAssociation(pathname)) {
            FileAssociation fileAssociation = new FileAssociation(pathname, fileId, splitter.getChunks().size());
            fileAssociations.add(fileAssociation);
        }

        this.manager.addUploadingChunks(splitter.getChunks());

        boolean desiredReplicationDegree = false;
        do{
            if(numTransmission > 5)
                break; // do something

            ArrayList<Chunk> uploadingChunks = this.manager.getUploading();
            Iterator<Chunk> it = uploadingChunks.iterator();
            while(it.hasNext()){
                Chunk c = it.next();
                MessageHeader header = new MessageHeader(Utils.MessageType.PUTCHUNK, protocolVersion, id, fileIdHashed.toString(), c.getChunkNo(), replicationDegree);
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

    /**
     * Checks to see if there's already a file association with the pathname.
     * @param pathname
     * @return
     */
    private boolean HasAssociation(String pathname) {
        for(int i = 0; i < fileAssociations.size(); i++) {
            if(fileAssociations.get(i).getPathname().equals(pathname))
                return true;
        }

        return false;
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

        FileAssociation fileAssociation = GetFileAssociation(pathname);
        if(fileAssociation == null)
            return;

        String fileId = fileAssociation.getFileId();
        int numChunks = fileAssociation.getTotalChunks();
        
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
     * Gets the file association with the pathname specified.
     * @param pathname
     * @return
     */
    private FileAssociation GetFileAssociation(String pathname) {
        for(int i = 0; i < fileAssociations.size(); i++) {
            if(fileAssociations.get(i).getPathname().equals(pathname))
                return fileAssociations.get(i);
        }

        return null;
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
        String lastModified = Long.toString(new File(pathname).lastModified());

        // Hashing the file id.
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String fileId = pathname + lastModified;
        md.update(fileId.getBytes("UTF-8"));
        byte[] fileIdHashed = md.digest();

        MessageHeader header = new MessageHeader(Utils.MessageType.DELETE, protocolVersion, id, fileIdHashed.toString());
        Message message = new Message(header);
        byte[] buffer = message.getMessageBytes();

        // We send 'numDeleteMessages' messages to make sure every chunk is properly deleted.
        for(int i = 0; i < numDeleteMessages; i++)
            mc.sendMessage(buffer);
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
