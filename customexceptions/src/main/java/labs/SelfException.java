package labs;

public class SelfException extends RuntimeException {
    public SelfException(String message) {
        super(message);
    }
}
