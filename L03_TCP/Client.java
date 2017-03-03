import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Evenilink on 03/03/2017.
 */
public class Client {
    public static void main(String args[]) throws IOException {
        if(args.length != 4) {
            System.out.println("Usage: java Client <host_name> <port_number> <oper> <opnd>");
            return;
        }

        System.out.println("Client is ready...");

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        String msgToSend = args[2] + " " + args[3];

        Socket socket = new Socket(hostName, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println(msgToSend);     // Sending a message to the server.
        String msgReceived = in.readLine();      // Reading a message from the server.
        System.out.println("Reply from Server: " + msgReceived);

        in.close();
        out.close();
        socket.close();
    }
}
