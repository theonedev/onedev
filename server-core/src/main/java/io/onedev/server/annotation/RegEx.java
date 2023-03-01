package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.RegExValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=RegExValidator.class) 
public @interface RegEx {

	String pattern();
	
    String message() default "Not matching specified regular expression";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
