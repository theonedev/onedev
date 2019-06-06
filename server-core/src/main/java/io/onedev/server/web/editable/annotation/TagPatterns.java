package io.onedev.server.web.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.TagPatternsValidator;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=TagPatternsValidator.class) 
public @interface TagPatterns {

	String message() default "Malformed tag patterns"; 
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}
