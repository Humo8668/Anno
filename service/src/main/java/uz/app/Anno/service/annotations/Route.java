package uz.app.Anno.service.annotations;


import uz.app.Anno.util.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String value();
    HttpMethod method() default HttpMethod.GET;
}
