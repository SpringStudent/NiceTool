package io.github.springstudent.bean;
import java.lang.annotation.*;

/**
 * @author zhouning
 * @date 2023/08/21 10:30
 */

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceLimit {
    long time() default 1000;

    int value() default 5;
}