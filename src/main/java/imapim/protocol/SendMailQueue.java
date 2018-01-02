package imapim.protocol;

import imapim.data.Email;
import imapim.data.Message;

import javax.mail.MessagingException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class SendMailQueue {

    Logger log = Logger.getLogger(SendMailQueue.class.getName());

    private static SendMailQueue ourInstance = null;

    public static synchronized SendMailQueue getInstance() {
        if(ourInstance == null) {
            ourInstance = new SendMailQueue();
        }
        return ourInstance;
    }

    private Queue<Email> emailQueue = new ConcurrentLinkedQueue<>();
    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();
    private Semaphore semaphore = new Semaphore(0);
    private Thread sendThread;
    private SendMailCallback sendCallback;

    private SendMailQueue() {
    }

    public void add(Email mail, Message message) {
        emailQueue.add(mail);
        messageQueue.add(message);
        semaphore.release();
    }

    public void start() {
        emailQueue.clear();
        sendThread = new Thread(() -> {
            while(!Thread.interrupted()) {
                Email email = null;
                Message message = null;
                try {
                    semaphore.acquire();
                    email = emailQueue.poll();
                    message = messageQueue.poll();
                    SMTPHelper.getInstance().send(email);
                    if(sendCallback != null) {
                        sendCallback.cb(email, message);
                    }
                } catch (MessagingException e) {
                    log.severe("Failed to send email: " + e.getLocalizedMessage() + "\n" + email);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        sendThread.start();
    }

    public void stop() {
        sendThread.interrupt();
    }

    public void setCallback(SendMailCallback callback) {
        sendCallback = callback;
    }

}
