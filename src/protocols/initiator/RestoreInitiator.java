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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    }

    public byte[] sendFile() {
        try {
            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        _File f = getParentPeer().getFileFromManager(pathname);

        System.out.println(Arrays.asList(restoring));

        //Collections.sort(restoring, (fruit2, fruit1) -> fruit1.getChunkNo().compareTo(fruit2.getChunkNo()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for(int i = 0; i < restoring.size() - 1; i++){
            Path path = Paths.get("data/"+f.getFileId() + restoring.get(i).getChunkNo());
            try {
                outputStream.write(Files.readAllBytes(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] fileData = outputStream.toByteArray();
        return fileData;
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
