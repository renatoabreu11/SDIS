package protocols;

import messageSystem.Message;
import messageSystem.MessageHeader;
import network.Peer;

import java.io.IOException;

public class Delete implements Runnable {

    private Peer parentPeer;
    private Message message;

    public Delete(Peer parentPeer, Message message) {
        this.parentPeer = parentPeer;
        this.message = message;
    }

    @Override
    public void run() {
        MessageHeader header = message.getHeader();
        String fileId = header.getFileId();

        try {
            parentPeer.deleteFileStorage(fileId);

            // Updates peer's metadata.
            parentPeer.getManager().WriteMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
