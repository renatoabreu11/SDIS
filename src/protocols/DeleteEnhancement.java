package protocols;

import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;
import utils.Utils;

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

        if((version.equals(Utils.ENHANCEMENT_DELETE) || version.equals(Utils.ENHANCEMENT_ALL)) &&
                (parentPeer.getProtocolVersion().equals(Utils.ENHANCEMENT_DELETE) || parentPeer.getProtocolVersion().equals(Utils.ENHANCEMENT_ALL))) {
            Map<String, ArrayList<Integer>> filesForAwoke = parentPeer.getManager().getHostIdDelete();
            Iterator it = filesForAwoke.entrySet().iterator();

            while(it.hasNext()) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, ArrayList<Integer>> entry = (Map.Entry<String, ArrayList<Integer>>) it.next();
                String fileId = entry.getKey();
                ArrayList<Integer> peersId = entry.getValue();

                if(peersId.contains(senderId)) {
                    try {
                        parentPeer.DeleteFile(fileId, 2);
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
}
