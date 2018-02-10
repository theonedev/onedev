package com.turbodev.server.util.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.turbodev.server.util.validation.DirectoryValidator;

/**
 * @author robin
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=DirectoryValidator.class) 
public @interface Directory {

	boolean writeable();
	
	boolean outsideOfInstallDir();
	
	boolean absolute();
	
	String message() default "Invalid directory";
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
