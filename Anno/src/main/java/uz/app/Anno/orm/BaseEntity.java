package uz.app.Anno.orm;

import java.lang.reflect.Field;

import uz.app.Anno.Anno;

public abstract class BaseEntity {

    public abstract boolean isValid();

    public abstract void validate() throws AnnoValidationException;

    public Object getIdValue()
    {
        Field idField = Anno.forEntity(this.getClass()).getIdField();
        idField.setAccessible(true);
        Object result;
        try {
            result = idField.get(this);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
        idField.setAccessible(false);
        return result;
    }
}
