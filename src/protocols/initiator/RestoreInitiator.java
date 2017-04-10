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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;

public class RestoreInitiator extends ProtocolInitiator {

    private String pathname;
    private ArrayList<Chunk> restoring = new ArrayList<>();
    private byte[] fileData;
    private protocolState currState;
    private DatagramSocket socket;
    private int privatePort;
    private String ipv4;

    private enum protocolState {
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
        if(getVersion().equals(Utils.ENHANCEMENT_RESTORE) || getVersion().equals(Utils.ENHANCEMENT_ALL)){
            privatePort = 4572;
            ipv4 = Utils.getIPV4address();
            try {
                socket = new DatagramSocket(privatePort);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startProtocol() throws IOException, InterruptedException {
        _File file = getParentPeer().getFileFromManager(pathname);
        if (file == null) {
            this.currState = protocolState.INVALIDFILE;
            if(getVersion().equals(Utils.ENHANCEMENT_RESTORE) || getVersion().equals(Utils.ENHANCEMENT_ALL))
                socket.close();
            return;
        }

        String fileId = file.getFileId();
        int numChunks = file.getNumChunks();

        this.currState = protocolState.RESTOREMESSAGE;
        for (int i = 0; i < numChunks; i++) {
            MessageHeader header = null;
            if(getVersion().equals(Utils.ENHANCEMENT_RESTORE) || getVersion().equals(Utils.ENHANCEMENT_ALL))
                header = new MessageHeader(Utils.MessageType.GETCHUNK, getVersion(), getParentPeer().getId(), fileId, i, ipv4 + ":" + privatePort);
            else header = new MessageHeader(Utils.MessageType.GETCHUNK, getVersion(), getParentPeer().getId(), fileId, i);
            Message message = new Message(header);
            byte[] buf = message.getMessageBytes();

            getParentPeer().sendMessageMC(buf);
        }
        waitForChunks();
    }

    public void waitForChunks() throws IOException {
        this.currState = protocolState.RECOVERCHUNKS;

        _File f = getParentPeer().getFileFromManager(pathname);
        int chunksNo = f.getNumChunks();
        boolean foundAllChunks = false;
        long end = System.currentTimeMillis() + Utils.RecoverMaxTime;

        RestoreEnhancementReceptor restoreEnhancement = null;
        if(getVersion().equals(Utils.ENHANCEMENT_RESTORE) || getVersion().equals(Utils.ENHANCEMENT_ALL)) {
            restoreEnhancement = new RestoreEnhancementReceptor(socket, this);
            Thread t = new Thread(restoreEnhancement);
            t.start();
        }

        while (System.currentTimeMillis() < end && !foundAllChunks) {
            if (chunksNo == restoring.size())
                foundAllChunks = true;
        }

        if(getVersion().equals(Utils.ENHANCEMENT_RESTORE) || getVersion().equals(Utils.ENHANCEMENT_ALL))
            restoreEnhancement.setCanRun(false);

        if (!foundAllChunks)
            currState = protocolState.BROKENFILE;
        else currState = protocolState.CONCATFILE;

        joinFile();
    }

    public void joinFile() {
        restoring.sort(Comparator.comparingInt(Chunk::getChunkNo));

        String str = "";
        for(Chunk chunk : restoring) {
            str += chunk.getChunkNo() + ", " + chunk.getChunkData().length + "\n";
        }

        System.out.println(str);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < restoring.size(); i++) {
            try {
                outputStream.write(restoring.get(i).getChunkData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileData = outputStream.toByteArray();
        currState = protocolState.SENDFILE;
    }

    public byte[] getFile() {
        if(getVersion().equals(Utils.ENHANCEMENT_RESTORE) || getVersion().equals(Utils.ENHANCEMENT_ALL))
            socket.close();

        if (currState != protocolState.SENDFILE)
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

        if (!restoring.contains(chunk))
            restoring.add(chunk);
    }

    public class RestoreEnhancementReceptor implements Runnable {

        private DatagramSocket socket;
        private RestoreInitiator parent;
        private boolean canRun;

        public RestoreEnhancementReceptor(DatagramSocket socket, RestoreInitiator parent) {
            this.socket = socket;
            this.parent = parent;
            canRun = true;
        }

        @Override
        public void run() {
            while(canRun) {
                byte[] buffer = new byte[Utils.HEADER_SIZE + Utils.BODY_SIZE];
                DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(dgp);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                if(!canRun)
                    return;
                String msgWrapper = new String(dgp.getData(), 0, dgp.getLength());
                logMessage("Restore Chunk Message Version 1.3\n");
                logMessage(msgWrapper);
                Message message = new Message(msgWrapper);
                parent.addChunkToRestoring(message);
            }
        }

        public void setCanRun(boolean flag) {
            canRun = flag;
        }
    }
}
