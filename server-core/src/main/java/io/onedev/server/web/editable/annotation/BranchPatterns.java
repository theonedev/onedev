package io.onedev.server.web.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.BranchPatternsValidator;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=BranchPatternsValidator.class) 
public @interface BranchPatterns {

	String value() default "";
	
	String message() default "Malformed branch patterns"; 
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}
