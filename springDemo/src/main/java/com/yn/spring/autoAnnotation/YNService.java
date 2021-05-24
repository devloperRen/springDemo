package com.yn.spring.autoAnnotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface YNService {
    String value() default "";
}
