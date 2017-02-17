public class MulticastSocketClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("Usage: java MulticastSocketClient <mcast_addr> <mcast_port> <oper> <opnd>*");
            return;
        }

        String pattern = "\\w{2}-\\w{2}-\\w{2}";
        String request = args[2];
        String plate_number = args[3];

        if(!plate_number.matches(pattern)){
            System.out.println("The plate number must be in the format XX-XX-XX where X is a letter or a digit.");
            return;
        }
    }
}
