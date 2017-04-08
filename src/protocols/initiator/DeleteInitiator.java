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

    private boolean ENH_fromDeleteProtocol;
    private String ENH_fileId;

    /*
    * Type 1: Normal Delete Protocol.
    * Type 2: Delete Enhancement Protocol.
    * */

    public DeleteInitiator(String version, boolean logSystem, Peer parentPeer, String pathname, int type) {
        super(version, logSystem, parentPeer);
        if(type == 1) {
            this.pathname = pathname;
            this.ENH_fromDeleteProtocol = false;
        } else {
            this.ENH_fileId = pathname;
            this.ENH_fromDeleteProtocol = true;
        }
    }

    @Override
    public void startProtocol() throws IOException, InterruptedException {
        if(ENH_fromDeleteProtocol)
            SendMessage(ENH_fileId);
        else {
            _File file;
            String fileId;

            if(pathname.contains("/")) {
                file = getParentPeer().getManager().getFile(pathname);
                fileId = file.getFileId();
            } else {
                fileId = pathname;
                file = getParentPeer().getManager().getFileStorage(fileId);
            }

            if(file == null) {
                System.out.println("File '" + pathname + "' doesn't exist. Operation aborted.");
                return;
            }
            logMessage("Pathname: " + pathname + ", file id: " + fileId);
            SendMessage(fileId);
        }
    }

    private void SendMessage(String fileId) throws IOException, InterruptedException {
        MessageHeader header = new MessageHeader(Utils.MessageType.DELETE, getVersion(), getParentPeer().getId(), fileId);
        Message message = new Message(header);
        byte[] buffer = message.getMessageBytes();

        if(getParentPeer().getProtocolVersion().equals(Utils.ENHANCEMENT_DELETE) || getParentPeer().getProtocolVersion().equals(Utils.ENHANCEMENT_ALL))
            getParentPeer().getManager().FillIdDelete(fileId);

        // We send 'Utils.DeleteRetransmissions' messages to make sure every chunk is properly deleted.
        for(int i = 0; i < Utils.DeleteRetransmissions; i++)
            getParentPeer().sendMessageMC(buffer);

        if(getParentPeer().getProtocolVersion().equals(Utils.ENHANCEMENT_DELETE) || getParentPeer().getProtocolVersion().equals(Utils.ENHANCEMENT_ALL))
            // Waits for the reply messages in order to receive the id of the peers that confirm the file deletion.
            TimeUnit.MILLISECONDS.sleep(Utils.DELETED_MAX_TIME);
    }
}
