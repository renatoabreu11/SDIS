package protocols.initiator;

import fileSystem.Chunk;
import fileSystem._File;
import network.Peer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;

public class RetrieveInfoInitiator extends ProtocolInitiator{

    private String out;

    public RetrieveInfoInitiator(String version, boolean logSystem, Peer parentPeer) {
        super(version, logSystem, parentPeer);
        out = "\n\n**************************************************************************\n**************************************************************************\n**************************************************************************\n\n";
    }

    @Override
    public void startProtocol() throws IOException {
        Iterator it = getParentPeer().getManager().getStorage().entrySet().iterator();
        ArrayList<Chunk> storedChunks = new ArrayList<>();

        while(it.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, _File> entry = (Map.Entry<String, _File>) it.next();
            String fileId = entry.getKey();
            _File file = entry.getValue();

            out += "File pathname: " + file.getPathname() + ", id: " + fileId + ", desired replication degree: " + file.getChunks().get(0).getReplicationDegree();
            for(Chunk chunk : file.getChunks()) {
                Path path = Paths.get("data/" + fileId + chunk.getChunkNo());
                long bytesSize = Files.readAllBytes(path).length;
                out += "\n\tChunk id: " + chunk.getChunkNo() + ", size: " + bytesSize + " bytes, current replication degree: " + chunk.getCurrReplicationDegree();
                if(chunk.getPeers().contains(getParentPeer().getId()))
                    storedChunks.add(chunk);
            }

            out += "\n\n";
        }

        out += "Stored Chunks";
        for(Chunk chunk : storedChunks) {
            Path path = Paths.get("data/" + chunk.getFileId() + chunk.getChunkNo());
            long bytesSize = Files.readAllBytes(path).length;
            out += "\n\tChunk id: " + chunk.getChunkNo() + ", size: " + bytesSize + " bytes, perceived replication degree: " + chunk.getReplicationDegree();
        }

        long occupiedSpace = getParentPeer().getManager().getCurrOccupiedSize(getParentPeer().getId());
        out += "\n\nPeer's storage capacity: " + getParentPeer().getMaxDiskSpace() + " KBytes, current occupied storage: " + occupiedSpace + " Bytes (or " + occupiedSpace / 1000.0 + " KBytes).\n";
        out += "\n\n**************************************************************************\n**************************************************************************\n**************************************************************************\n\n";
    }

    public String getOut() {
        return out;
    }
}
