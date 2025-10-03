package com.uniquindio.userservice.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)   // Se puede poner en métodos
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsOwner {
}
