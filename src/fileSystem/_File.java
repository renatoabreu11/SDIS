package fileSystem;

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
     * Set the file pathname
     * @param pathname
     */
    public void setPathname(String pathname) {
        this.pathname = pathname;
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
        boolean found = false;
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).equals(c)){
                found = true;
                break;
            }
        }

        if(found)
            return false;
        else chunks.add(c);

        return true;
    }

    /**
     * Remove a chunk from the list of file chunks
     * @param c
     * @return
     */
    public boolean removeChunk(Chunk c){
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).equals(c)){
                chunks.remove(i);
                return true;
            }
        }

        return false;
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
     * Set the number of chunks
     * @param numChunks
     */
    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }

    /**
     * Returns the number of bytes all the chunks of this file occupies.
     * @return
     */
    public long getFileBytesSize() {
        long numBytes = 0;

        for(Chunk chunk : chunks)
            numBytes += chunk.getChunkData().length;

        return numBytes;
    }
}
