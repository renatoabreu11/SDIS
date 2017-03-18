package fileSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.copyOfRange;

public class Splitter {
    private byte[] fileData;
    private ArrayList<Chunk> chunks;

    /**
     * @param fileData file in bytes.
     */
    public Splitter(byte[] fileData){
        this.fileData = fileData;
        this.chunks = new ArrayList<>();
    }

    public byte[] getFileName() {
        return fileData;
    }

    public void setFileName(byte[] fileName) {
        this.fileData = fileName;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void resetSplitter(){
        fileData = null;
        chunks.clear();
    }

    public Chunk getChunk(int i){
        return this.chunks.get(i);
    }

    public byte[] getChunkData(int i){
        return this.chunks.get(i).getChunkData();
    }

    public void splitFile(int desiredReplicationDegree, String fileId) throws IOException {
        int currentChunkNo = 0;

        if(fileData.length <= 64000){
            Chunk c = new Chunk(fileId, desiredReplicationDegree, currentChunkNo, fileData);
            chunks.add(c);
        }else{
            for(int i = 0; i < fileData.length; i++){
                if( i != 0 && i % 64000 == 0){
                    currentChunkNo++;
                    int minDelimiter = i - 64000;
                    int maxDelimiter = i;
                    byte[] chunkData = copyOfRange(fileData, minDelimiter, maxDelimiter);
                    Chunk c = new Chunk(fileId, desiredReplicationDegree, currentChunkNo, chunkData);
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
                Chunk c = new Chunk(fileId, desiredReplicationDegree, currentChunkNo, chunkData);
                System.out.println(chunkData.length);
                System.out.println(Arrays.toString(chunkData));
                chunks.add(c);
            }
        }
    }
}
