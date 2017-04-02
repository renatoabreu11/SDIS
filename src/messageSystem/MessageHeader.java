package messageSystem;

import utils.Utils;

public class MessageHeader {
    private Utils.MessageType messageType;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo = -1;
    private int replicationDegree = -1;

    /**
     * Putchunk protocol header
     * @param messageType
     * @param version
     * @param senderId
     * @param fileId
     * @param chunkNo
     * @param replicationDegree
     */
    public MessageHeader(Utils.MessageType messageType, String version, int senderId, String fileId, int chunkNo, int replicationDegree){
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
    }

    /**
     * Stored, Getchunk, Chunk and Removed protocol header
     * @param messageType
     * @param version
     * @param senderId
     * @param fileId
     * @param chunkNo
     */
    public MessageHeader(Utils.MessageType messageType, String version, int senderId, String fileId, int chunkNo){
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    /**
     * Delete protocol header
     * @param messageType
     * @param version
     * @param senderId
     * @param fileId
     */
    public MessageHeader(Utils.MessageType messageType, String version, int senderId, String fileId){
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }

    /**
     * Creates a string accordingly to the message header
     * @return
     */
    public String getMessageHeaderAsString(){
        String header;
        switch(messageType){
            case DELETE:
                header = messageType + " " + version + " " + senderId + " " + fileId + " " +  Utils.CRLF + Utils.CRLF;
                break;
            case PUTCHUNK:
                header = messageType + " " + version + " " + senderId + " " + fileId + " " + chunkNo + " " + replicationDegree + " " + Utils.CRLF + Utils.CRLF;
                break;
            default:
                header = messageType + " " + version + " " + senderId + " " + fileId + " " + chunkNo + " " + Utils.CRLF + Utils.CRLF;
        }
        return header;
    }

    /**
     * Get the type of message
     * @return
     */
    public Utils.MessageType getMessageType() {
        return messageType;
    }

    /**
     * Get the protocol version currently being used
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the peer that sent the message
     * @return
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Get the identifier of the file sent through the message body
     * @return
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Set the file identifier
     * @param fileId
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Get the chunk number of the file sent through the message body
     * @return
     */
    public int getChunkNo() {
        return chunkNo;
    }

    /**
     * Get the file replication degree
     * @return
     */
    public int getReplicationDegree() {
        return replicationDegree;
    }
}
