package fileSystem;

import java.util.ArrayList;

public class Chunk{

    private String fileId;
    private String fileExtension;

    private int chunkNo;
    private int replicationDegree;
    private int currReplicationDegree;
    private ArrayList<Integer> peers = new ArrayList<>();
    private byte[] chunkData;

    /**
     *
     * @param fileId
     * @param replicationDegree
     * @param chunkNo
     * @param chunkData
     */
    public Chunk(String fileId, int replicationDegree, int chunkNo, byte[] chunkData){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.currReplicationDegree = 0;
        this.chunkData = chunkData;
    }

    /**
     *
     * @param fileId
     * @param chunkNo
     */
    public Chunk(String fileId, int chunkNo){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public Chunk(String fileId, int chunkNo, byte[] chunkData) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.chunkData = chunkData;
    }

    /**
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj){
        if (obj instanceof Chunk) {
            Chunk c = (Chunk) obj;
            return (c.fileId.equals(this.fileId) && c.chunkNo == this.chunkNo);
        } else
            return false;
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
    public byte[] getChunkData() {
        return chunkData;
    }

    /**
     *
     * @param chunkData
     */
    public void setChunkData(byte[] chunkData) {
        this.chunkData = chunkData;
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

    /**
     *
     * @return
     */
    public int getCurrReplicationDegree() {
        return currReplicationDegree;
    }

    /**
     *
     * @param currReplicationDegree
     */
    public void setCurrReplicationDegree(int currReplicationDegree) {
        this.currReplicationDegree = currReplicationDegree;
    }

    /**
     *
     * @return
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     *
     * @param fileExtension
     */
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     *
     * @return
     */
    public boolean desiredReplication(){
        return (replicationDegree == currReplicationDegree);
    }

    /**
     *
     */
    public void addReplicationDegree(){
        this.currReplicationDegree++;
    }

    /**
     *
     */
    public void subReplicationDegree(){
        this.currReplicationDegree--;
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
     * @param senderId
     */
    public void updateReplication(int senderId) {
        if(!peerHasChunk(senderId)){
            peers.add(senderId);
            addReplicationDegree();
        }
    }

    /**
     *
     * @param senderId
     * @return
     */
    private boolean peerHasChunk(int senderId) {
        for(int i = 0; i < this.peers.size(); i++){
            if(this.peers.get(i) == senderId)
                return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public ArrayList<Integer> getPeers() {
        return peers;
    }

    /**
     *
     * @param peers
     */
    public void setPeers(ArrayList<Integer> peers) {
        this.peers = peers;
    }
}
