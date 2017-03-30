package fileSystem;

import java.util.ArrayList;
import java.util.Arrays;

public class Chunk implements Comparable<Chunk>{

    private int chunkNo;
    private int replicationDegree;
    private int currReplicationDegree;
    private ArrayList<Integer> peers = new ArrayList<>();
    private byte[] chunkData;
    private String fileId;

    /**
     *
     * @param replicationDegree
     * @param chunkNo
     * @param chunkData
     */
    public Chunk( int replicationDegree, int chunkNo, byte[] chunkData, String fileId){
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.currReplicationDegree = 0;
        this.chunkData = chunkData;
        this.fileId = fileId;
    }

    /**
     *
     * @param chunkNo
     */
    public Chunk(int chunkNo){
        this.chunkNo = chunkNo;
    }

    /**
     *
     * @param chunkNo
     * @param chunkData
     */
    public Chunk(int chunkNo, byte[] chunkData) {
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
            // SEE IF THIS DOESN'T BLOW UP THE COMPARATION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            return (c.chunkNo == this.chunkNo && Arrays.equals(c.chunkData, this.chunkData));
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

    @Override
    public int compareTo(Chunk chunk) {
        if(this.chunkNo > chunk.getChunkNo())
            return 1;
        else
            return 0;
    }

    public String getFileId() {
        return fileId;
    }
}
