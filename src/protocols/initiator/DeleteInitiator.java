package protocols.initiator;

import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
import java.security.MessageDigest;

public class DeleteInitiator extends ProtocolInitiator{
    private String pathname;
    private enum protocolState{

    }

    public DeleteInitiator(String version, boolean logSystem, Peer parentPeer, String pathname) {
        super(version, logSystem, parentPeer);
        this.pathname = pathname;
    }

    @Override
    public void startProtocol() {
        //isto não faz sentido. O file id de um ficheiro é constituido por mais que o pathname e o last modified
        /*/ CAN A REMOTE PEER ACCESS THE LAST MODIFICATION TIME, OR DO WE NEED TO GET THIS FROM THE fileData??????????????
        String lastModified = Long.toString(new java.io.File(pathname).lastModified());

        // Hashing the file id.
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String fileId = pathname + lastModified;
        md.update(fileId.getBytes("UTF-8"));
        byte[] fileIdHashed = md.digest();*/

        String fileId = "";
        MessageHeader header = new MessageHeader(Utils.MessageType.DELETE, getVersion(), getParentPeer().getId(), fileId);
        Message message = new Message(header);
        byte[] buffer = new byte[0];
        try {
            buffer = message.getMessageBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // We send 'Utils.DeleteRetransmissions' messages to make sure every chunk is properly deleted.
        for(int i = 0; i < Utils.DeleteRetransmissions; i++)
            getParentPeer().sendMessageMC(buffer);
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }
}
