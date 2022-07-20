package uz.app.Anno.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class ReflectionUtil {
    public static Collection<Class<?> > getAnnotatedClasses(Annotation annotation) {
        Reflections reflections = new Reflections("", new TypeAnnotationsScanner());
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation);
        return annotatedClasses;
    }

    public static Collection<Field> getAnnotatedFieldsInClass(Annotation annotation, Class<?> cls) {
        Reflections reflections = new Reflections("", new FieldAnnotationsScanner());
        Set<Field> annotatedFields = reflections.getFieldsAnnotatedWith(annotation);
        return annotatedFields;
    }

    public static Collection<Method> getAnnotatedMethodsInClass(Annotation annotation, Class<?> cls) {
        Reflections reflections = new Reflections("", new MethodAnnotationsScanner());
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(annotation);
        return annotatedMethods;
    }

    public static <T> Collection<Class<? extends T> > getSubtypesOf(Class<T> cls) {
        Reflections reflections = new Reflections("", new SubTypesScanner());
        Set<Class<? extends T>> subtypeClasses = reflections.getSubTypesOf(cls);
        return subtypeClasses;
    }

    /*public static <T extends Annotation> T getAnnotationOnClass(Class<T> annotationClass, Class<?> annotatedClass) {
        return annotatedClass.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getAnnotationOnField(Class<T> annotation, Field field) {
        return field.getAnnotation(annotation);
    }

    public static <T extends Annotation> T getAnnotationOnMethod(Class<T> annotation, Method method) {
        return method.getAnnotation(annotation);
    }*/
}
