package com.handwin.server.controller;

import java.lang.annotation.*;

/**
 * Created by Danny on 2014-12-03.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Controller {
    String[] value() default {};
    boolean disable() default false;
    int order() default 0;
}
