package fileSystem;

import java.util.ArrayList;
import java.util.HashMap;
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

    public int updateUploadedChunks(String fileId) {

        int nrChunksWithoutReplication = 0;
        for (Map.Entry<Chunk, ArrayList<Integer>> ee : uploading.entrySet()) {
            Chunk key = ee.getKey();
            ArrayList<Integer> values = ee.getValue();
            if (key.getFileId() == fileId){
                if(key.desiredReplication()){
                    uploading.remove(key);
                    storage.put(key, values);
                }else
                    nrChunksWithoutReplication++;
            }
        }
        return nrChunksWithoutReplication;
    }
}
