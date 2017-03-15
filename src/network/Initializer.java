package network;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Initializer {

    public static void main(String[] args) {
        if(args.length != 4) {
            System.out.println("Usage: java Initializer <protocol_version> <server_id> <service_access_point> <mc:port> <mdb:port> <mdl:port>");
            return;
        }

        String protocolVersion = args[0];
        String serverId = args[1];
        String serviceAccessPoint = args[2];

        String[] msgSplit = args[3].split(":");
        String multicastAddress = msgSplit[0];
        String multicastPort = msgSplit[1];

        //msgSplit = args[4].split(":");
        String mdbAddress = "1";//msgSplit[0];
        String mdbPort = "1";//msgSplit[1];

        //msgSplit = args[5].split(":");
        String mdlAddress = "1";//msgSplit[0];
        String mdlPort = "1";//msgSplit[1];

        /*
        * TODO:
        * analyse the protocol version restrictions here.
        * */

        Peer peerObj = new Peer(multicastAddress, Integer.parseInt(multicastPort), mdbAddress, Integer.parseInt(mdbPort), mdlAddress, Integer.parseInt(mdlPort));

        try {
            IClientPeer stub = (IClientPeer) UnicastRemoteObject.exportObject(peerObj, 0);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind(serviceAccessPoint, stub);

            System.out.println("Initializer is ready.");
        } catch (RemoteException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
