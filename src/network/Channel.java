package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import static utils.Utils.isValidIPV4;

public abstract class Channel implements Runnable {
    public static final int HEADER_SIZE = 256;
    public static final int BODY_SIZE = 64000;

    private String mcAddress;
    private int mcPort;
    private MulticastSocket socket;
    private InetAddress group;
    private Peer parentPeer;

    public Channel(String mcAddress, String mcPort, Peer parentPeer) throws UnknownHostException, IOException {
        if(!isValidIPV4(mcAddress)){
            System.out.println("Invalid IPV4 address!");
            return;
        }

        this.parentPeer = parentPeer;

        this.mcAddress = mcAddress;
        this.mcPort = Integer.parseInt(mcPort);

        this.group = InetAddress.getByName(this.mcAddress);
        socket = new MulticastSocket(this.mcPort);
        socket.joinGroup(group);
    }

    public int getMcPort() {
        return mcPort;
    }

    public void setMcPort(int mcPort) {
        this.mcPort = mcPort;
    }

    public MulticastSocket getSocket() {
        return socket;
    }

    public void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    public String getMcAddress() {
        return mcAddress;
    }

    public void setMcAddress(String mcAddress) {
        this.mcAddress = mcAddress;
    }

    public void setGroup(InetAddress group) {
        this.group = group;
    }

    public InetAddress getGroup() {
        return this.group;
    }

    public void sendMessage(String message){
        byte[] buffer = message.getBytes();
        DatagramPacket packet = null;
        packet = new DatagramPacket(buffer, buffer.length, group, mcPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(byte[] message){
        DatagramPacket packet = new DatagramPacket(message, message.length, group, mcPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Peer getParentPeer() {
        return parentPeer;
    }

    public void setParentPeer(Peer parentPeer) {
        this.parentPeer = parentPeer;
    }
}
