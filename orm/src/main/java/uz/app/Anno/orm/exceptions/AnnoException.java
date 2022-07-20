package uz.app.Anno.orm.exceptions;

/**
 * Throws when Anno couldn't map the data to an entity-object.
 * Usually, it's surrounding exception class for base reasons of exceptions.
 */
public class AnnoException extends Throwable{
    public AnnoException(String message)
    {
        super(message);
    }

    public AnnoException(String message, Throwable cause) {
        super(message, cause);
    }
}
