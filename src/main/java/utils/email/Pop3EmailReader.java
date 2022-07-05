package utils.email;

import lombok.SneakyThrows;

import javax.mail.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
  Before reading emails you have to
  1. Enable Less secure app access here https://myaccount.google.com/lesssecureapps
  for the test@gmail.com account to allow this class to load emails via POP3
  2. Enable the POP protocol itself here https://mail.google.com/mail/u/1/?tab=mm#settings/fwdandpop

  Use it like this:

      Pop3EmailReader r = new Pop3EmailReader(host, username, password);
      final Instant newerThan = Instant.now().minus(3, ChronoUnit.DAYS);
      List<OfflineMessage> messages = r.getMessagesBy("sender@gmail.com", Date.from(newerThan));

 */
public class Pop3EmailReader {
  private String host;
  private String user;
  private String password;

  public Pop3EmailReader(String host, String user, String password) {
    this.host = host;
    this.user = user;
    this.password = password;
  }

  @SneakyThrows
  public List<OfflineMessage> getMessagesBy(MessageFilter filter) {
    Properties properties = new Properties();

    properties.put("mail.pop3.host", host);
    properties.put("mail.pop3.port", "995");
    properties.put("mail.pop3.starttls.enable", "true");
    Session emailSession = Session.getDefaultInstance(properties);

    //create the POP3 store object and connect with the pop server
    Store store = emailSession.getStore("pop3s");

    store.connect(host, user, password);

    //create the folder object and open it
    Folder emailFolder = store.getFolder("INBOX");
    emailFolder.open(Folder.READ_ONLY);

    // retrieve the messages from the folder in an array and print it
    List<Message> messages = new ArrayList<Message>(Arrays.asList(emailFolder.getMessages()));
    messages.removeIf(m -> !filter.test(m));

    List<OfflineMessage> offlineMessages = messages.stream().map(OfflineMessage::new).collect(Collectors.toList());
    //close the store and folder objects
    emailFolder.close(false);
    store.close();
    return offlineMessages;
  }

  public List<OfflineMessage> getMessagesBy(String sender, Date newerThan) {
    return getMessagesBy(m -> {
      boolean byDate = newerThan == null || newerThan.before(m.getSentDate());
      Stream<Address> senders = Arrays.stream(m.getFrom());
      boolean bySender = sender == null || senders.anyMatch(a -> a.toString().toLowerCase().contains(sender));
      return byDate && bySender;
    });
  }

  public List<OfflineMessage> getMessagesBy(String sender, int lastMinutes) {
    final Instant newerThan = Instant.now().minus(lastMinutes, ChronoUnit.MINUTES);
    return getMessagesBy(sender, Date.from(newerThan));
  }
}
