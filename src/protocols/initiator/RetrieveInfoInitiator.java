package protocols.initiator;

import network.Peer;

public class RetrieveInfoInitiator extends ProtocolInitiator{
    public RetrieveInfoInitiator(String version, boolean logSystem, Peer parentPeer) {
        super(version, logSystem, parentPeer);
    }

    @Override
    public void startProtocol() {

    }
}
