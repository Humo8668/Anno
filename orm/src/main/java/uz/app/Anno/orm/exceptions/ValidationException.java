package uz.app.Anno.orm.exceptions;

public class ValidationException extends Throwable {
    protected String invalidFieldName;

    public ValidationException(String message)
    {
        super(message);
    }

    public ValidationException(String message, String invalidFieldName)
    {
        super(message);
        this.invalidFieldName = invalidFieldName;
    }
    
    public String getInvalidFieldName(){
        return invalidFieldName;
    }
}
