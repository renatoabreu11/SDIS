package network;

import channels.*;
import fileSystem.*;
import messageSystem.*;
import protocols.ProtocolDispatcher;
import protocols.initiator.BackupInitiator;
import protocols.initiator.DeleteInitiator;
import protocols.initiator.ProtocolInitiator;
import protocols.initiator.RestoreInitiator;
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
    private ProtocolInitiator protocol = null;
    private FileManager manager;

    private int id;
    private String protocolVersion;
    private String serverAccessPoint;
    private IClientPeer stub;

    // Backup protocol auxiliar variables.
    private ArrayList<Chunk> chunkBackingUp = new ArrayList<>();

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
        protocol = new BackupInitiator(protocolVersion, true, this, fileData, pathname, replicationDegree);
        protocol.startProtocol();
        protocol.endProtocol();
        protocol = null;
    }

    public void updateFileStorage(Message msgWrapper) {
        this.manager.updateStorage(msgWrapper);
        if(this.protocol instanceof BackupInitiator)
            ((BackupInitiator) this.protocol).updateUploadingChunks(msgWrapper);
    }

    public void deleteFileStorage(String fileId) throws IOException {
        this.manager.deleteStoredChunks(fileId);
    }

    @Override
    public void RestoreFile(String pathname) throws IOException, InterruptedException {
        protocol = new RestoreInitiator(protocolVersion, true, this, pathname);
        protocol.startProtocol();
        protocol.endProtocol();
        protocol = null;
    }

    /**
     * Restore protocol callable.
     * Called every time the MDR received a message.
     * Adds a chunk to the file system (if initiator-peer) or stops others from sending the message.
     * @param msgWrapper
     */
    public void receiveChunk(Message msgWrapper) {
        if(this.protocol instanceof BackupInitiator)
            manager.addChunkToRestoring(msgWrapper);
    }

    @Override
    public void DeleteFile(String pathname) throws IOException, NoSuchAlgorithmException {
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
            ManageChunks(client_maxDiskSpace);

        maxDiskSpace = client_maxDiskSpace;
    }

    /**
     * Removes the chunks which have a higher replication degree until the available space is
     * lower or equal than the amount the user specified.
     * Sends a message to the MC every time a chunk is deleted, ir order to try to maintain the
     * replication degree.s
     * @param client_maxDiskSpace
     * @throws IOException
     */
    private void ManageChunks(long client_maxDiskSpace) throws IOException {
        ArrayList<Chunk> orderedChunks = GetFilesHigherRD();
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
                    manager.deleteStoredChunk(fileId, currChunkToDelete.getChunkNo());
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

    /**
     * Returns all the chunks stored in the peer sorted by their duplication degree.
     * @return
     */
    private ArrayList<Chunk> GetFilesHigherRD() {
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

    @Override
    public void RetrieveInformation() throws IOException {
        String out = "";
        Iterator it = manager.getStorage().entrySet().iterator();

        while(it.hasNext()) {
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            String fileId = entry.getKey();
            _File file = entry.getValue();

            out += "File pathname: " + file.getPathname() + ", id: " + fileId + ", desired replication degree: " + file.getChunks().get(0).getReplicationDegree();
            for(Chunk chunk : file.getChunks())
                out += "\n\tChunk id: " + chunk.getChunkNo() + ", size: " + chunk.getChunkData().length + ", current replication degree: " + chunk.getCurrReplicationDegree();

            out += "\n\n";
        }

        out += "Peer's storage capacity: " + maxDiskSpace + ", current occupied storage: " + manager.getCurrOccupiedSize() + "\n";
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
        System.setProperty("java.rmi.server.hostname", Utils.IPV4_ADDRESS);

        Peer peer = new Peer(args[0], Integer.parseInt(args[1]), args[2], multicastInfo);

        Registry registry;
        if(peer.id == 1){
            registry = LocateRegistry.createRegistry(Utils.RMI_PORT);
            registry.bind("IClientPeer", peer.getStub());
        } else
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

    public FileManager getManager() {
        return manager;
    }

    public void setManager(FileManager manager) {
        this.manager = manager;
    }

    public ProtocolDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(ProtocolDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public long getMaxDiskSpace() {
        return maxDiskSpace;
    }

    public void setMaxDiskSpace(long maxDiskSpace) {
        this.maxDiskSpace = maxDiskSpace;
    }

    public ArrayList<Chunk> getChunkBackingUp() {
        return chunkBackingUp;
    }

    public void setChunkBackingUp(ArrayList<Chunk> chunkBackingUp) {
        this.chunkBackingUp = chunkBackingUp;
    }

    public void addChunkBackingUp(Chunk chunk) {
        this.chunkBackingUp.add(chunk);
    }

    public void removeChunkBackingUp(Chunk chunk) {
        this.chunkBackingUp.remove(chunk);
    }

    public void addMessageToDispatcher(String message) {
        dispatcher.addMessage(message);
    }

    public ProtocolInitiator getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolInitiator protocol) {
        this.protocol = protocol;
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
}
