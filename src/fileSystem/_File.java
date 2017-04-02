package fileSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class _File {

    private String fileId;
    private String pathname;
    private ArrayList<Chunk> chunks = new ArrayList<>();
    private int numChunks;

    public _File(String pathname, String fileId, int totalChunks) {
        this.pathname = pathname;
        this.fileId = fileId;
        numChunks = totalChunks;
    }

    /**
     * Return the file pathname
     * @return String pathname
     */
    public String getPathname() {
        return pathname;
    }

    /**
     * Get the file identifier
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
     * Get the chunks that belong to this file
     * @return
     */
    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    /**
     * Set the array list of chunks that compose this file
     * @param chunks
     */
    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
    }

    /**
     * Add a new chunk to the file chunks, if it doesn't exist
     * @param c
     * @return
     */
    public boolean addChunk(Chunk c){
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).getChunkNo() == c.getChunkNo())
                return false;
        }
        chunks.add(c);
        if(numChunks < chunks.size())
            numChunks++;
        return true;
    }

    /**
     * Update the current replication degree of a chunk
     * @param chunkNo
     * @param senderId
     * @return
     */
    public boolean updateChunk(int chunkNo, int senderId) {
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).getChunkNo() == chunkNo){
                chunks.get(i).updateReplication(senderId);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the number of chunks from which the file is composed
     * @return
     */
    public int getNumChunks() {
        return numChunks;
    }

    /**
     * Returns the number of bytes all the chunks of this file occupies.
     * @return
     */
    public long getFileBytesSize() throws IOException {
        long numBytes = 0;

        for(Chunk chunk : chunks) {
            Path path = Paths.get("data/chunks/" + fileId + chunk.getChunkNo());
            long bytesSize = Files.readAllBytes(path).length;
            numBytes += bytesSize;
        }

        return numBytes;
    }

    public ArrayList<Chunk> getStoredChunks(int peer_id) {
        ArrayList<Chunk> storedChunks = new ArrayList<>();
        for(int i = 0; i < chunks.size(); i++){
            if(chunks.get(i).peerHasChunk(peer_id))
                storedChunks.add(chunks.get(i));
        }
        return storedChunks;
    }

    public void removeChunkPeer(int chunkNo, int id) {
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).getChunkNo() == chunkNo){
                chunks.get(i).removePeer(id);
                chunks.get(i).subReplicationDegree();
            }
        }
    }
}
