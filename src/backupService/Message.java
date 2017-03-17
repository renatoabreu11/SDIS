package backupService;


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
