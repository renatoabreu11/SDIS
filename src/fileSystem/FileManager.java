package fileSystem;

import messageSystem.Message;
import messageSystem.MessageHeader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileManager {
    private Map<Chunk, ArrayList<Integer>> storage = new HashMap<>();
    private Map<Chunk, ArrayList<Integer>> uploading = new HashMap<>();

    public FileManager(){}

    public Map<Chunk, ArrayList<Integer>> getStorage() {
        return storage;
    }

    public void setStorage(Map<Chunk, ArrayList<Integer>> storage) {
        this.storage = storage;
    }

    public void resetStorage(){
        this.storage.clear();
    }

    public void resetUploadingChunks(){
        this.uploading.clear();
    }

    public ArrayList<Integer> getChunkStorage(String fileId, int chunkNo){
        Chunk lookupChunk = new Chunk(fileId, chunkNo);
        if(storage.containsKey(lookupChunk)){
            return storage.get(lookupChunk);
        }
        return null;
    }

    public void addUploadingChunks(ArrayList<Chunk> chunks) {
        for(int i = 0; i < chunks.size(); i++){
            this.uploading.put(chunks.get(i), new ArrayList<>());
        }
    }

    public Map<Chunk, ArrayList<Integer>> getUploading() {
        return uploading;
    }

    public void setUploading(Map<Chunk, ArrayList<Integer>> uploading) {
        this.uploading = uploading;
    }

    public int chunksToUpload() {
        int nrChunksWithoutReplication = 0;

        for (Iterator<Map.Entry<Chunk, ArrayList<Integer>>> it = uploading.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Chunk, ArrayList<Integer>> entry = it.next();
            if(entry.getKey().desiredReplication())
                it.remove();
            else
                nrChunksWithoutReplication++;
        }
        return nrChunksWithoutReplication;
    }

    public void updateStorage(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        String version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();

        Chunk lookupChunk = new Chunk(fileId, chunkNo);
        if(storage.containsKey(lookupChunk)){
            storage.get(lookupChunk).add(senderId);

            Map.Entry<Chunk, ArrayList<Integer>> entry = null;
            for (Iterator<Map.Entry<Chunk, ArrayList<Integer>>> it = storage.entrySet().iterator(); it.hasNext();) {
                entry = it.next();
                if(entry.getKey().equals(lookupChunk)){
                    it.remove();
                    break;
                }
            }

            storage.put(entry.getKey(), entry.getValue());
        }
    }

    public void updateUploadingChunks(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        String version = header.getVersion();
        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();

        Chunk lookupChunk = new Chunk(fileId, chunkNo);
        if(uploading.containsKey(lookupChunk)){
            Map.Entry<Chunk, ArrayList<Integer>> entry = null;
            for (Iterator<Map.Entry<Chunk, ArrayList<Integer>>> it = uploading.entrySet().iterator(); it.hasNext();) {
                entry = it.next();
                if(entry.getKey().equals(lookupChunk)){
                    entry.getValue().add(senderId);
                    it.remove();
                    break;
                }
            }
            uploading.put(entry.getKey(), entry.getValue());
        }
    }
}
