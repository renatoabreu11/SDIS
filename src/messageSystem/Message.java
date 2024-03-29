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
     * Given a string message, parses it accordingly to the specification
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
        String version, fileId = null;
        int senderId = -1, chunkNo, replicationDegree;
        version = headerSplit[1];

        switch(headerSplit[0]){
            case "PUTCHUNK":
                type = Utils.MessageType.PUTCHUNK; numberOfArgs = 6; break;
            case "STORED":
                type = Utils.MessageType.STORED; numberOfArgs = 5; break;
            case "GETCHUNK":
                type = Utils.MessageType.GETCHUNK;
                if(version.equals(Utils.ENHANCEMENT_RESTORE) || version.equals(Utils.ENHANCEMENT_ALL))
                    numberOfArgs = 6;
                else numberOfArgs = 5;
                break;
            case "CHUNK":
                type = Utils.MessageType.CHUNK; numberOfArgs = 5; break;
            case "DELETE":
                type = Utils.MessageType.DELETE; numberOfArgs = 4; break;
            case "REMOVED":
                type = Utils.MessageType.REMOVED; numberOfArgs = 5; break;
            case "ENH_DELETED":
                type = Utils.MessageType.ENH_DELETED; numberOfArgs = 3; break;
            case "ENH_AWOKE":
                type = Utils.MessageType.ENH_AWOKE; numberOfArgs = 3; break;
            default:
                return;
        }

        if(headerSplit.length != numberOfArgs)
            return;

        senderId = Integer.parseInt(headerSplit[2]);

        if(numberOfArgs >= 4)
            fileId = headerSplit[3];
        else
            fileId = headerSplit[1];

        if(type == Utils.MessageType.PUTCHUNK){
            chunkNo = Integer.parseInt(headerSplit[4]);
            replicationDegree = Integer.parseInt(headerSplit[5]);
            header = new MessageHeader(type, version, senderId, fileId, chunkNo, replicationDegree);
        } else if(type == Utils.MessageType.GETCHUNK && (version.equals(Utils.ENHANCEMENT_RESTORE) || version.equals(Utils.ENHANCEMENT_ALL))){
            chunkNo = Integer.parseInt(headerSplit[4]);
            String sender_access = headerSplit[5];
            header = new MessageHeader(type, version, senderId, fileId, chunkNo, sender_access);
        } else if(type == Utils.MessageType.GETCHUNK || type == Utils.MessageType.CHUNK || type == Utils.MessageType.REMOVED || type == Utils.MessageType.STORED){
            chunkNo = Integer.parseInt(headerSplit[4]);
            header = new MessageHeader(type, version, senderId, fileId, chunkNo);
        } else if(type == Utils.MessageType.ENH_AWOKE)
            header = new MessageHeader(type, version, senderId);
        else if(type == Utils.MessageType.ENH_DELETED)
            header = new MessageHeader(type, senderId, fileId);
        else
            header = new MessageHeader(type, version, senderId, fileId);

        if(msgBody != null){
            body = new MessageBody(msgBody.getBytes());
        }
    }

    /**
     * Return the message as a string
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
     * Return the message as a byte array
     * @return
     * @throws IOException
     */
    public byte[] getMessageBytes() throws IOException {
        byte header[] = this.header.getMessageHeaderAsString().getBytes();

        if(this.body != null){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write( header );
            outputStream.write( body.getBody() );

            return outputStream.toByteArray( );
        } else return header;
    }

    /**
     * Get the message header
     * @return
     */
    public MessageHeader getHeader() {
        return header;
    }

    /**
     * Set the message header
     * @param header
     */
    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    /**
     * Get the message body
     * @return
     */
    public MessageBody getBody() {
        return body;
    }
}
