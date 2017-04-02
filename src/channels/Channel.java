package channels;

import network.Peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import static utils.Utils.isValidIPV4;

public abstract class Channel implements Runnable {
    public static final int HEADER_SIZE = 256;
    public static final int BODY_SIZE = 64000;

    private boolean logSystem;
    private PrintWriter logFile;
    private String mcAddress;
    private int mcPort;
    private MulticastSocket socket;
    private InetAddress group;
    private Peer parentPeer;

    /**
     * Checks if the IPV4 address given in the args is valid, and if it is joins a multicast channel defined by the given address and port
     * @param mcAddress
     * @param mcPort
     * @param parentPeer
     * @throws UnknownHostException
     * @throws IOException
     */
    public Channel(String mcAddress, String mcPort, Peer parentPeer, boolean logSystem) throws UnknownHostException, IOException {
        if(!isValidIPV4(mcAddress)){
            System.out.println("Invalid IPV4 address!");
            return;
        }

        this.logSystem = logSystem;
        if(this.logSystem){
            try{
                logFile = new PrintWriter("data/Multicast.log", "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.parentPeer = parentPeer;

        this.mcAddress = mcAddress;
        this.mcPort = Integer.parseInt(mcPort);

        this.group = InetAddress.getByName(this.mcAddress);
        socket = new MulticastSocket(this.mcPort);
        socket.joinGroup(group);
    }

    /**
     * Returns the multicast port
     * @return mcPort
     */
    public int getMcPort() {
        return mcPort;
    }

    /**
     * Updates the multicast port
     * @param mcPort
     */
    public void setMcPort(int mcPort) {
        this.mcPort = mcPort;
    }

    /**
     * Returns the multicast socket where the communication was established
     * @return multicast socket
     */
    public MulticastSocket getSocket() {
        return socket;
    }

    /**
     * Updates the multicast socket
     * @param socket
     */
    public void setSocket(MulticastSocket socket) {
        this.socket = socket;
    }

    /**
     * Returns the multicast address
     * @return multicast address
     */
    public String getMcAddress() {
        return mcAddress;
    }

    /**
     * Updates the multicast address
     * @param mcAddress
     */
    public void setMcAddress(String mcAddress) {
        this.mcAddress = mcAddress;
    }

    /**
     * Changes the inetAddress instance
     * @param group
     */
    public void setGroup(InetAddress group) {
        this.group = group;
    }

    /**
     * Returns the inetAddress instance
     * @return InetAddress
     */
    public InetAddress getGroup() {
        return this.group;
    }

    /**
     * Returns the peer where this channel was initialized
     * @return Peer
     */
    public Peer getParentPeer() {
        return parentPeer;
    }

    /**
     * Updates the parent peer
     * @param parentPeer
     */
    public void setParentPeer(Peer parentPeer) {
        this.parentPeer = parentPeer;
    }

    /**
     * Sends a message via the multicast channel
     * @param message String
     */
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

    /**
     * Sends a message via the multicast channel
     * @param message byte[]
     */
    public void sendMessage(byte[] message){
        DatagramPacket packet = new DatagramPacket(message, message.length, group, mcPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logMessage(String s){
        if(logSystem)
            logFile.println(s);
    }
}
