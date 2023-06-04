package io.onedev.server.annotation;

import io.onedev.server.validation.validator.ReservedOptionsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= ReservedOptionsValidator.class) 
public @interface ReservedOptions {
	
	String message() default "";
	
	String[] value();
	
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
