package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.AttributeNameValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=AttributeNameValidator.class) 
public @interface AttributeName {

	String message() default "";
	
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
