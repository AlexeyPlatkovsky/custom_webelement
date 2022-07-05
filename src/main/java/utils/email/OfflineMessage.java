package utils.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class OfflineMessage {
    private Date sentDate;
    private String subject;
    private Address[] recipients;
    private Address[] senders;
    private String folderName;
    private String content;

    @SneakyThrows
    public OfflineMessage(Message m) {
        sentDate = m.getSentDate();
        subject = m.getSubject();
        recipients = m.getAllRecipients();
        senders = m.getFrom();
        folderName = m.getFolder().getName();
        // if we read the message content then it's marked as read
        // and it won't be returned the next time we load messages from the folder
        // so the following line can be invoked only once for any message
        content = loadContent(m);
    }

    private String loadContent(Message message) throws MessagingException, IOException {
        Object content = message.getContent();
        if (!(content instanceof Multipart)) {
            return content.toString();
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Multipart mp = (Multipart) content;
        for (int i = 0; i < mp.getCount(); i++) {
            mp.getBodyPart(i).writeTo(os);
            os.write('\n');
        }
        return os.toString("UTF-8");
    }

}
