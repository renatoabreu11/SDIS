package messageSystem;


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
