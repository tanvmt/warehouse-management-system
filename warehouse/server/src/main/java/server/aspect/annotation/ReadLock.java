package server.aspect.annotation;

import java.util.concurrent.TimeUnit;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadLock {
    long timeout() default 5; // Mặc định chờ 5 giây
    TimeUnit unit() default TimeUnit.SECONDS;
}
