package protocols;

import network.Peer;

public class ProtocolDispatcher {
    private String message;

    public ProtocolDispatcher(String message){
        this.message = message;
        String[] msgSplit = this.message.split("\\s+", 6);

        //Fazer parse da mensagem recebida

        String type = msgSplit[0];
        if(!type.equals("PUTCHUNK"))
            return;
        String version = msgSplit[1];
        String senderId = msgSplit[2];
        String fileId = msgSplit[3];
        String chunkNo = msgSplit[4];
        String replicationDegree = msgSplit[5];
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void dispatchRequest(Peer parentPeer) {
        //switch com o tipo de messagem e respetiva criação do thread
    }
}
