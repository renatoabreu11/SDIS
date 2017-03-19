package messageSystem;

public class MessageBody {

    private byte[] body;

    /**
     *
     * @param body
     */
    public MessageBody(byte[] body){
        this.body = body;
    }

    /**
     *
     * @return
     */
    public byte[] getBody() {
        return body;
    }

    /**
     *
     * @param body
     */
    public void setBody(byte[] body) {
        this.body = body;
    }
}
