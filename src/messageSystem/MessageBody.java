package messageSystem;

public class MessageBody {

    private byte[] body;

    /**
     * MessageBody constructor
     * @param body
     */
    public MessageBody(byte[] body){
        this.body = body;
    }

    /**
     * Get the body data
     * @return
     */
    public byte[] getBody() {
        return body;
    }
}
