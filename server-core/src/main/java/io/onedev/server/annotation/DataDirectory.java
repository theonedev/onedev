package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.DataDirectoryValidator;

/**
 * @author robin
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy= DataDirectoryValidator.class) 
public @interface DataDirectory {

	boolean exists() default true;

	String message() default "";
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
