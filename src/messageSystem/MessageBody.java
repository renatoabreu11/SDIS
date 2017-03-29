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

    /**
     * Set the body data
     * @param body
     */
    public void setBody(byte[] body) {
        this.body = body;
    }
}
