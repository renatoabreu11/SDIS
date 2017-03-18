package fileSystem;

import java.util.ArrayList;

public class Chunk implements Comparable<Chunk> {

    private String fileId;
    private String fileExtension;

    private int chunkNo;
    private int replicationDegree;
    private int currReplicationDegree;
    private ArrayList<Integer> peers = new ArrayList<>();
    private byte[] chunkData;

    public Chunk(String fileId, int replicationDegree, int chunkNo, byte[] chunkData){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.currReplicationDegree = 0;
        this.chunkData = chunkData;
    }

    public Chunk(String fileId, int chunkNo){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    public boolean equals(Object obj){
        if (obj instanceof Chunk) {
            Chunk c = (Chunk) obj;
            return (c.fileId.equals(this.fileId) && c.chunkNo == this.chunkNo);
        } else
            return false;
    }

    @Override
    public int compareTo(Chunk chunk) {
        return 0;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    public byte[] getChunkData() {
        return chunkData;
    }

    public void setChunkData(byte[] chunkData) {
        this.chunkData = chunkData;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    public int getCurrReplicationDegree() {
        return currReplicationDegree;
    }

    public void setCurrReplicationDegree(int currReplicationDegree) {
        this.currReplicationDegree = currReplicationDegree;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public boolean desiredReplication(){
        return (replicationDegree == currReplicationDegree);
    }

    public void addReplicationDegree(){
        this.currReplicationDegree++;
    }

    public void subReplicationDegree(){
        this.currReplicationDegree--;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void updateReplication(int senderId) {
        if(!peerHasChunk(senderId)){
            peers.add(senderId);
            addReplicationDegree();
        }
    }

    private boolean peerHasChunk(int senderId) {
        for(int i = 0; i < this.peers.size(); i++){
            if(this.peers.get(i) == senderId)
                return true;
        }
        return false;
    }

    public ArrayList<Integer> getPeers() {
        return peers;
    }

    public void setPeers(ArrayList<Integer> peers) {
        this.peers = peers;
    }
}
