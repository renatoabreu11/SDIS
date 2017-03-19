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
        String hostname = "localhost";
        String remoteObjectName = "IClientPeer";

        registry = LocateRegistry.getRegistry(hostname);
        stub = (IClientPeer) registry.lookup(remoteObjectName);

        Menu();
    }

    /**
     * Shows the menu
     */
    public static void Menu() throws InterruptedException, IOException, NoSuchAlgorithmException {
        System.out.print(
                "1 - Backup a file\n" +
                "2 - Restore a file\n" +
                "3 - Delete a file\n" +
                "4 - Manage local service storage\n" +
                "5 - Retrieve local service state information\n\n" +
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
                System.out.println("File pathname: ");
                pathname = scanner.nextLine();
                stub.DeleteFile(pathname);
                break;
            default: break;
        }

        System.out.println("Client has ended.");
    }
}
