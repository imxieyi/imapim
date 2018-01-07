package imapim.protocol;

import imapim.data.ChatMessage;

import javax.mail.MessagingException;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class SendMailQueue extends Observable {

    private Logger log = Logger.getLogger(SendMailQueue.class.getName());

    private static SendMailQueue ourInstance = null;

    public static synchronized SendMailQueue getInstance() {
        if(ourInstance == null) {
            ourInstance = new SendMailQueue();
        }
        return ourInstance;
    }

    private Queue<ChatMessage> cmQueue = new ConcurrentLinkedQueue<>();
    private Semaphore semaphore = new Semaphore(0);
    private Thread sendThread;

    private SendMailQueue() {
    }

    public void add(ChatMessage cm) {
        cmQueue.add(cm);
        semaphore.release();
    }

    public void start() {
        cmQueue.clear();
        sendThread = new Thread(() -> {
            while(!Thread.interrupted()) {
                ChatMessage cm = null;
                try {
                    semaphore.acquire();
                    cm = cmQueue.poll();
                    SMTPHelper.getInstance().send(cm.mail);
                    setChanged();
                    notifyObservers(cm);
                } catch (MessagingException e) {
                    log.severe("Failed to send email: " + e.getLocalizedMessage() + "\n" + cm.mail);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        sendThread.start();
    }

    public void stop() {
        if (sendThread != null) {
            sendThread.interrupt();
        }
    }

}
