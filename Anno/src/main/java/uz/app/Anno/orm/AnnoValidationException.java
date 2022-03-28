package uz.app.Anno.orm;

public class AnnoValidationException extends Throwable {
    protected String invalidFieldName;

    public AnnoValidationException(String message)
    {
        super(message);
    }

    public AnnoValidationException(String message, String invalidFieldName)
    {
        super(message);
        this.invalidFieldName = invalidFieldName;
    }
    
    public String getInvalidFieldName(){
        return invalidFieldName;
    }
}
