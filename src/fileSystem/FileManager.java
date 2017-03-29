package fileSystem;

import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileManager {

    private Map<String, _File> storage; //String continua a ser o fileIdhashed. Storage tem a informação de todos os ficheiros, quer eles estejam no pc ou não. Se estiverem a lista de chunks não é vazia
    //Fiz isto para reduzir o numero de estruturas que guardam a informação. A procura torna-se mais rápido uma vez que é a partir de um hashmap
    private ArrayList<Chunk> uploading;
    private ArrayList<Chunk> restoring;
    private String backupLocation = "/tmp";

    /**
     *
     */
    public FileManager(){
        storage = new HashMap<>();
        uploading = new ArrayList<>();
        restoring = new ArrayList<>();
    }

    /**
     *
     * @return
     */
    public Map<String, _File> getStorage() {
        return storage;
    }

    /**
     *
     * @param storage
     */
    public void setStorage(Map<String, _File> storage) {
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
    public _File getFileStorage(String fileId){
        return storage.get(fileId);
    }

    /**
     *
     * @param pathname
     * @return
     */
    public _File getFile(String pathname){
        Map<String, _File> map =  storage;
        for (_File f : map.values()) {
           if(f.getPathname().equals(pathname))
               return f;
        }
        return null;
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
     */
    public boolean addFileToStorage(_File f){
        if(storage.put(f.getFileId(), f) == null)
            return false;
        else return true;
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

        _File file = this.getFileStorage(fileId);
        if(file != null){
            file.updateChunk(chunkNo, senderId);
        }
    }

    /**
     *
     * @param msgWrapper
     */
    public void updateUploadingChunks(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        int senderId = header.getSenderId();
        int chunkNo = header.getChunkNo();

        Chunk c = new Chunk(chunkNo);
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
        _File file = storage.get(fileId);
        if(file == null)
            return;

        // Removes all the chunk's files from the computer.
        for(int i = 0; i < file.getChunks().size(); i++) {
            Chunk chunk = file.getChunks().get(i);
            //Os chunks não precisam de extensão
            Path path = Paths.get(fileId + chunk.getChunkNo() + ".txt");
            Files.delete(path);
        }

        // Removes the entry on the map.
        storage.remove(fileId);
    }

    /**
     * If a peer is receiving a chunk from a file it doesn't have on the 'storage', it creates the file
     * first and then it adds the chunk to the file array.
     * @param c
     */
    public void addChunkToStorage(String fileId, Chunk c) {
        if(!storage.containsKey(fileId)) {
            _File _file = new _File(null, fileId, 0);
            storage.put(fileId, _file);
        }
        storage.get(fileId).addChunk(c);
    }

    /**
     * Restore protocol callable.
     * @param message
     */
    public void addChunkToRestoring(Message message) {
        MessageHeader header = message.getHeader();
        MessageBody body = message.getBody();

        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();
        byte[] data = body.getBody();

        Chunk chunk = new Chunk(chunkNo, data);

        if(!restoring.contains(chunk))
            restoring.add(chunk);
    }

    public String getBackupLocation() {
        return backupLocation;
    }

    public void setBackupLocation(String backupLocation) {
        this.backupLocation = backupLocation;
    }

    public long getCurrOccupiedSize() {
        Iterator it = storage.entrySet().iterator();
        long numBytes = 0;

        while(it.hasNext()) {
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            _File file = entry.getValue();

            numBytes += file.getFileBytesSize();
        }

        return numBytes;
    }
}
