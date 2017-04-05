package fileSystem;

import java.util.ArrayList;
import java.util.Arrays;

public class Chunk implements Comparable<Chunk>{

    private int chunkNo;
    private int replicationDegree;
    private int currReplicationDegree;
    private boolean receivedPutChunk;
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
        receivedPutChunk = true;
    }

    public Chunk( int replicationDegree, int chunkNo, String fileId){
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.currReplicationDegree = 0;
        this.fileId = fileId;
        receivedPutChunk = true;
    }

    /**
     *
     * @param chunkNo
     * @param fileId
     */
    public Chunk(int chunkNo, String fileId){
        this.chunkNo = chunkNo;
        this.fileId = fileId;
        replicationDegree = -1;
        receivedPutChunk = false;
    }

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
     * LoadMetadata() callable.
     * @param replicationDegree
     * @param fileId
     * @param chunkNo
     * @param currReplicationDegree
     * @param peers
     */
    public Chunk(int replicationDegree, String fileId, int chunkNo, int currReplicationDegree, ArrayList<Integer> peers) {
        this.replicationDegree = replicationDegree;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.currReplicationDegree = currReplicationDegree;

        for(int i = 0; i < peers.size(); i++)
            this.peers.add(peers.get(i));
    }

    /**
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj){
        if (obj instanceof Chunk) {
            Chunk c = (Chunk) obj;
            return (c.chunkNo == this.chunkNo);
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
     * @return
     */
    public byte[] getChunkData() {
        return chunkData;
    }

    /**
     *
     * @return
     */
    public void setReplicationDegree(int rd) {
        replicationDegree = rd;
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
     * @return
     */
    public int getCurrReplicationDegree() {
        return currReplicationDegree;
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
    public boolean updateReplication(int senderId, int desiredRD) {
        if(desiredRD != -1 && this.replicationDegree != desiredRD){
            receivedPutChunk = true;
            this.replicationDegree = desiredRD;
        }
        if(!peerHasChunk(senderId)){
            if(!receivedPutChunk)
                replicationDegree++;
            peers.add(senderId);
            addReplicationDegree();
            return true;
        }
        return false;
    }

    /**
     *
     * @param senderId
     * @return
     */
    public boolean peerHasChunk(int senderId) {
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

    public void removePeer(int id) {
        for(int i = 0; i < this.peers.size(); i++){
            if(this.peers.get(i) == id)
                this.peers.remove(i);
        }
    }

    public boolean higherRD() {
        return (currReplicationDegree > replicationDegree);
    }
}
