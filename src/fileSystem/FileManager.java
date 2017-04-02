package fileSystem;

import messageSystem.Message;
import messageSystem.MessageHeader;
import utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager {

    private ConcurrentHashMap<String, _File> storage = new ConcurrentHashMap<>();
    private String backupLocation = "data/chunks/";

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

        _File f = new _File(null, fileId, 0);
        this.addFileToStorage(f);
        _File file = this.getFileStorage(fileId);
        Chunk c = new Chunk(chunkNo);
        c.updateReplication(senderId);
        boolean exists = file.addChunk(c);
        if(!exists)
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
            Path path = Paths.get(backupLocation + fileId + chunk.getChunkNo());
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
     * @param id
     * @throws IOException
     */
    public synchronized void deleteStoredChunk(String fileId, int chunkNo, int id) throws IOException {
        _File file = storage.get(fileId);

        for(Chunk chunk : file.getChunks()) {
            if(chunk.getChunkNo() == chunkNo) {
                if(chunk.peerHasChunk(id)) {
                    Path path = Paths.get(backupLocation + fileId + chunkNo);
                    Files.delete(path);
                    file.removeChunkPeer(chunkNo, id);
                }
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

    public synchronized long getCurrOccupiedSize() throws IOException {
        Iterator it = storage.entrySet().iterator();
        long numBytes = 0;

        while(it.hasNext()) {
            @SuppressWarnings("unchecked")
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

    /**
     * Writes all the storage info on a file.
     * Needed in case the peer crashes or is terminated. Allows for recovering of data.
     * @throws IOException
     */
    public synchronized void WriteMetadata() throws IOException {
        /*
        * File format:
        * "file_id..pathname..replication_degree..number_chunks
        * chunk_1..current_replication_degree..peer1..peer2..peer_n
        * chunk_2..current_replication_degree..peer1..peer2..peer_n
        * chunk_number..current_replication_degree..peer1..peer2..peer_n
        * file_id..pathname..replication_degree
        * chunk_1..current_replication_degree..peer1..peer2..peer_n
        * chunk_2..current_replication_degree..peer1..peer2..peer_n
        * chunk_number..current_replication_degree..peer1..peer2..peer_n"
        * */
        FileOutputStream fos = new FileOutputStream("data/metadata.txt");
        String str = "";

        Iterator it = storage.entrySet().iterator();
        while(it.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, _File> storedFile = (Map.Entry<String, _File>) it.next();
            String fileId = storedFile.getKey();
            _File file = storedFile.getValue();

            str += fileId + ".." + file.getPathname() + ".." + file.getChunks().get(0).getReplicationDegree() + ".." + file.getChunks().size() + "\n";
            for(int i = 0; i < file.getChunks().size(); i++) {
                Chunk chunk = file.getChunks().get(i);
                str += chunk.getChunkNo() + ".." + chunk.getCurrReplicationDegree() + "..";
                for(int j = 0; j < chunk.getPeers().size(); j++) {
                    str += chunk.getPeers().get(j);
                    if(j < chunk.getPeers().size() - 1)
                        str += "..";
                }
                if(i < file.getChunks().size() - 1)
                str += "\n";
            }

            if(it.hasNext())
                str += "\n";
        }

        fos.write(str.getBytes());
        fos.close();
    }

    /**
     * Loads previously existed metadata.
     * @throws IOException
     */
    public void LoadMetadata() throws IOException {
        if(!new File(Utils.METADATA_PATHNAME).exists())
            return;

        FileReader fr = new FileReader(Utils.METADATA_PATHNAME);
        BufferedReader br = new BufferedReader(fr);
        String line;

        while(true) {
            if((line = br.readLine()) == null)
                break;
            String[] lineSplit = line.split("\\.\\.");

            String fileId = lineSplit[0];
            String pathname = lineSplit[1].equals("null") ? null : lineSplit[1];
            int replicationDegree = Integer.parseInt(lineSplit[2]);
            int numChunks = Integer.parseInt(lineSplit[3]);
            _File file = new _File(pathname, fileId, numChunks);

            for(int i = 0; i < numChunks; i++) {
                line = br.readLine();
                lineSplit = line.split("\\.\\.");

                int chunkNo = Integer.parseInt(lineSplit[0]);
                int currReplicationDegree = Integer.parseInt(lineSplit[1]);
                ArrayList<Integer> peers = new ArrayList<>();

                for(int j = 2; j < lineSplit.length; j++)
                    peers.add(Integer.parseInt(lineSplit[j]));

                Chunk chunk = new Chunk(replicationDegree, fileId, chunkNo, currReplicationDegree, peers);
                file.addChunk(chunk);
            }

            storage.put(fileId, file);
        }

        /*int numChunks = 0;
        Iterator it = storage.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, _File> storedFile = (Map.Entry<String, _File>) it.next();
            _File file = storedFile.getValue();

            for(Chunk chunk : file.getChunks())
                numChunks++;
        }

        System.out.println("Loaded " + storage.size() + " files, and " + numChunks + " chunks.");*/
    }
}