package protocols;

import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;

public class Delete implements Runnable {

    private Peer parentPeer;
    private Message message;

    public Delete(Peer parentPeer, Message message) {
        this.parentPeer = parentPeer;
        this.message = message;
    }

    @Override
    public void run() {
        MessageHeader header = message.getHeader();
        String version = header.getVersion();
        String fileId = header.getFileId();

        try {
            parentPeer.deleteFileStorage(fileId);
            parentPeer.getManager().WriteMetadata();

            if((version.equals(Utils.ENHANCEMENT_DELETE) || version.equals(Utils.ENHANCEMENT_ALL)) &&
                    (parentPeer.getProtocolVersion().equals(Utils.ENHANCEMENT_DELETE) || parentPeer.getProtocolVersion().equals(Utils.ENHANCEMENT_ALL))) {
                MessageHeader ENH_header = new MessageHeader(Utils.MessageType.ENH_DELETED, parentPeer.getId(), fileId);
                Message ENH_message = new Message(ENH_header);
                byte[] buffer = ENH_message.getMessageBytes();
                parentPeer.sendMessageMC(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}