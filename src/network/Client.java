package network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Client {

    private static Registry registry;
    private static IClientPeer stub;

    public static void main(String[] args) throws InterruptedException, IOException, NotBoundException, NoSuchAlgorithmException {
        if(args.length != 2) {
            System.out.println("Usage: java -jar Client <host_name> <remote_object_name>");
            return;
        }

        String hostname = args[0];
        String remoteObjectName = args[1];

        registry = LocateRegistry.getRegistry(hostname);
        stub = (IClientPeer) registry.lookup(remoteObjectName);

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
                    stub.BackupFile(fileData, pathname, replicationDegree);
                    break;
                case 2:
                    System.out.println("File pathname: ");
                    pathname = scanner.nextLine();
                    stub.RestoreFile(pathname);
                    break;
                case 3:
                    System.out.println("_File pathname: ");
                    pathname = scanner.nextLine();
                    stub.DeleteFile(pathname);
                    break;
                case 4:
                    System.out.println("Maximum disk space available (in KBytes): ");
                    long maxDiskSpace = scanner.nextLong();
                    String msgManage = stub.ManageDiskSpace(maxDiskSpace);
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
