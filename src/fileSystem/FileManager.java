package fileSystem;

import messageSystem.Message;
import messageSystem.MessageHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager {

    private ConcurrentHashMap<String, _File> storage = new ConcurrentHashMap<>();
    private String backupLocation = "/tmp";

    /**
     * File manager Constructor
     */
    public FileManager(){}

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
    public void setStorage(ConcurrentHashMap<String, _File> storage) {
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
     */
    public boolean addFileToStorage(_File f){
        if(storage.put(f.getFileId(), f) == null)
            return false;
        else return true;
    }

    /**
     *
     * @param msgWrapper
     */
    public synchronized void updateStorage(Message msgWrapper) {
        MessageHeader header = msgWrapper.getHeader();

        int senderId = header.getSenderId();
        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();

        _File file = this.getFileStorage(fileId);
        if(file != null)
            file.updateChunk(chunkNo, senderId);
    }

    /**
     * Delete protocol callable.
     * Deletes all chunk's files from the computer and the map.
     * @param fileId name of the file to delete.
     * @throws IOException
     */
    public synchronized void deleteStoredChunks(String fileId) throws IOException {
        _File file = storage.get(fileId);
        if(file == null)
            return;

        // Removes all the chunk's files from the computer.
        for(int i = 0; i < file.getChunks().size(); i++) {
            Chunk chunk = file.getChunks().get(i);
            Path path = Paths.get(fileId + chunk.getChunkNo());
            Files.delete(path);
        }

        // Removes the entry on the map.
        storage.remove(fileId);
    }

    /**
     * Given the fileId and the chunk number, removes the chunk associated with it, from both the
     * system and the map.
     * @param fileId
     * @param chunkNo
     * @throws IOException
     */
    public synchronized void deleteStoredChunk(String fileId, int chunkNo) throws IOException {
        _File file = storage.get(fileId);

        for(Chunk chunk : file.getChunks()) {
            if(chunk.getChunkNo() == chunkNo) {
                Path path = Paths.get(fileId + chunkNo);
                Files.delete(path);
                file.removeChunk(chunk);
                break;
            }
        }
    }

    /**
     * If a peer is receiving a chunk from a file it doesn't have on the 'storage', it creates the file
     * first and then it adds the chunk to the file array.
     * Returns a boolean indicating if the chunk was added to the storage or not (in case it's a duplicate).
     * @param c
     */
    public synchronized boolean addChunkToStorage(String fileId, Chunk c) {
        if(!storage.containsKey(fileId)) {
            _File _file = new _File(null, fileId, 0);
            storage.put(fileId, _file);
        }
        return storage.get(fileId).addChunk(c);
    }

    public String getBackupLocation() {
        return backupLocation;
    }

    public void setBackupLocation(String backupLocation) {
        this.backupLocation = backupLocation;
    }

    public synchronized long getCurrOccupiedSize() {
        Iterator it = storage.entrySet().iterator();
        long numBytes = 0;

        while(it.hasNext()) {
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            _File file = entry.getValue();

            numBytes += file.getFileBytesSize();
        }

        return numBytes;
    }

    /**
     * Given the fileId and the chunk number, it returns the corresponding chunk.
     * @param fileId
     * @param chunkNo
     * @return
     */
    public synchronized Chunk getChunk(String fileId, int chunkNo) {
        _File file = storage.get(fileId);
        if(file == null) {
            return null;
        }

        for(Chunk chunk : file.getChunks()) {
            if(chunk.getChunkNo() == chunkNo)
                return chunk;
        }

        return null;
    }
}
