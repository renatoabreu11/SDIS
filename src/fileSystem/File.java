package fileSystem;

import java.util.ArrayList;

public class File {

    private String fileId;
    private String pathname;
    private ArrayList<Chunk> chunks = new ArrayList<>();
    private int numChunks;
    private String fileExtension; //Um file pode não ter extensão, não esquecer. Os chunks não precisam de conhecer a sua extensão, penso eu

    public File(String pathname, String fileId, int totalChunks) {
        this.pathname = pathname;
        this.fileId = fileId;
        numChunks = totalChunks;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

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

    public boolean removeChunk(Chunk c){
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).equals(c)){
                chunks.remove(i);
                return true;
            }
        }

        return false;
    }

    public boolean updateChunk(int chunkNo, int senderId) {
        for(int i = 0; i < this.chunks.size(); i++){
            if(chunks.get(i).getChunkNo() == chunkNo){
                chunks.get(i).updateReplication(senderId);
                return true;
            }
        }
        return false;
    }

    public int getNumChunks() {
        return numChunks;
    }

    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }
}
