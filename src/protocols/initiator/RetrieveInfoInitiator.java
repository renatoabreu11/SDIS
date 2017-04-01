package protocols.initiator;

import fileSystem.Chunk;
import fileSystem._File;
import network.Peer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class RetrieveInfoInitiator extends ProtocolInitiator{

    private String out;

    public RetrieveInfoInitiator(String version, boolean logSystem, Peer parentPeer) {
        super(version, logSystem, parentPeer);
        out = "\n\n**************************************************************************\n**************************************************************************\n**************************************************************************\n\n";
    }

    @Override
    public void startProtocol() {
        Iterator it = getParentPeer().getManager().getStorage().entrySet().iterator();
        ArrayList<Chunk> storedChunks = new ArrayList<>();

        while(it.hasNext()) {
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            String fileId = entry.getKey();
            _File file = entry.getValue();

            out += "File pathname: " + file.getPathname() + ", id: " + fileId + ", desired replication degree: " + file.getChunks().get(0).getReplicationDegree();
            for(Chunk chunk : file.getChunks()) {
                out += "\n\tChunk id: " + chunk.getChunkNo() + ", size: " + chunk.getChunkData().length + ", current replication degree: " + chunk.getCurrReplicationDegree();
                if(chunk.getPeers().contains(getParentPeer().getId()))
                    storedChunks.add(chunk);
            }

            out += "\n\n";
        }

        out += "Stored Chunks";
        for(Chunk chunk : storedChunks) {
            out += "\n\tChunk id: " + chunk.getChunkNo() + ", size: " + chunk.getChunkData().length + " bytes, perceived replication degree: " + chunk.getReplicationDegree();
        }

        out += "\n\nPeer's storage capacity: " + getParentPeer().getMaxDiskSpace() + " KBytes, current occupied storage: " + getParentPeer().getManager().getCurrOccupiedSize() / 1000 + " KBytes\n";
        out = "\n\n**************************************************************************\n**************************************************************************\n**************************************************************************\n\n";
    }

    public String getOut() {
        return out;
    }
}
