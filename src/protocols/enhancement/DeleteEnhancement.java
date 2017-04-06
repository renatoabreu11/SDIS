package protocols.enhancement;

import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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

        Map<String, ArrayList<Integer>> filesForAwoke = parentPeer.getSendersIdRepliesToDelete();
        Iterator it = filesForAwoke.entrySet().iterator();

        while(it.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> entry = (Map.Entry<String, ArrayList<Integer>>) it.next();
            String fileId = entry.getKey();
            ArrayList<Integer> peersId = entry.getValue();

            if(peersId.contains(senderId)) {
                try {
                    parentPeer.DeleteFile(fileId);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
