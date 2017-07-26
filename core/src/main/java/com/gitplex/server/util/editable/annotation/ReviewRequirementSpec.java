package com.gitplex.server.util.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.gitplex.server.util.validation.ReviewRequirementSpecValidator;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=ReviewRequirementSpecValidator.class) 
public @interface ReviewRequirementSpec {

	String message() default "Malformed review requirement spec"; 
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}
