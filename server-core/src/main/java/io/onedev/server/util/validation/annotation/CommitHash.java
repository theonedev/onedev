package io.onedev.server.util.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.CommitHashValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=CommitHashValidator.class) 
public @interface CommitHash {
	
    String message() default "Not a valid commit hash";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
