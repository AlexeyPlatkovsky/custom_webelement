package core.tools.func;

@FunctionalInterface
public interface JFunc<R> {
    R invoke() throws Exception;

    default R execute() {
        try {
            return invoke();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
