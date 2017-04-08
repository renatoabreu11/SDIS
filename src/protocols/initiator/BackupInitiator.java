package protocols.initiator;

import fileSystem.Chunk;
import fileSystem.Splitter;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static utils.Utils.BackupRetransmissions;

public class BackupInitiator extends ProtocolInitiator{

    private byte[] fileData;
    private String pathname;
    private int replicationDegree;
    private int numTransmission;
    private ArrayList<Chunk> uploading = new ArrayList<>();
    private boolean fromManageProtocol;     // True when the backup is initialized by the Manage Protocol.
    private enum protocolState{
        INIT,
        BACKUPMESSAGE,
        EXCEEDETRANSMISSIONS,
        SUCCESS
        }
    private protocolState currState;
    private byte[] chunkData;

    public BackupInitiator(String version, boolean logSystem, Peer parentPeer, byte[] fileData, String pathname, int replicationDegree) {
        super(version, logSystem, parentPeer);
        logMessage("Init Backup protocol...");
        this.fileData = fileData;
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
        this.numTransmission = 1;
        currState = protocolState.INIT;
        this.fromManageProtocol = false;
    }

    /**
     * Manage Storage callable.
     * @param version
     * @param logSystem
     * @param parentPeer
     * @param chunk
     * @param chunkData
     */
    public BackupInitiator(String version, boolean logSystem, Peer parentPeer, Chunk chunk, byte[] chunkData) {
        super(version, logSystem, parentPeer);
        this.numTransmission = 1;
        this.chunkData = chunkData;
        this.fromManageProtocol = true;
        uploading.add(chunk);
    }

    public void startProtocol(){
        if(fromManageProtocol)
            uploadChunks(uploading.get(0).getFileId());
        else {
            logMessage("Creating file identifier...");

            String fileId = createFileId();

            // Only splits the fileData if the backup protocol was started by the interface (and not the Manage Protocol).
            logMessage("Splitting file in multiple chunks...");
            Splitter splitter = new Splitter(fileData);
            try {
                splitter.splitFile(replicationDegree, fileId);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Never saves the file if the backup was started due to the Manage protocol.
            _File file = new _File(pathname, fileId, splitter.getChunks().size());
            this.getParentPeer().saveFileToStorage(file);
            uploading = splitter.getChunks();


            logMessage("Sending backup messages...");
            this.currState = protocolState.BACKUPMESSAGE;
            uploadChunks(fileId);
        }
    }

    private void uploadChunks(String fileId) {
        boolean desiredReplicationDegree = false;
        do{
            if(numTransmission > BackupRetransmissions) {
                logMessage("WARNING: number of retransmission exceeded. Aborting...");
                this.currState = protocolState.EXCEEDETRANSMISSIONS;
                return;
            }

            Iterator<Chunk> it = uploading.iterator();
            while(it.hasNext()){
                Chunk c = it.next();
                MessageHeader header = new MessageHeader(Utils.MessageType.PUTCHUNK, getVersion(), getParentPeer().getId(), fileId, c.getChunkNo(), replicationDegree);
                MessageBody body;
                if (fromManageProtocol) body = new MessageBody(chunkData);
                else body = new MessageBody(c.getChunkData());
                Message message = new Message(header, body);

                byte[] buffer = new byte[0];
                try {
                    buffer = message.getMessageBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getParentPeer().sendMessageMDB(buffer);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(1000*numTransmission);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int chunksToUpload = chunksToUpload();
            if(chunksToUpload == 0)
                desiredReplicationDegree = true;
            else numTransmission++;
        } while(!desiredReplicationDegree);

        uploading.clear();
        this.currState = protocolState.SUCCESS;
    }

    public void updateUploadingChunks(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        int senderId = header.getSenderId();
        int chunkNo = header.getChunkNo();

        Chunk c = new Chunk(chunkNo);
        for(int i = 0; i < uploading.size(); i++){
            if(c.getChunkNo() == uploading.get(i).getChunkNo()){
                logMessage("Updating replication degree");
                uploading.get(i).addPeer(senderId);
                break;
            }
        }
    }

    public String createFileId(){
        String lastModified = Long.toString(new java.io.File(pathname).lastModified());

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String fileInfo = pathname + replicationDegree + lastModified;
        md.update(fileInfo.getBytes(StandardCharsets.UTF_8));
        byte[] fileIdHashed = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : fileIdHashed) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public int chunksToUpload() {
        int nrChunksWithoutReplication = 0;

        Iterator<Chunk> it = uploading.iterator();
        while(it.hasNext()){
            Chunk c = it.next();
            if(c.getCurrReplicationDegree() >= c.getReplicationDegree()){
                it.remove();
            }else nrChunksWithoutReplication++;
        }

        return nrChunksWithoutReplication;
    }

    public String endProtocol() {
        if(this.currState == protocolState.EXCEEDETRANSMISSIONS)
            return "Backup Protocol Failure";
        else if(this.currState == protocolState.SUCCESS)
            return "Backup Protocol Success";
        else return "Beep Boop Error Beep Boop";
    }
}
