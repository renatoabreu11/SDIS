package network;

import channels.*;
import fileSystem.*;
import messageSystem.*;
import protocols.ProtocolDispatcher;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.AlreadyBoundException;
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
    private ProtocolDispatcher dispatcher;

    private String protocolVersion;
    private String serverAccessPoint;
    private int id;
    private IClientPeer stub;

    private boolean isInitiator = false;
    private boolean logSystem = true;
    private FileManager manager;

    // Restore protocol auxiliar variables.
    private boolean canSendRestoreMessages = true;

    // Manage disk space auxiliar variables.
    private long maxDiskSpace = 74;

    public Peer(String protocolVersion, int id, String serverAccessPoint, String[] multicastInfo) throws IOException {
        this.protocolVersion = protocolVersion;
        this.id = id;
        this.serverAccessPoint = serverAccessPoint;
        manager = new FileManager();

        mc = new ControlChannel(multicastInfo[0], multicastInfo[1], this);
        mdb = new BackupChannel(multicastInfo[2], multicastInfo[3], this);
        mdr = new RestoreChannel(multicastInfo[4], multicastInfo[5], this);
        dispatcher = new ProtocolDispatcher(this);

        new Thread(mc).start();
        new Thread(mdb).start();
        new Thread(mdr).start();
        new Thread(dispatcher).start();

        if(id == 1)
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

        // Adds a mapping between the 'pathname', the file id and the number of chunks.
        //Aqui é o file id com hash ou sem?
        _File file = new _File(pathname, fileId, splitter.getChunks().size());
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

        _File file = manager.getFile(pathname);
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

    /**
     * Client callable.
     * Handles the disk space.
     * @param client_maxDiskSpace
     * @throws IOException
     */
    @Override
    public void ManageDiskSpace(long client_maxDiskSpace) throws IOException {
        long freeCurrSpace;

        switch(System.getProperty("os.name")) {
            case "Linux":
                freeCurrSpace = new File("/").getFreeSpace() / 1000;
                break;
            case "Windows":
                freeCurrSpace = new File("C:").getFreeSpace() / 1000;
                break;
            default: freeCurrSpace = 0; break;
        }

        if(client_maxDiskSpace > freeCurrSpace) {
            System.out.println("The machine hosting the peer doesn't have that much free space.");
            return;
        }

        if(client_maxDiskSpace < manager.getCurrOccupiedSize())
            manageChunks(client_maxDiskSpace);

        maxDiskSpace = client_maxDiskSpace;
    }

    /**
     * Returns all the chunks stored in the peer sorted by their duplication degree.
     * @return
     */
    private ArrayList<Chunk> deleteFilesHigherRD() {
        Map<String, _File> storedFiles = manager.getStorage();
        Iterator it = storedFiles.entrySet().iterator();

        ArrayList<Chunk> chunkList = new ArrayList<>();
        while(it.hasNext()) {
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            _File file = entry.getValue();

            for(int i = 0; i < file.getNumChunks(); i++)
                chunkList.add(file.getChunks().get(i));
        }

        Collections.sort(chunkList);
        return chunkList;
    }

    /**
     * Removes the chunks which have a higher replication degree until the available space is
     * lower or equal than the amount the user specified.
     * Sends a message to the MC every time a chunk is deleted, ir order to try to maintain the
     * replication degree.s
     * @param client_maxDiskSpace
     * @throws IOException
     */
    private void manageChunks(long client_maxDiskSpace) throws IOException {
        ArrayList<Chunk> orderedChunks = deleteFilesHigherRD();
        int i = 0;
        Chunk currChunkToDelete = orderedChunks.get(i);
        boolean found = false;

        do {
            Iterator it = manager.getStorage().entrySet().iterator();

            // Searches for the file which contains the chunk to be removed,
            // because we need to assign to the message a fileId.
            while(it.hasNext()) {
                Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
                _File file = entry.getValue();

                if(file.getChunks().contains(currChunkToDelete)) {
                    found = true;
                    String fileId = entry.getKey();
                    MessageHeader header = new MessageHeader(Utils.MessageType.REMOVED, protocolVersion, id, fileId, currChunkToDelete.getChunkNo());
                    Message message = new Message(header);
                    byte[] buffer = message.getMessageBytes();
                    mc.sendMessage(buffer);
                    break;
                }
            }

            // Safety measure.
            if(!found) {
                System.out.println("ERROR: couldn't find the file to remove. Aborting...");
                return;
            }

            i++;
            currChunkToDelete = orderedChunks.get(i);
            found = false;
        } while(client_maxDiskSpace < manager.getCurrOccupiedSize());
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

        System.out.println(Arrays.toString(multicastInfo));

        // Overrides the RMI connection to the actual server, instead of the localhost address.
        System.setProperty("java.rmi.server.hostname",Utils.IPV4_ADDRESS);

        Peer peer = new Peer(args[0], Integer.parseInt(args[1]), args[2], multicastInfo);

        Registry registry;
        if(peer.id == 1){
            registry = LocateRegistry.createRegistry(Utils.RMI_PORT);
            registry.bind("IClientPeer", peer.getStub());
        }
        else
            registry = LocateRegistry.getRegistry();

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

    public ProtocolDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(ProtocolDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }


    public void setCanSendRestoreMessages(boolean canSendMessages) {
        this.canSendRestoreMessages = canSendMessages;
    }

    public long getMaxDiskSpace() {
        return maxDiskSpace;
    }

    public void setMaxDiskSpace(long maxDiskSpace) {
        this.maxDiskSpace = maxDiskSpace;
    }
}
