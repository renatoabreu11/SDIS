package protocols;

import fileSystem.Chunk;
import fileSystem._File;
import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import protocols.initiator.RestoreInitiator;
import utils.Utils;

import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Restore implements Runnable {

    private Peer parentPeer;
    private Message request;

    public Restore(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        System.out.println("Starting restore.");
    }

    @Override
    public void run() {
        String version = request.getHeader().getVersion();
        String fileId = request.getHeader().getFileId();
        int chunkNo = request.getHeader().getChunkNo();
        String[] sender = null;
        if(version.equals(Utils.ENHANCEMENT_RESTORE) || version.equals(Utils.ENHANCEMENT_ALL))
            sender = request.getHeader().getSender_access().split(":");

        _File file = parentPeer.getManager().getFileStorage(fileId);
        if(file == null)
            return;

        ArrayList<Chunk> chunks = file.getStoredChunks(parentPeer.getId());
        for(int i = 0; i < chunks.size(); i++) {
            if(chunks.get(i).getChunkNo() == chunkNo) {
                MessageHeader header = new MessageHeader(Utils.MessageType.CHUNK, version, parentPeer.getId(), fileId, chunkNo);
                Path path = Paths.get("bin/data/chunks/" + fileId + chunkNo);
                byte[] data = new byte[0];
                try {
                    data = Files.readAllBytes(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MessageBody body = new MessageBody(data);

                if((version.equals(Utils.ENHANCEMENT_RESTORE) || version.equals(Utils.ENHANCEMENT_ALL)) &&
                        (parentPeer.getProtocolVersion().equals(Utils.ENHANCEMENT_RESTORE) || parentPeer.getProtocolVersion().equals(Utils.ENHANCEMENT_ALL))){
                    Message message = new Message(header, body);
                    Message multicastMsg = new Message(header);

                    DatagramSocket socket = null;
                    InetAddress address = null;
                    byte[] bufferPrivate = null;
                    byte[] bufferMulticast = null;
                    try {
                        socket = new DatagramSocket();
                        assert sender != null;
                        address = InetAddress.getByName(sender[0]);
                        bufferPrivate = message.getMessageBytes();
                        bufferMulticast = multicastMsg.getMessageBytes();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    assert bufferPrivate != null;
                    DatagramPacket packet = new DatagramPacket(bufferPrivate, bufferPrivate.length, address, Integer.parseInt(sender[1]));
                    try {
                        socket.send(packet);
                        parentPeer.sendMessageMDR(bufferMulticast);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    socket.close();
                } else {
                    try {
                        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(401));

                        if(parentPeer.getChunkRestoring().contains(fileId + chunkNo)){
                            parentPeer.getChunkRestoring().remove(fileId + chunkNo);
                            return;
                        }

                        Message message = new Message(header, body);
                        byte[] buffer = message.getMessageBytes();
                        parentPeer.sendMessageMDR(buffer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }
}
