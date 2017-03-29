package protocols.initiator;

import network.Peer;

public class ManageDiskInitiator extends ProtocolInitiator{
    public ManageDiskInitiator(String version, boolean logSystem, Peer parentPeer) {
        super(version, logSystem, parentPeer);
    }

    @Override
    public void startProtocol() {

    }
}
