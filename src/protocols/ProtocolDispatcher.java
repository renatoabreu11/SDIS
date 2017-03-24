package protocols;

import messageSystem.Message;
import messageSystem.MessageBody;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

import java.io.IOException;
import java.util.Arrays;

public class ProtocolDispatcher {
    private String message;
    private Message msgWrapper;

    public ProtocolDispatcher(String message){
        // Remove if not testing.
        System.out.println(message);

        this.message = message;
        String[] msgSplit = this.message.split("\\R\\R", 2);

        String msgHeader, msgBody = null;
        if(msgSplit.length == 0 || msgSplit.length > 2)
            return; //message discarded
        else if (msgSplit.length == 2)
            msgBody = msgSplit[1];

        msgHeader = msgSplit[0];

        String[] headerSplit = msgHeader.split("\\s+");

        Utils.MessageType type;
        int numberOfArgs;

        switch(headerSplit[0]){
            case "PUTCHUNK":
                type = Utils.MessageType.PUTCHUNK; numberOfArgs = 6; break;
            case "STORED":
                type = Utils.MessageType.STORED; numberOfArgs = 5; break;
            case "GETCHUNK":
                type = Utils.MessageType.GETCHUNK; numberOfArgs = 5; break;
            case "CHUNK":
                type = Utils.MessageType.CHUNK; numberOfArgs = 5; break;
            case "DELETE":
                type = Utils.MessageType.DELETE; numberOfArgs = 5; break;
            case "REMOVED":
                type = Utils.MessageType.REMOVED; numberOfArgs = 4; break;
            default:
                return;
        }
        
        if(headerSplit.length != numberOfArgs)
            return;

        MessageHeader header;
        MessageBody body;
        String version, fileId;
        int senderId, chunkNo, replicationDegree;

        version = headerSplit[1];
        senderId = Integer.parseInt(headerSplit[2]);
        fileId = headerSplit[3];

        if(type == Utils.MessageType.PUTCHUNK){
            chunkNo = Integer.parseInt(headerSplit[4]);
            replicationDegree = Integer.parseInt(headerSplit[5]);
            header = new MessageHeader(type, version, senderId, fileId, chunkNo, replicationDegree);
        }else if(type == Utils.MessageType.GETCHUNK || type == Utils.MessageType.CHUNK || type == Utils.MessageType.REMOVED){
            chunkNo = Integer.parseInt(headerSplit[4]);
            header = new MessageHeader(type, version, senderId, fileId, chunkNo);
        }else
            header = new MessageHeader(type, version, senderId, fileId);

        System.out.println(header.getMessageHeaderAsString());
        if(msgBody != null){
            body = new MessageBody(msgBody.getBytes());
            msgWrapper = new Message(header, body);
        }else
            msgWrapper = new Message(header);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message getMsgWrapper() {
        return msgWrapper;
    }

    public void setMsgWrapper(Message msgWrapper) {
        this.msgWrapper = msgWrapper;
    }

    public void dispatchRequest(Peer parentPeer) throws IOException {
        System.out.println(msgWrapper.getMessageString());
        switch(msgWrapper.getHeader().getMessageType()){
            case PUTCHUNK:
                Backup backup = new Backup(parentPeer, msgWrapper);
                new Thread(backup).start();
                break;
            case STORED:
                parentPeer.updateFileStorage(msgWrapper);
                break;
            case GETCHUNK:
                Restore restore = new Restore(parentPeer, msgWrapper);
                new Thread(restore).start();
                break;
            case CHUNK:
                parentPeer.receiveChunk(msgWrapper);
                break;
            case DELETE:
                Delete delete = new Delete(parentPeer, msgWrapper);
                new Thread(delete).start();
                break;
            case REMOVED:
            default: return;
        }
    }
}
