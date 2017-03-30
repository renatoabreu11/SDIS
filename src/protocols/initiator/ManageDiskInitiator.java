package protocols.initiator;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

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
        long freeCurrSpace;

        switch(System.getProperty("os.name")) {
            case "Linux":
                freeCurrSpace = new File("/").getFreeSpace() / 1000;
                break;
            case "Windows":
                freeCurrSpace = new File("C:").getFreeSpace() / 1000;
                break;
            default: freeCurrSpace = 0; break;
        }

        if(clientMaxDiskSpace > freeCurrSpace)
            successMessage = "The machine hosting the peer doesn't have that much free space.";

        if(clientMaxDiskSpace < getParentPeer().getManager().getCurrOccupiedSize())
            ManageChunks();

        getParentPeer().setMaxDiskSpace(clientMaxDiskSpace);
    }

    @Override
    public void endProtocol() {

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
        boolean found = false;

        do {
            Iterator it = getParentPeer().getManager().getStorage().entrySet().iterator();

            // Searches for the file which contains the chunk to be removed,
            // because we need to assign to the message a fileId.
            while(it.hasNext()) {
                Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
                _File file = entry.getValue();

                if(file.getChunks().contains(currChunkToDelete)) {
                    found = true;
                    String fileId = entry.getKey();

                    MessageHeader header = new MessageHeader(Utils.MessageType.REMOVED, getParentPeer().getProtocolVersion(), getParentPeer().getId(), fileId, currChunkToDelete.getChunkNo());
                    Message message = new Message(header);
                    byte[] buffer = message.getMessageBytes();
                    getParentPeer().getMc().sendMessage(buffer);
                    getParentPeer().getManager().deleteStoredChunk(fileId, currChunkToDelete.getChunkNo());
                    break;
                }
            }

            // Safety measure.
            if(!found) {
                System.out.println("ERROR: couldn't find the file to remove. Aborting...");
                return;
            }

            i++;
            currChunkToDelete = orderedChunks.get(i);
            found = false;
        } while(clientMaxDiskSpace < getParentPeer().getManager().getCurrOccupiedSize());

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
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            _File file = entry.getValue();

            for(int i = 0; i < file.getNumChunks(); i++)
                chunkList.add(file.getChunks().get(i));
        }

        Collections.sort(chunkList);
        return chunkList;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}