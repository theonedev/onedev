package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.EnvNameValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=EnvNameValidator.class) 
public @interface EnvVarName {

	String message() default "name should start with letter and can only consist of "
			+ "alphanumeric and underscore characters";
	
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
