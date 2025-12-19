package server.aspect.annotation;
import java.util.concurrent.TimeUnit;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteLock {
    long timeout() default 10;
    TimeUnit unit() default TimeUnit.SECONDS;
}