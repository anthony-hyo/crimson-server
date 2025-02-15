package com.crimson.annotations;

import java.lang.annotation.*;

@Repeatable(RequestValues.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Request {
    String name();

}
