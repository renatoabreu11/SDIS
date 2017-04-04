package utils;

import java.util.regex.Pattern;

public class Utils {
    public static long RecoverMaxTime = 2500;

    public enum MessageType {
        PUTCHUNK,
        STORED,
        GETCHUNK,
        CHUNK,
        DELETE,
        REMOVED
    }

    public enum SubProtocol{
        BACKUP,
        RESTORE,
        DELETE,
        RECLAIM,
        STATE
    }

    public final static char CR  = (char) 0x0D;
    public final static char LF  = (char) 0x0A;

    public final static String CRLF  = "" + CR + LF;

    static private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
    static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValidIPV4(final String s)
    {
        return IPV4_PATTERN.matcher(s).matches();
    }

    public final static int BackupRetransmissions = 5;
    public final static int DeleteRetransmissions = 3;

    public final static int RMI_PORT = 1099;
    public final static String IPV4_ADDRESS = "192.168.1.8";

    public final static String METADATA_PATHNAME = "data/chunks/metadata.txt";
}
