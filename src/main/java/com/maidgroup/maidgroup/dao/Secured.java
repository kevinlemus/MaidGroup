package com.maidgroup.maidgroup.dao;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Secured {
    boolean isAdmin() default true;
}
