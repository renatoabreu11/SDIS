package protocols.initiator;

import network.Peer;

public class ManageDiskInitiator extends ProtocolInitiator{
    private long maxDiskSpace;
    public ManageDiskInitiator(String version, boolean logSystem, Peer parentPeer, long client_maxDiskSpace) {
        super(version, logSystem, parentPeer);
        maxDiskSpace = client_maxDiskSpace;
    }

    @Override
    public void startProtocol() {
    }
}
