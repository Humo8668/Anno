package uz.app.Anno.service.exceptions;

public class WrongUsageException extends Exception {
    public WrongUsageException(String message) {
        super(message);
    }

    public WrongUsageException(String message, Throwable ex) {
        super(message, ex);
    }

    public WrongUsageException(Throwable ex) {
        super(ex);
    }
}
