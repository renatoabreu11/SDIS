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
     *
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
     *
     * @return
     */
    public byte[] getMessageHeaderAsBytes(){
        String header = getMessageHeaderAsString();
        return header.getBytes();
    }

    /**
     *
     * @return
     */
    public Utils.MessageType getMessageType() {
        return messageType;
    }

    /**
     *
     * @param messageType
     */
    public void setMessageType(Utils.MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     *
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     *
     * @return
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     *
     * @param senderId
     */
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    /**
     *
     * @return
     */
    public String getFileId() {
        return fileId;
    }

    /**
     *
     * @param fileId
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     *
     * @return
     */
    public int getChunkNo() {
        return chunkNo;
    }

    /**
     *
     * @param chunkNo
     */
    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    /**
     *
     * @return
     */
    public int getReplicationDegree() {
        return replicationDegree;
    }

    /**
     *
     * @param replicationDegree
     */
    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}
