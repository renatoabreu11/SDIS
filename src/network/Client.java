package network;

import utils.Utils;

import java.io.FileOutputStream;
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
        if(args.length != 0) {
            System.out.println("Usage: java -jar Client");
            return;
        }

        String hostname = Utils.IPV4_ADDRESS;
        String remoteObjectName = "IClientPeer";

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
                    stub.DeleteFile(pathname);
                    break;
                case 4:
                    System.out.print("Maximum disk space available (in KBytes): ");
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
