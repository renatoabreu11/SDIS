package fileSystem;

import utils.Utils;

import java.io.File;
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

    public String toString(){
        String s = "FileId: " + fileId + " pathname: " + pathname + " numChunks: " + numChunks;
        return s;
    }

    public Chunk getChunk(int id){
      for(Chunk c : chunks)
        if(c.getChunkNo() == id)
          return c;

      return null;
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
            if(chunks.get(i).getChunkNo() == c.getChunkNo()){
                return false;
            }
        }
        chunks.add(c);
        if(numChunks < chunks.size())
            numChunks++;
        return true;
    }

    public synchronized boolean addChunkReceived(Chunk c, int peer, boolean message){
        if(message) //putchunk
            addChunkThroughPutchunk(c, peer);
        else return addChunkThroughStored(c, peer);
        return true;
    }

    public synchronized void addChunkThroughPutchunk(Chunk c, int peer){
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).getChunkNo() == c.getChunkNo()){
                chunks.get(i).updateReplication(c.getReplicationDegree());
                chunks.get(i).addPeer(peer);
                return;
            }
        }

        c.addPeer(peer);
        chunks.add(c);
        if(numChunks < chunks.size())
            numChunks++;
    }

    public synchronized boolean addChunkThroughStored(Chunk c, int peer){
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).getChunkNo() == c.getChunkNo())
                return chunks.get(i).addPeer(peer);
        }

        c.addPeer(peer);
        chunks.add(c);
        if(numChunks < chunks.size())
            numChunks++;
        return true;
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
    public long getFileBytesSize(int id) throws IOException {
        long numBytes = 0;

        for(Chunk chunk : chunks) {
            //Só faz isto se o peer tiver o chunk guardado. Na storage está a info de todos os chunks!!!
            if(chunk.peerHasChunk(id)){
                Path path = Paths.get(Utils.CHUNKS_DIR + fileId + chunk.getChunkNo());
                long bytesSize = Files.readAllBytes(path).length;
                numBytes += bytesSize;
            }
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
            if(chunks.get(i).getChunkNo() == chunkNo)
                chunks.get(i).removePeer(id);
        }
    }

    public ArrayList<Chunk> getStoredChunksWithHigherRD(int id) {
        ArrayList<Chunk> storedChunks = new ArrayList<>();
        for(int i = 0; i < chunks.size(); i++){
            if(chunks.get(i).peerHasChunk(id) && chunks.get(i).higherRD())
                storedChunks.add(chunks.get(i));
        }
        return storedChunks;
    }

    public ArrayList<Integer> getAllPeers() {
        ArrayList<Integer> allPeers = new ArrayList<>();

        for(Chunk chunk : chunks) {
            for(int i = 0; i < chunk.getPeers().size(); i++) {
                if(!allPeers.contains(chunk.getPeers().get(i)))
                    allPeers.add(chunk.getPeers().get(i));
            }
        }

        return allPeers;
    }

    public void updateReplication(int replicationDegree) {
        for(Chunk chunk : chunks) {
            chunk.setReplicationDegree(replicationDegree);
        }
    }
}
