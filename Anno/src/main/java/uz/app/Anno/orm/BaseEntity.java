package uz.app.Anno.orm;

public abstract class BaseEntity {

    public abstract boolean isValid();

    public abstract void validate() throws AnnoValidationException;
}
