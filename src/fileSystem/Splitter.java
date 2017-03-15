package fileSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.copyOfRange;

public class Splitter {
    private String fileName;
    private ArrayList<Chunk> chunks;

    /**
     * @param fileName file full path!
     */
    public Splitter(String fileName){
        this.fileName = fileName;
        this.chunks = new ArrayList<>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
    }

    public void resetSplitter(){
        fileName = null;
        chunks.clear();
    }

    public void splitFile(int desiredReplicationDegree) throws IOException {
        Path path = Paths.get(this.fileName);
        byte[] data = Files.readAllBytes(path);

        int currentChunkNo = 0;

        if(data.length <= 64000){
            Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, data);
            chunks.add(c);
        }else{
            for(int i = 0; i < data.length; i++){
                if( i != 0 && i % 64000 == 0){
                    currentChunkNo++;
                    int minDelimiter = i - 64000;
                    int maxDelimiter = i;
                    byte[] chunkData = copyOfRange(data, minDelimiter, maxDelimiter);
                    Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, chunkData);
                    System.out.println(chunkData.length);
                    System.out.println(Arrays.toString(chunkData));
                    chunks.add(c);
                }
            }

            int nrChunks = currentChunkNo;
            int leftoverBytes = data.length - (nrChunks * 64000);
            if(leftoverBytes != 0){
                currentChunkNo++;
                int minDelimiter = data.length - leftoverBytes;
                int maxDelimiter = data.length;
                byte[] chunkData = copyOfRange(data, minDelimiter, maxDelimiter);
                Chunk c = new Chunk(desiredReplicationDegree, currentChunkNo, chunkData);
                System.out.println(chunkData.length);
                System.out.println(Arrays.toString(chunkData));
                chunks.add(c);
            }
        }
    }
}
