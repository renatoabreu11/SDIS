package protocols.initiator;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RestoreInitiator extends ProtocolInitiator{

    private String pathname;
    private ArrayList<Chunk> restoring = new ArrayList<>();
    private enum protocolState{

    }
    public RestoreInitiator(String protocolVersion, boolean b, Peer peer, String pathname) {
        super(protocolVersion, b, peer);
        this.pathname = pathname;
    }

    @Override
    public void startProtocol() throws IOException, InterruptedException {
        _File file = getParentPeer().getFileFromManager(pathname);
        if(file == null)
            return;

        String fileId = file.getFileId();
        int numChunks = file.getNumChunks();

        for(int i = 0; i < numChunks; i++) {
            MessageHeader header = new MessageHeader(Utils.MessageType.GETCHUNK, getVersion(), getParentPeer().getId(), fileId, (i+1));
            Message message = new Message(header);
            byte[] buf = message.getMessageBytes();

            getParentPeer().sendMessageMC(buf);
            TimeUnit.MILLISECONDS.sleep(1000); //it is supposed to wait?
        }

        sendFile();
    }

    private void sendFile() {
        // Send file to client or restore the file in the curr peer????????????????????????????
    }

    /**
     * Restore protocol callable.
     * @param message
     */
    public void addChunkToRestoring(Message message) {
        MessageHeader header = message.getHeader();
        MessageBody body = message.getBody();

        String fileId = header.getFileId();
        int chunkNo = header.getChunkNo();
        byte[] data = body.getBody();

        Chunk chunk = new Chunk(chunkNo, data);

        if(!restoring.contains(chunk))
            restoring.add(chunk);
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }


    public ArrayList<Chunk> getRestoring() {
        return restoring;
    }

    public void setRestoring(ArrayList<Chunk> restoring) {
        this.restoring = restoring;
    }

}
