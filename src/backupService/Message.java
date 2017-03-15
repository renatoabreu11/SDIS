package backupService;


public class Message {
    private MessageHeader header;
    private MessageBody body;

    public Message(MessageHeader header, MessageBody body){
        this.header = header;
        this.body = body;
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
