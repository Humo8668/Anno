package uz.app.Anno.orm.exceptions;

import java.lang.reflect.Field;

public class NotAnnotatedFieldException extends RuntimeException {
    Field notAnnotatedField;

    public NotAnnotatedFieldException(String message) {
        super(message);
    }

    public NotAnnotatedFieldException(String message, Field field) {
        super(message);
        this.notAnnotatedField = field;
    }

    public Field getNotAnnotatedField() {
        return notAnnotatedField;
    }
}
