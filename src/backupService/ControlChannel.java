package backupService;

import network.Peer;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ControlChannel extends Channel {

    private ArrayList<Integer> peerResponded;
    private long time;
    private boolean isCounting;
    private int numRetransmission;

    private String pathname;
    private int replicationDegree;

    public ControlChannel(String mcAddress, String mcPort, Peer parentPeer) throws IOException {
        super(mcAddress, mcPort, parentPeer);
        peerResponded = new ArrayList<>();
        time = 0;
        isCounting = false;
        numRetransmission = 0;
        System.out.println("Control channel online.");
    }

    public void StartTime() {
        time = System.currentTimeMillis();
    }

    public void ActivateListenReplies(String pathname, int replicationDegree) {
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
        StartTime();
        isCounting = true;
    }

    @Override
    public void run() {
        // We need a way to define THIS as the initiator peer, so that only him can run this code.
        // O tempo fica 1, 2, 3, 4, 5 ou 1, 2, 4, 8, 16??????????????

        while (true) {
            byte[] buffer = new byte[HEADER_SIZE];
            DatagramPacket dgp = new DatagramPacket(buffer, buffer.length);
            try {
                if(isCounting) {
                    if(System.currentTimeMillis() - time > 1 * (numRetransmission+1)) {
                        getParentPeer().BackupFile(pathname, replicationDegree);
                        numRetransmission++;
                        StartTime();
                    } else {
                        this.getSocket().receive(dgp);
                        String message = new String(dgp.getData());
                        System.out.println("MC message: " + message);

                        // Initiator peer received the reply message.

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Integer> getPeerResponded() {
        return peerResponded;
    }

    public void setPeerResponded(ArrayList<Integer> peerResponded) {
        this.peerResponded = peerResponded;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isCounting() {
        return isCounting;
    }

    public void setCounting(boolean counting) {
        isCounting = counting;
    }

    public int getNumRetransmission() {
        return numRetransmission;
    }

    public void setNumRetransmission(int numRetransmission) {
        this.numRetransmission = numRetransmission;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}
