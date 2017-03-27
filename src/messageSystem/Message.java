package messageSystem;


import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Message {

    private MessageHeader header;
    private MessageBody body;

    /**
     * Constructor with a header and a body.
     * @param header
     * @param body
     */
    public Message(MessageHeader header, MessageBody body){
        this.header = header;
        this.body = body;
    }

    /**
     * Constructor with only a header.
     * @param header
     */
    public Message(MessageHeader header) {
        this.header = header;
        this.body = null;
    }

    /**
     *
     * @param message
     */
    public Message(String message){
        String[] msgSplit = message.split("\\R\\R", 2);

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

        if(msgBody != null){
            body = new MessageBody(msgBody.getBytes());
        }
    }

    /**
     *
     * @return
     */
    public String getMessageString(){
        if(this.body != null){
            String str = new String(this.body.getBody());
            return this.header.getMessageHeaderAsString() + str;
        }
        else return this.header.getMessageHeaderAsString();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public byte[] getMessageBytes() throws IOException {
        byte header[] = this.header.getMessageHeaderAsString().getBytes();

        if(this.body != null){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write( header );
            outputStream.write( body.getBody() );

            byte messageBytes[] = outputStream.toByteArray( );
            return messageBytes;
        } else return header;
    }

    /**
     *
     * @return
     */
    public MessageHeader getHeader() {
        return header;
    }

    /**
     *
     * @param header
     */
    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    /**
     *
     * @return
     */
    public MessageBody getBody() {
        return body;
    }

    /**
     *
     * @param body
     */
    public void setBody(MessageBody body) {
        this.body = body;
    }
}
