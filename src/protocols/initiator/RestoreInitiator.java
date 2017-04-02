package protocols.initiator;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class RestoreInitiator extends ProtocolInitiator{

    private String pathname;
    private ArrayList<Chunk> restoring = new ArrayList<>();
    private byte[] fileData;
    private protocolState currState;
    private enum protocolState{
        INIT,
        RESTOREMESSAGE,
        INVALIDFILE,
        RECOVERCHUNKS,
        BROKENFILE,
        CONCATFILE,
        SENDFILE
    }

    public RestoreInitiator(String protocolVersion, boolean b, Peer peer, String pathname) {
        super(protocolVersion, b, peer);
        this.pathname = pathname;
        this.currState = protocolState.INIT;
    }

    @Override
    public void startProtocol() throws IOException, InterruptedException {
        _File file = getParentPeer().getFileFromManager(pathname);
        if(file == null){
            this.currState = protocolState.INVALIDFILE;
            return;
        }

        String fileId = file.getFileId();
        int numChunks = file.getNumChunks();

        this.currState = protocolState.RESTOREMESSAGE;
        for(int i = 0; i < numChunks; i++) {
            MessageHeader header = new MessageHeader(Utils.MessageType.GETCHUNK, getVersion(), getParentPeer().getId(), fileId, i);
            Message message = new Message(header);
            byte[] buf = message.getMessageBytes();

            getParentPeer().sendMessageMC(buf);
        }
        waitForChunks();
    }

    public void waitForChunks(){
        this.currState = protocolState.RECOVERCHUNKS;

        _File f = getParentPeer().getFileFromManager(pathname);
        int chunksNo = f.getNumChunks();
        boolean foundAllChunks = false;
        long t = System.currentTimeMillis();
        long end = t+Utils.RecoverMaxTime;

        while(System.currentTimeMillis() < end && !foundAllChunks){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(chunksNo == restoring.size())
                foundAllChunks = true;
        }

        if(!foundAllChunks)
            currState = protocolState.BROKENFILE;
        else currState = protocolState.CONCATFILE;

        joinFile();
    }

    public void joinFile(){
        _File f = getParentPeer().getFileFromManager(pathname);
        Collections.sort(restoring);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for(int i = 0; i < restoring.size(); i++){
            Path path = Paths.get("data/" + f.getFileId() + restoring.get(i).getChunkNo());
            try {
                outputStream.write(Files.readAllBytes(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileData = outputStream.toByteArray();
        currState = protocolState.SENDFILE;
    }

    public byte[] getFile() {
        if(currState != protocolState.SENDFILE)
            return null;
        return fileData;
    }

    /**
     * Restore protocol callable.
     * @param message
     */
    public synchronized void addChunkToRestoring(Message message) {
        MessageHeader header = message.getHeader();
        MessageBody body = message.getBody();

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

    public protocolState getCurrState() {
        return currState;
    }

    public void setCurrState(protocolState currState) {
        this.currState = currState;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }


}
