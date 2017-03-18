package backupService;

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

    public String getMessageHeaderAsString(){
        String header;
        switch(messageType){
            case DELETED:
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

    public byte[] getMessageHeaderAsBytes(){
        String header = getMessageHeaderAsString();
        return header.getBytes();
    }

    public Utils.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(Utils.MessageType messageType) {
        this.messageType = messageType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}
