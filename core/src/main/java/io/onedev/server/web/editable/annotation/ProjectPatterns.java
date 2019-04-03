package io.onedev.server.web.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.ProjectPatternsValidator;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=ProjectPatternsValidator.class) 
public @interface ProjectPatterns {

	String message() default "Malformed project patterns"; 
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}
