package protocols.initiator;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ManageDiskInitiator extends ProtocolInitiator{

    private long clientMaxDiskSpace;
    private String successMessage;

    public ManageDiskInitiator(String version, boolean logSystem, Peer parentPeer, long clientMaxDiskSpace) {
        super(version, logSystem, parentPeer);
        this.clientMaxDiskSpace = clientMaxDiskSpace;
        successMessage = null;
    }

    @Override
    public void startProtocol() throws IOException {
        long freeCurrSpace = 0;

        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("linux"))
            freeCurrSpace = new File("/").getFreeSpace() / 1000;
        else if(os.contains("windows"))
            freeCurrSpace = new File("c:").getUsableSpace() / 1000;
        else
            System.out.println("Couldn't determine your Operating System. Ignoring max available space in your computer.");

        if(freeCurrSpace != 0 && clientMaxDiskSpace > freeCurrSpace) {
            successMessage = "The machine hosting the peer doesn't have that much free space.";
            return;
        }

        if(clientMaxDiskSpace < getParentPeer().getDiskUsage()  / 1000)
            ManageChunks();

        getParentPeer().setMaxDiskSpace(clientMaxDiskSpace);
    }

    @Override
    public String endProtocol() {
        return null;
    }

    /**
     * Removes the chunks which have a higher replication degree until the available space is
     * lower or equal than the amount the user specified.
     * Sends a message to the MC every time a chunk is deleted, ir order to try to maintain the
     * replication degree.s
     * @throws IOException
     */
    private void ManageChunks() throws IOException {
        ArrayList<Chunk> orderedChunks = GetFilesHigherRD();
        int i = 0;
        Chunk currChunkToDelete = orderedChunks.get(i);

        do {
            _File file = getParentPeer().getManager().getFileStorage(orderedChunks.get(i).getFileId());
            String fileId = file.getFileId();

            MessageHeader header = new MessageHeader(Utils.MessageType.REMOVED, getParentPeer().getProtocolVersion(), getParentPeer().getId(), fileId, currChunkToDelete.getChunkNo());
            Message message = new Message(header);
            
            getParentPeer().getManager().deleteStoredChunk(fileId, currChunkToDelete.getChunkNo(), getParentPeer().getId());
            byte[] buffer = message.getMessageBytes();
            getParentPeer().getMc().sendMessage(buffer);

            getParentPeer().getManager().WriteMetadata();

            i++;
            if(i == orderedChunks.size()) {
                successMessage = "Removed everything on the peer, returning...";
                return;
            }
            currChunkToDelete = orderedChunks.get(i);
        } while(clientMaxDiskSpace < getParentPeer().getDiskUsage() / 1000);

        successMessage = "The maximum disk space available was updated.";
    }

    /**
     * Returns all the chunks stored in the peer sorted by their duplication degree.
     * @return
     */
    private ArrayList<Chunk> GetFilesHigherRD() {
        Map<String, _File> storedFiles = getParentPeer().getManager().getStorage();
        Iterator it = storedFiles.entrySet().iterator();

        ArrayList<Chunk> chunkList = new ArrayList<>();
        while(it.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            _File file = entry.getValue();

            ArrayList<Chunk> storedChunks = file.getStoredChunks(getParentPeer().getId());

            if(storedChunks != null && storedChunks.size() != 0)
                chunkList.addAll(storedChunks);
        }

        chunkList.sort(Comparator.comparingInt(Chunk::getCurrReplicationDegree).reversed());
        return chunkList;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}
