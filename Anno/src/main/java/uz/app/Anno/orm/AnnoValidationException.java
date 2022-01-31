package uz.app.Anno.orm;

public class AnnoValidationException extends Throwable {
    public String errorCode;

    public AnnoValidationException(String errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }
}
