package fileSystem;

import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileManager {
    private Map<String, ArrayList<Chunk>> storage = new HashMap<>();
    private ArrayList<Chunk> uploading = new ArrayList<>();

    /**
     *
     */
    public FileManager(){}

    /**
     *
     * @return
     */
    public Map<String, ArrayList<Chunk>> getStorage() {
        return storage;
    }

    /**
     *
     * @param storage
     */
    public void setStorage(Map<String, ArrayList<Chunk>> storage) {
        this.storage = storage;
    }

    /**
     *
     */
    public void resetStorage(){
        this.storage.clear();
    }

    /**
     *
     */
    public void resetUploadingChunks(){
        this.uploading.clear();
    }

    /**
     *
     * @param fileId
     * @return
     */
    public ArrayList<Chunk> getFileStorage(String fileId){
        return storage.get(fileId);
    }

    /**
     *
     * @param chunks
     */
    public void addUploadingChunks(ArrayList<Chunk> chunks) {
        uploading = chunks;
    }

    /**
     *
     * @return
     */
    public ArrayList<Chunk> getUploading() {
        return uploading;
    }

    /**
     *
     * @param uploading
     */
    public void setUploading(ArrayList<Chunk> uploading) {
        this.uploading = uploading;
    }

    /**
     *
     * @return
     */
    public int chunksToUpload() {
        int nrChunksWithoutReplication = 0;

        Iterator<Chunk> it = uploading.iterator();
        while(it.hasNext()){
            if(it.next().desiredReplication()){
                it.remove();
            }else nrChunksWithoutReplication++;
        }

        return nrChunksWithoutReplication;
    }

    /**
     *
     * @param msgWrapper
     */
    public void updateStorage(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();

        ArrayList<Chunk> chunks = this.getFileStorage(fileId);
        if(chunks != null){
            Chunk c = new Chunk(fileId, chunkNo);
            for(int i = 0; i < chunks.size(); i++){
                if(c.equals(chunks.get(i))){
                    chunks.get(i).updateReplication(senderId);
                    break;
                }
            }
        }
    }

    /**
     *
     * @param msgWrapper
     */
    public void updateUploadingChunks(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        String version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();

        Chunk c = new Chunk(fileId, chunkNo);
        for(int i = 0; i < uploading.size(); i++){
            if(c.equals(uploading.get(i))){
                uploading.get(i).updateReplication(senderId);
                break;
            }
        }
    }

    /**
     * Delete protocol callable.
     * Deletes all chunk's files from the computer and the map.
     * @param fileId name of the file to delete.
     * @throws IOException
     */
    public void deleteStoredChunk(String fileId) throws IOException {
        ArrayList<Chunk> fileChunks = storage.get(fileId);
        if(fileChunks == null)
            return;

        // Removes all the chunk's files from the computer.
        for(int i = 0; i < fileChunks.size(); i++) {
            Chunk chunk = fileChunks.get(i);
            Path path = Paths.get(chunk.getFileId() + chunk.getChunkNo() + chunk.getFileExtension());
            Files.delete(path);
        }

        // Removes the entry on the map.
        storage.remove(fileId);
    }

    /**
     *
     * @param c
     */
    public void addChunkToStorage(Chunk c) {
        boolean containsFile = storage.containsKey(c.getFileId());
        if(!containsFile)
            storage.put(c.getFileId(), new ArrayList<>());

        storage.get(c.getFileId()).add(c);

    }
}
