package fileSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.copyOfRange;

public class Splitter {
    private byte[] fileData;
    private ArrayList<Chunk> chunks = new ArrayList<>();

    /**
     * Splitter constructor
     * @param fileData file in bytes.
     */
    public Splitter(byte[] fileData){
        this.fileData = fileData;
    }

    /**
     * Gets the byte array that contains the file data
     * @return
     */
    public byte[] getFileData() {
        return fileData;
    }

    /**
     * Set the byte array that contains the file data
     * @param fileData
     */
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    /**
     * Get the all chunks
     * @return
     */
    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    /**
     * Set the chunks list
     * @param chunks
     */
    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
    }

    /**
     * Clears the file data and the respective chunks
     */
    public void resetSplitter(){
        fileData = null;
        chunks.clear();
    }

    /**
     * Get the chunk at the given position
     * @param i
     * @return
     */
    public Chunk getChunk(int i){
        return this.chunks.get(i);
    }

    /**
     * Get the chunk data at the given position
     * @param i
     * @return
     */
    public byte[] getChunkData(int i){
        return this.chunks.get(i).getChunkData();
    }

    /**
     * Accordingly to the filedata, splits the data in chunks of 64KB.
     * @param desiredReplicationDegree
     * @throws IOException
     */
    public void splitFile(int desiredReplicationDegree, String fileId) throws IOException {
        int currentChunkNo = 0;

        if(fileData.length <= 64000){
            Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, fileData, fileId);
            chunks.add(c);
            if(fileData.length == 64000){
                currentChunkNo++;
                byte[] emptyChunk = new byte[0];
                Chunk lastC = new Chunk(desiredReplicationDegree, currentChunkNo, emptyChunk, fileId);
                chunks.add(lastC);
            }
        }  else{
            for(int i = 64000; i < fileData.length; i += 64000){
                int minDelimiter = i - 64000;
                int maxDelimiter = i;
                byte[] chunkData = copyOfRange(fileData, minDelimiter, maxDelimiter);
                Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, chunkData, fileId);
                chunks.add(c);
                currentChunkNo++;
            }

            int nrChunks = currentChunkNo;
            int leftoverBytes = fileData.length - (nrChunks * 64000);
            if(leftoverBytes != 0){
                int minDelimiter = fileData.length - leftoverBytes;
                int maxDelimiter = fileData.length;
                byte[] chunkData = copyOfRange(fileData, minDelimiter, maxDelimiter);
                Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, chunkData, fileId);
                chunks.add(c);
            }else{
                byte[] emptyChunk = new byte[0];
                Chunk lastC = new Chunk(desiredReplicationDegree, currentChunkNo, emptyChunk, fileId);
                chunks.add(lastC);
            }
        }
    }
}
