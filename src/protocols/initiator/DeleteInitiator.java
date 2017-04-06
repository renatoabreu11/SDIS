package protocols.initiator;

import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DeleteInitiator extends ProtocolInitiator{

    private String pathname;

    public DeleteInitiator(String version, boolean logSystem, Peer parentPeer, String pathname) {
        super(version, logSystem, parentPeer);
        this.pathname = pathname;
    }

    @Override
    public void startProtocol() throws IOException, InterruptedException {
        _File file;
        String fileId;

        if(pathname.contains(File.separator)) {
            file = getParentPeer().getManager().getFile(pathname);
            fileId = file.getFileId();
        } else {
            fileId = pathname;
            file = getParentPeer().getManager().getFileStorage(fileId);
        }

        logMessage("Pathname: " + pathname + ", file id: " + file.getFileId());

        MessageHeader header = new MessageHeader(Utils.MessageType.DELETE, getVersion(), getParentPeer().getId(), fileId);
        Message message = new Message(header);
        byte[] buffer = message.getMessageBytes();

        // We send 'Utils.DeleteRetransmissions' messages to make sure every chunk is properly deleted.
        for(int i = 0; i < Utils.DeleteRetransmissions; i++)
            getParentPeer().sendMessageMC(buffer);

        if(getParentPeer().getProtocolVersion().equals(Utils.ENHANCEMENT_DELETE) || getParentPeer().getProtocolVersion().equals(Utils.ENHANCEMENT_ALL)) {
            // Waits for the reply messages in order to receive the id of the peers that confirm the file deletion.
            TimeUnit.MILLISECONDS.sleep(Utils.DELETED_MAX_TIME);

            ArrayList<Integer> sendersIdReplied = getParentPeer().getSendersIdRepliesToDelete().get(fileId);
            ArrayList<Integer> allPeers = file.getAllPeers();

            // Subtracts from all the peers the ones that replied, leaving the list with only those who haven't.
            allPeers.removeAll(sendersIdReplied);

            // Sets the list in the peer with only the id's of the peers that did not reply.
            getParentPeer().setPeersDeletedReply(allPeers, fileId);
        }
    }
}
