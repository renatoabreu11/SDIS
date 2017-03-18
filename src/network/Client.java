package network;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Client {

    private static Registry registry;
    private static IClientPeer stub;

    public static void main(String[] args) {
        String hostname = "localhost";
        String remoteObjectName = "IClientPeer";

        try {
            registry = LocateRegistry.getRegistry(hostname);
            stub = (IClientPeer) registry.lookup(remoteObjectName);

            Menu();
        } catch (RemoteException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Shows the menu
     */
    public static void Menu() {
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

                try {
                    stub.BackupFile(pathname, replicationDegree);
                } catch (RemoteException e) {
                    System.out.println(e.toString());
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                System.out.println("File pathname: ");
                pathname = scanner.nextLine();

                try {
                    stub.RestoreFile(pathname);
                } catch (RemoteException e) {
                    System.out.println(e.toString());
                    e.printStackTrace();
                }
                break;
            case 3:
                System.out.println("File pathname: ");
                pathname = scanner.nextLine();

                try {
                    stub.DeleteFile(pathname);
                } catch (RemoteException e) {
                    System.out.println(e.toString());
                    e.printStackTrace();
                }
                break;
            default: break;
        }
    }
}
