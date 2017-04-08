package protocols.initiator;

import fileSystem.Chunk;
import fileSystem._File;
import network.Peer;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;
import static jdk.nashorn.internal.objects.NativeMath.round;

public class RetrieveInfoInitiator extends ProtocolInitiator{

    private String out;

    public RetrieveInfoInitiator(String version, boolean logSystem, Peer parentPeer) {
        super(version, logSystem, parentPeer);
        out = "\n\n**************************************************************************\n*************************** Local Service Info ***************************\n**************************************************************************\n\n";
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
                out += "\n\tChunk id: " + chunk.getChunkNo() + ", perceived replication degree: " + chunk.getCurrReplicationDegree();
                if(chunk.getPeers().contains(getParentPeer().getId()))
                    storedChunks.add(chunk);
            }

            out += "\n\n";
        }

        out += "Stored Chunks\n";
        if(storedChunks.size() == 0)
            out += "\tThere are no stored chunks in this peer.";
        else {
            for(Chunk chunk : storedChunks) {
                Path path = Paths.get(Utils.CHUNKS_DIR + chunk.getFileId() + chunk.getChunkNo());
                long bytesSize = Files.readAllBytes(path).length;
                out += "\n\tChunk id: " + chunk.getChunkNo() + ", size: " + bytesSize + " bytes, perceived replication degree: " + chunk.getCurrReplicationDegree();
            }
        }

        long occupiedSpace = getParentPeer().getManager().getCurrOccupiedSize(getParentPeer().getId());
        long maxDiskSpace = getParentPeer().getMaxDiskSpace();
        double percentegeOccupied = Math.round((occupiedSpace/1000.0) / maxDiskSpace * 100);
        out += "\n\nPeer's storage capacity: " + maxDiskSpace + " KBytes, current occupied storage: " + occupiedSpace + " Bytes (or " + occupiedSpace / 1000.0 + " KBytes).\n" + percentegeOccupied + "% of 100% occupied.\n";
        out += "\n\n**************************************************************************\n**************************************************************************\n**************************************************************************\n\n";
    }

    public String getOut() {
        return out;
    }
}
