package protocols;

import messageSystem.Message;
import network.Peer;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProtocolDispatcher implements Runnable{
    private Peer parentPeer;
    private AtomicBoolean execute;
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Message> messages;

    public ProtocolDispatcher(Peer parentPeer){
        executor = Executors.newFixedThreadPool(5);
        execute = new AtomicBoolean(true);
        this.parentPeer = parentPeer;
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
        } catch (RuntimeException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatchRequest(Message message) throws IOException {
        System.out.println(message.getMessageString());
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
            default: return;
        }
    }

    public Peer getParentPeer() {
        return parentPeer;
    }

    public void setParentPeer(Peer parentPeer) {
        this.parentPeer = parentPeer;
    }

    public void setMessages(ConcurrentLinkedQueue<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(String msgWrapper) {
        Message message = new Message(msgWrapper);
        messages.add(message);
    }
}
