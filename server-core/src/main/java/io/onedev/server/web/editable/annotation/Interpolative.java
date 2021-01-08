package io.onedev.server.web.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.InterpolativeValidator;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=InterpolativeValidator.class) 
public @interface Interpolative {

	String message() default "";
	
	String literalSuggester() default "";
	
	String variableSuggester() default "";
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    String exampleVar() default "a";
    
}
