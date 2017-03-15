package fileSystem;

public class Chunk implements Comparable<Chunk> {

    private int chunkNo;
    private int replicationDegree;
    private int currReplicationDegree;
    private byte[] chunkData;

    public Chunk(int replicationDegree, int chunkNo, byte[] chunkData){
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.currReplicationDegree = 0;
        this.chunkData = chunkData;
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

    public void addReplicationDegree(){
        this.currReplicationDegree++;
    }

    public void subReplicationDegree(){
        this.currReplicationDegree--;
    }
}
