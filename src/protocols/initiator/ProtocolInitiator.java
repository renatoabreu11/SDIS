package protocols.initiator;

import messageSystem.Message;
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

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isLogSystem() {
        return logSystem;
    }

    public void setLogSystem(boolean logSystem) {
        this.logSystem = logSystem;
    }

    public PrintWriter getLogFile() {
        return logFile;
    }

    public void setLogFile(PrintWriter logFile) {
        this.logFile = logFile;
    }

    public void closeLogWriter(){
        if(logSystem)
            logFile.close();
    }

    public Peer getParentPeer() {
        return parentPeer;
    }

    public void setParentPeer(Peer parentPeer) {
        this.parentPeer = parentPeer;
    }

    public abstract void startProtocol() throws IOException;

    public void logMessage(String s){
        if(logSystem)
            logFile.println(s);
    }

    public void endProtocol() {
        if(logSystem)
            logFile.close();
    }
}
