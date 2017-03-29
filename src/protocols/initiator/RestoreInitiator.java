package protocols.initiator;

import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RestoreInitiator extends ProtocolInitiator{
    private String pathname;
    private enum protocolState{

    }
    public RestoreInitiator(String protocolVersion, boolean b, Peer peer, String pathname) {
        super(protocolVersion, b, peer);
        this.pathname = pathname;
    }

    @Override
    public void startProtocol() {
        _File file = getParentPeer().getFileFromManager(pathname);
        if(file == null)
            return;

        String fileId = file.getFileId();
        int numChunks = file.getNumChunks();

        for(int i = 0; i < numChunks; i++) {
            MessageHeader header = new MessageHeader(Utils.MessageType.GETCHUNK, getVersion(), getParentPeer().getId(), fileId, (i+1));
            Message message = new Message(header);
            byte[] buf = new byte[0];
            try {
                buf = message.getMessageBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            getParentPeer().sendMessageMC(buf);
            try {
                TimeUnit.MILLISECONDS.sleep(1000); //it is supposed to wait?
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sendFile();
    }

    private void sendFile() {
        // Send file to client or restore the file in the curr peer????????????????????????????
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }
}
