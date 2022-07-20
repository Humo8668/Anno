package uz.app.Anno.orm;

import uz.app.Anno.orm.exceptions.*;

public interface IEntity {

    public boolean isValid();

    public void validate() throws ValidationException;
}
