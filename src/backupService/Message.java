package backupService;


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

    public String getMessageString(){
        if(this.body != null)
            return this.header.getMessageHeaderAsString() + this.body.getBody().toString();
        else return this.header.getMessageHeaderAsString();
    }

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

    public MessageHeader getHeader() {
        return header;
    }

    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    public MessageBody getBody() {
        return body;
    }

    public void setBody(MessageBody body) {
        this.body = body;
    }
}
