package io.onedev.server.web.editable.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.NumericValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=NumericValidator.class) 
public @interface Numeric {
	boolean interpolative() default false;
	
    String message() default "Invalid number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}
