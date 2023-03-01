package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.JobMatchValidator;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JobMatchValidator.class) 
public @interface JobMatch {

	boolean withProjectCriteria() default false;

	boolean withJobCriteria() default false;

	String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}
