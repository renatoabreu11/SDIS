package network;

import utils.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;

public class TestApp {
    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException, NoSuchAlgorithmException {
        if(args.length < 2 || args.length > 4) {
            System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return;
        }

        String peer_ap = args[0];
        String sub_protocol = args[1];
        String IPV4Address = null;
        String remoteObject;

        boolean validProtocol = false;
        for ( Utils.SubProtocol m : Utils.SubProtocol.values()) {
            if (m.name().equals(sub_protocol)) {
                validProtocol = true;
                break;
            }
        }

        if(!validProtocol){
            System.out.println("The sub protocol specified must be one of the following: BACKUP, RESTORE, DELETE, RECLAIM, STATE");
            return;
        }

        Utils.SubProtocol request = Utils.SubProtocol.valueOf(sub_protocol);
        switch (request){
            case BACKUP:
                if(args.length != 4){
                    System.out.println("Usage: java TestApp <peer_ap> BACKUP <pathname> <replication_degree>");
                    return;
                }
                break;
            case RESTORE:
                if(args.length != 3){
                    System.out.println("Usage: java TestApp <peer_ap> RESTORE <pathname>");
                    return;
                }
                break;
            case DELETE:
                if(args.length != 3){
                    System.out.println("Usage: java TestApp <peer_ap> DELETE <pathname>");
                    return;
                }
                break;
            case RECLAIM:
                if(args.length != 3){
                    System.out.println("Usage: java TestApp <peer_ap> RECLAIM <space_reclaim>");
                    return;
                }
                break;
            case STATE:
                if(args.length != 3){
                    System.out.println("Usage: java TestApp <peer_ap> STATE <space_reclaim>");
                    return;
                }
                break;
        }

        String[] accessPointSplit = peer_ap.split(":");
        if(accessPointSplit.length == 2){
            IPV4Address = accessPointSplit[0];
            remoteObject = accessPointSplit[1];
        }else if(accessPointSplit.length == 1){
            remoteObject = accessPointSplit[0];
            IPV4Address = "127.0.0.1";
        }else{
            System.out.println("The peer access point must be in the following format: <IP address>:<RemoteObject> or only <RemoteObject> if the initiator peer runs on the localhost");
            return;
        }

        Registry registry = LocateRegistry.getRegistry(IPV4Address);
        IClientPeer stub = (IClientPeer) registry.lookup(remoteObject);

        String pathname;
        switch (request){
            case BACKUP:
                pathname = args[2];
                int replicationDegree = Integer.parseInt(args[3]);
                Path path = Paths.get(pathname);
                byte[] fileData = Files.readAllBytes(path);
                String message = stub.BackupFile(fileData, pathname, replicationDegree);
                System.out.println(message);
                break;
            case RESTORE:
                pathname = args[2];
                byte[] recoveredChunks = stub.RestoreFile(pathname);
                FileOutputStream fos = new FileOutputStream(pathname);
                fos.write(recoveredChunks);
                fos.close();
                break;
            case DELETE:
                pathname = args[2];
                stub.DeleteFile(pathname);
                break;
            case RECLAIM:
                int spaceToReclaim = Integer.parseInt(args[2]);
                stub.ManageDiskSpace(spaceToReclaim); // Space to reclaim, however in the protocol the variable that is defined is the maxSpace
                break;
            case STATE:
                System.out.println(stub.RetrieveInformation());
                break;
        }

    }
}
