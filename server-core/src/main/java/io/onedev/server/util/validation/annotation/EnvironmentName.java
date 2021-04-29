package io.onedev.server.util.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.EnvironmentNameValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=EnvironmentNameValidator.class) 
public @interface EnvironmentName {

	String message() default "name should start with letter and can only consist of "
			+ "alphanumeric and underscore characters";
	
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
