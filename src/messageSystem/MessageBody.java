package messageSystem;

public class MessageBody {

    private byte[] body;

    public MessageBody(byte[] body){
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
