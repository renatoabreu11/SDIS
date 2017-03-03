import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Evenilink on 03/03/2017.
 */
public class Server {
    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Server <srvc_port>");
            return;
        }

        System.out.println("Server is ready...");
        int port = Integer.parseInt(args[0]);
        HashMap<String, String> vehicles = new HashMap<>();

        ServerSocket serverSocket = new ServerSocket(port);
        String msgReceived, msgToSend = null;

        while (true) {
            //The 'accept' method waits until a client starts up and requests a connection on the host and port of this server.
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            msgReceived = in.readLine();

            String[] msgSplit = msgReceived.split(" ");
            switch(msgSplit[0]) {
                case "REGISTER":
                    vehicles.put(msgSplit[1], msgSplit[2]);
                    msgToSend = "Vehicle '" + msgSplit[1] + "' registered successfully.";
                    out.println(msgToSend);
                    break;
                case "LOOKUP":
                    String owner = vehicles.get(msgSplit[1]);
                    if(owner == null)
                        msgToSend = "There is no vehicle '" + msgSplit[1] + "' registered.";
                    else
                        msgToSend = "The owner of '" + msgSplit[1] + "' is " + owner + ".";
                    out.println(msgToSend);
                default: break;
            }
            
            System.out.println(msgSplit[0] + " " + msgSplit[1] + ":: " + msgToSend);
            in.close();
            out.close();
            clientSocket.close();
        }

        //serverSocket.close();
    }
}
