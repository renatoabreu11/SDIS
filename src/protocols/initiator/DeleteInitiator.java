package protocols.initiator;

import fileSystem._File;
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
    public void startProtocol() throws IOException {
        _File file = getParentPeer().getManager().getFile(pathname);
        String fileId = file.getFileId();

        System.out.println("Pathname: " + pathname + ", file id: " + file.getFileId());

        MessageHeader header = new MessageHeader(Utils.MessageType.DELETE, getVersion(), getParentPeer().getId(), fileId);
        Message message = new Message(header);
        byte[] buffer = message.getMessageBytes();

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
