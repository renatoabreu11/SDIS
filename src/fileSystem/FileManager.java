package fileSystem;

import messageSystem.Message;
import messageSystem.MessageHeader;
import utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager {

    private ConcurrentHashMap<String, _File> storage;
    private String backupLocation;
    private HashMap<String, ArrayList<Integer>> hostIdDelete;

    /**
     * File manager Constructor
     */
    public FileManager(){
        storage = new ConcurrentHashMap<>();
        backupLocation = "data/chunks/";
        hostIdDelete = new HashMap<>();
    }

    /**
     * Associates all the peer's id that have chunks of the fileId.
     * @param fileId
     */
    public void FillIdDelete(String fileId) {
        if(hostIdDelete.containsKey(fileId))
            return;

        _File file = storage.get(fileId);
        hostIdDelete.put(fileId, new ArrayList<>(file.getAllPeers()));
    }

    /**
     * Removes peer's id associated with fileId from the map.
     * @param fileId
     * @param id
     */
    public void RemovePeerId(String fileId, int id) {
        ArrayList<Integer> peers = hostIdDelete.get(fileId);

        if(peers.contains(id)) {
            peers.remove(id);
            if(peers.size() == 0)
                hostIdDelete.remove(fileId);
        }
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
    public synchronized boolean addFileToStorage(_File f){
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

        _File f;
        if(!storage.containsKey(fileId))
           storage.put(fileId, new _File(null, fileId, 0));

        f = storage.get(fileId);
        Chunk c = new Chunk(chunkNo, fileId);
        f.addChunkThroughStored(c, senderId);
    }

    /**
     * Delete protocol callable.
     * Deletes all chunk's files from the computer and the map.
     * @param fileId name of the file to delete.
     * @throws IOException
     */
    public synchronized void deleteStoredChunks(String fileId, int peer) throws IOException {
        _File file = storage.get(fileId);
        if(file == null)
            return;

        // Removes all the chunk's files from the computer.
        for(int i = 0; i < file.getChunks().size(); i++) {
            Chunk chunk = file.getChunks().get(i);
            if(chunk.peerHasChunk(peer)){
                Path path = Paths.get(backupLocation + fileId + chunk.getChunkNo());
                Files.delete(path);
            }
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
    public synchronized long deleteStoredChunk(String fileId, int chunkNo, int id) throws IOException {
        _File file = storage.get(fileId);
        long spaceReclaimed = 0;
        for(Chunk chunk : file.getChunks()) {
            if(chunk.getChunkNo() == chunkNo) {
                if(chunk.peerHasChunk(id)) {
                    Path path = Paths.get(backupLocation + fileId + chunkNo);
                    spaceReclaimed = Files.readAllBytes(path).length;
                    Files.delete(path);
                    file.removeChunkPeer(chunkNo, id);
                }
                break;
            }
        }
        return spaceReclaimed;
    }

    public synchronized long getCurrOccupiedSize(int peer_id) throws IOException {
        Iterator it = storage.entrySet().iterator();
        long numBytes = 0;

        while(it.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            _File file = entry.getValue();

            numBytes += file.getFileBytesSize(peer_id);
        }

        return numBytes;
    }

    /**
     * Given the fileId and the chunk number, it returns the corresponding chunk.
     * @param fileId
     * @param chunkNo
     * @return
     */
    public synchronized Chunk getChunk(String fileId, int chunkNo, int peer) {
        _File file = storage.get(fileId);
        if(file == null) {
            return null;
        }

        for(Chunk chunk : file.getChunks()) {
            if(chunk.getChunkNo() == chunkNo && chunk.peerHasChunk(peer))
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
        FileOutputStream fos = new FileOutputStream(Utils.METADATA_PATHNAME);
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
    }

    public synchronized ArrayList<Chunk> getChunksWithHighRD(int id) {
        ArrayList<Chunk> ret = new ArrayList<>();
        Iterator it = storage.entrySet().iterator();

        while(it.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            _File file = entry.getValue();

            ArrayList<Chunk> chunks = file.getStoredChunksWithHigherRD(id);
            if(chunks != null && chunks.size() != 0)
                ret.addAll(chunks);
        }

        ret.sort(Comparator.comparingInt(Chunk::getCurrReplicationDegree).reversed());

        return ret;
    }

    public synchronized long countDisposableSpace(ArrayList<Chunk> storedChunksWithHighRD) {
        long numBytes = 0;
        for(Chunk c : storedChunksWithHighRD){
            Path path = Paths.get("data/chunks/" + c.getFileId() + c.getChunkNo());
            long bytesSize = 0;
            try {
                bytesSize = Files.readAllBytes(path).length;
            } catch (IOException e) {
                e.printStackTrace();
            }
            numBytes += bytesSize;
        }
        return numBytes;
    }

    public synchronized boolean checkStoredChunk(String fileId, Chunk chunk, int id) {
        if(!storage.containsKey(fileId)) {
            return false;
        }

        ArrayList<Chunk> storedChunks = storage.get(fileId).getStoredChunks(id);
        for(Chunk c : storedChunks){
            if(c.getChunkNo() == chunk.getChunkNo())
                return true;
        }
        return false;
    }

    public synchronized void storeChunk(String fileId, Chunk chunk, int id, byte[] chunkData) {
        if(!storage.containsKey(fileId)) {
            _File _file = new _File(null, fileId, 0);
            storage.put(fileId, _file);
        }

        chunk.addPeer(id);
        storage.get(fileId).addChunkThroughPutchunk(chunk);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("data/chunks/" + fileId + chunk.getChunkNo());
            fileOutputStream.write(chunkData);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void WriteRemovePeerId() throws IOException {
        String str = "";
        FileOutputStream fos = new FileOutputStream(Utils.PEERS_TO_DELETE_PATHNAME);
        Iterator it = hostIdDelete.entrySet().iterator();

        while(it.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, ArrayList<Integer>> entry = (Map.Entry<String, ArrayList<Integer>>) it.next();
            String fileId = entry.getKey();
            ArrayList<Integer> peers = entry.getValue();

            str += fileId + "..";
            for(int i = 0; i < peers.size(); i++) {
                str += peers.get(i);
                if(i != peers.size()-1)
                    str += "..";
            }

            if(it.hasNext())
                str += "\n";
        }

        fos.write(str.getBytes());
        fos.close();
    }

    public void LoadRemovePeerId() throws IOException {
        if(!new File(Utils.PEERS_TO_DELETE_PATHNAME).exists())
            return;

        FileReader fr = new FileReader(Utils.PEERS_TO_DELETE_PATHNAME);
        BufferedReader br = new BufferedReader(fr);
        String line;

        while(true) {
            if((line = br.readLine()) == null)
                return;

            String[] lineSplit = line.split("\\.\\.");
            String fileId = lineSplit[0];

            ArrayList<Integer> peers = new ArrayList<>();
            for(int i = 1; i < lineSplit.length; i++)
                peers.add(Integer.parseInt(lineSplit[i]));

            hostIdDelete.put(fileId, peers);
        }
    }

    public HashMap<String, ArrayList<Integer>> getHostIdDelete() {
        return hostIdDelete;
    }
}