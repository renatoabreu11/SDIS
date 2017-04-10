package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class Utils {

    public static String ENHANCEMENT_BACKUP = "1.1";
    public static String ENHANCEMENT_RESTORE = "1.2";
    public static String ENHANCEMENT_DELETE = "1.3";
    public static String ENHANCEMENT_ALL = "2.0";

    public static long RecoverMaxTime = 7500;
    public static long DELETED_MAX_TIME = 2500;

    public static long MAX_DISK_SPACE = 740;

    public static boolean LOG_SYSTEM = true;

    public enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        CHUNK,
        DELETE,
        REMOVED,
        ENH_DELETED,
        ENH_AWOKE
    }

    public enum SubProtocol{
        BACKUP,
        RESTORE,
        DELETE,
        RECLAIM,
        STATE
    }

    public static final int HEADER_SIZE = 256;
    public static final int BODY_SIZE = 64000;

    public final static char CR  = (char) 0x0D;
    public final static char LF  = (char) 0x0A;

    public final static String CRLF  = "" + CR + LF;

    public final static int BackupRetransmissions = 5;
    public final static int DeleteRetransmissions = 3;

    public final static int RMI_PORT = 1099;

    public final static String CHUNKS_DIR = "bin/data/chunks/";
    public final static String METADATA_PATHNAME = "bin/data/chunks/metadata.txt";
    public final static String PEERS_TO_DELETE_PATHNAME = "bin/data/peers_to_delete.txt";

    static private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValidIPV4(final String s)
    {
        return IPV4_PATTERN.matcher(s).matches();
    }

    public static String getIPV4address(){
        String ip;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    if(Utils.isValidIPV4(ip))
                        return ip;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
