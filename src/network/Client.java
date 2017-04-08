package network;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Client {

    private static Registry registry;
    private static IClientPeer stub;

    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException, NoSuchAlgorithmException {
        if(args.length != 1) {
            System.out.println("Usage: java Client <peer_ap>");
            return;
        }

        String peer_ap = args[0];
        String IPV4Address;
        String remoteObject;
        String[] accessPointSplit = peer_ap.split(":");
        if(accessPointSplit.length == 2){
            IPV4Address = accessPointSplit[0];
            remoteObject = accessPointSplit[1];
        }else if(accessPointSplit.length == 1){
            remoteObject = accessPointSplit[0];
            IPV4Address = "127.0.0.1";
        }else{
            System.out.println("The initiator peer access point must be in the following format: <IP address>:<RemoteObject> or only <RemoteObject> if the initiator peer runs on the localhost");
            return;
        }

        registry = LocateRegistry.getRegistry(IPV4Address);
        stub = (IClientPeer) registry.lookup(remoteObject);

        Menu();
    }

    /**
     * Shows the menu
     */
    public static void Menu() throws InterruptedException, IOException, NoSuchAlgorithmException {
        boolean exit = false;
        while(!exit) {
            System.out.print(
                    "1 - Backup a file\n" +
                            "2 - Restore a file\n" +
                            "3 - Delete a file\n" +
                            "4 - Manage local service storage\n" +
                            "5 - Retrieve local service state information\n" +
                            "6 - Exit\n\n" +
                            "Select an option: ");
            Scanner scanner = new Scanner(System.in);
            int decider = scanner.nextInt();
            scanner.nextLine();     // Needed to pick up the '\n'

            switch (decider) {
                case 1:
                    System.out.print("File pathname: ");
                    String pathname = scanner.nextLine();
                    System.out.print("Replication degree: ");
                    int replicationDegree = scanner.nextInt();
                    scanner.nextLine();

                    Path path = Paths.get(pathname);
                    byte[] fileData = Files.readAllBytes(path);
                    String message = stub.BackupFile(fileData, pathname, replicationDegree);
                    System.out.println(message);
                    break;
                case 2:
                    System.out.print("File pathname: ");
                    pathname = scanner.nextLine();
                    fileData = stub.RestoreFile(pathname);
                    if(fileData == null){
                        System.out.println("The specified file cannot be restored");
                    }else{
                        FileOutputStream fos = new FileOutputStream(pathname);
                        fos.write(fileData);
                        fos.close();
                        System.out.println("File successfully restored!");
                    }
                    break;
                case 3:
                    System.out.print("File pathname: ");
                    pathname = scanner.nextLine();
                    stub.DeleteFile(pathname, 1);
                    break;
                case 4:
                    System.out.print("Maximum disk space available (in KBytes): ");
                    long maxDiskSpace = scanner.nextLong();
                    String msgManage = stub.ManageDiskSpace(maxDiskSpace);
                    System.out.println("Ola1");
                    System.out.println(msgManage);
                    break;
                case 5:
                    String peerInfo = stub.RetrieveInformation();
                    System.out.println(peerInfo);
                    break;
                case 6:
                    exit = true;
                    break;
                default: break;
            }

            System.out.println();
        }

        System.out.println("Client has ended.");
    }
}
