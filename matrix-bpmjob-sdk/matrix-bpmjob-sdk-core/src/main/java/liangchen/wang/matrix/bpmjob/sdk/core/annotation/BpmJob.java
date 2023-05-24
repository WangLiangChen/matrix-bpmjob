package liangchen.wang.matrix.bpmjob.sdk.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Liangchen.Wang 2023-05-23 8:33
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BpmJob {
    String value() default "";

    String[] names() default {};
}
