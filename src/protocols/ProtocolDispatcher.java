package protocols;

import messageSystem.Message;
import network.Peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProtocolDispatcher implements Runnable{
    private Peer parentPeer;
    private AtomicBoolean execute;
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Message> messages;
    private boolean logSystem;
    private PrintWriter logFile;

    public ProtocolDispatcher(Peer parentPeer, boolean logSystem){
        executor = Executors.newFixedThreadPool(5);
        execute = new AtomicBoolean(true);
        this.parentPeer = parentPeer;
        messages = new ConcurrentLinkedQueue<>();

        this.logSystem = logSystem;
        if(this.logSystem){
            try{
                logFile = new PrintWriter("data/Dispatcher.log", "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (execute.get() || !executor.isTerminated()) {
                Message message;
                while ((message = messages.poll()) != null) {
                    dispatchRequest(message);
                }
                // Sleep in case there wasn't any runnable in the queue. This helps to avoid hogging the CPU.
                Thread.sleep(1);
            }
        } catch (RuntimeException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatchRequest(Message message) throws IOException {
        logMessage(message.getMessageString());
        switch(message.getHeader().getMessageType()){
            case PUTCHUNK:
                Backup backup = new Backup(parentPeer, message);
                executor.execute(backup);
                break;
            case STORED:
                parentPeer.updateFileStorage(message);
                break;
            case GETCHUNK:
                Restore restore = new Restore(parentPeer, message);
                executor.execute(restore);
                break;
            case CHUNK:
                parentPeer.receiveChunk(message);
                break;
            case DELETE:
                Delete delete = new Delete(parentPeer, message);
                executor.execute(delete);
                break;
            case REMOVED:
                Manage manage = new Manage(parentPeer, message);
                executor.execute(manage);
                break;
            case ENH_DELETED:
                parentPeer.ENH_UpdateDeleteResponse(message);
                break;
            case ENH_AWOKE:
                DeleteEnhancement deleteEnhancement = new DeleteEnhancement(parentPeer, message);
                executor.execute(deleteEnhancement);
                break;
            default: return;
        }
    }

    public Peer getParentPeer() {
        return parentPeer;
    }

    public void setParentPeer(Peer parentPeer) {
        this.parentPeer = parentPeer;
    }

    public void addMessage(String msgWrapper) {
        Message message = new Message(msgWrapper);
        messages.add(message);
    }

    public void logMessage(String s){
        if(logSystem){
            logFile.println(s);
            logFile.flush();
        }
    }
}
