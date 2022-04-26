package uz.app.Anno.orm;

import java.lang.reflect.Field;

public class AnnoNotAnnotatedField extends RuntimeException {
    Field notAnnotatedField;

    public AnnoNotAnnotatedField(String message) {
        super(message);
    }

    public AnnoNotAnnotatedField(String message, Field field) {
        super(message);
        this.notAnnotatedField = field;
    }

    public Field getNotAnnotatedField() {
        return notAnnotatedField;
    }
}
