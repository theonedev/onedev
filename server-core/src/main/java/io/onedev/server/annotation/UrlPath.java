package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.UrlPathValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=UrlPathValidator.class) 
public @interface UrlPath {

	String message() default "";
	
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
