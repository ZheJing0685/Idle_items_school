package com.idleitems.school.annotation;

import com.idleitems.school.entity.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    User.Role[] value() default {};
    String message() default "权限不足";
}