package protocols.enhancement;

import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class DeleteEnhancement implements Runnable {

    private Peer parentPeer;
    private Message request;

    public DeleteEnhancement(Peer parentPeer, Message request) {
        this.parentPeer = parentPeer;
        this.request = request;

        System.out.println("Starting Delete Enhancement.");
    }

    @Override
    public void run() {
        MessageHeader header = request.getHeader();

        String version = header.getVersion();
        int senderId = header.getSenderId();

        // Verificar se este peer tem algum peerId que nao estava activo no momento do delete.
        Map<String, ArrayList<Integer>> filesForAwoke = parentPeer.getSendersIdRepliesToDelete();

        Iterator it = filesForAwoke.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> entry = (Map.Entry<String, ArrayList<Integer>>) it.next();
            String fileId = entry.getKey();
            //ArrayList<Integer>
        }
    }
}
