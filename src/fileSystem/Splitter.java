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
            for(int i = 64000; i <= fileData.length; i += 64000){
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
