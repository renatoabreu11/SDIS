package fileSystem;

import messageSystem.Message;
import messageSystem.MessageHeader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileManager {
    private Map<String, ArrayList<Chunk>> storage = new HashMap<>();
    private ArrayList<Chunk> uploading = new ArrayList<>();

    public FileManager(){}

    public Map<String, ArrayList<Chunk>> getStorage() {
        return storage;
    }

    public void setStorage(Map<String, ArrayList<Chunk>> storage) {
        this.storage = storage;
    }

    public void resetStorage(){
        this.storage.clear();
    }

    public void resetUploadingChunks(){
        this.uploading.clear();
    }

    public ArrayList<Chunk> getFileStorage(String fileId){
        if(storage.containsKey(fileId)){
            return storage.get(fileId);
        }
        return null;
    }

    public void addUploadingChunks(ArrayList<Chunk> chunks) {
        uploading = chunks;
    }

    public ArrayList<Chunk> getUploading() {
        return uploading;
    }

    public void setUploading(ArrayList<Chunk> uploading) {
        this.uploading = uploading;
    }

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

    public void updateStorage(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        String version = header.getVersion();
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
}
