package imapim.protocol;

import imapim.data.Email;
import imapim.data.Message;

public interface SendMailCallback {

    void cb(Email email, Message message);

}
