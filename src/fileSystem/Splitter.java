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
    public void splitFile(int desiredReplicationDegree) throws IOException {
        int currentChunkNo = 0;

        if(fileData.length <= 64000){
            Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, fileData);
            chunks.add(c);
        }else{
            for(int i = 0; i < fileData.length; i++){
                if( i != 0 && i % 64000 == 0){
                    currentChunkNo++;
                    int minDelimiter = i - 64000;
                    int maxDelimiter = i;
                    byte[] chunkData = copyOfRange(fileData, minDelimiter, maxDelimiter);
                    Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, chunkData);
                    System.out.println(chunkData.length);
                    System.out.println(Arrays.toString(chunkData));
                    chunks.add(c);
                }
            }

            int nrChunks = currentChunkNo;
            int leftoverBytes = fileData.length - (nrChunks * 64000);
            if(leftoverBytes != 0){
                currentChunkNo++;
                int minDelimiter = fileData.length - leftoverBytes;
                int maxDelimiter = fileData.length;
                byte[] chunkData = copyOfRange(fileData, minDelimiter, maxDelimiter);
                Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, chunkData);
                System.out.println(chunkData.length);
                System.out.println(Arrays.toString(chunkData));
                chunks.add(c);
            }
        }
    }
}
