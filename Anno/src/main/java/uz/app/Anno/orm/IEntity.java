package uz.app.Anno.orm;

public interface IEntity {

    public boolean isValid();

    public void validate() throws AnnoValidationException;
}
