package utils.email;

import lombok.SneakyThrows;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.function.Predicate;

public interface MessageFilter extends Predicate<Message> {
    boolean shouldRetain(Message m) throws IOException, MessagingException;

    @SneakyThrows
    @Override
    default boolean test(Message message) {
        return shouldRetain(message);
    }
}
