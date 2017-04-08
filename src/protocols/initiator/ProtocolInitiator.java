package protocols.initiator;

import network.Peer;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class ProtocolInitiator {

    private String version;
    private boolean logSystem;
    private PrintWriter logFile;
    private Peer parentPeer;

    public ProtocolInitiator(String version, boolean logSystem, Peer parentPeer){
        this.version = version;
        this.logSystem = logSystem;
        if(this.logSystem){
            try{
                logFile = new PrintWriter("data/Protocol.log", "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.parentPeer = parentPeer;
    }

    public String getVersion() {
        return version;
    }

    public Peer getParentPeer() {
        return parentPeer;
    }

    public void setParentPeer(Peer parentPeer) {
        this.parentPeer = parentPeer;
    }

    public abstract void startProtocol() throws IOException, InterruptedException;

    public void logMessage(String s){
        if(logSystem){
            logFile.println(s);
            logFile.flush();
        }
    }

    public String endProtocol() {
        if(logSystem)
            logFile.close();
        return null;
    }
}
