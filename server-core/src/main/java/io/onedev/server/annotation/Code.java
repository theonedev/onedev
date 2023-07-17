/*
 * Copyright OneDev (c) 2005-2008,
 * Date: Feb 24, 2008
 * Time: 4:29:05 PM
 * All rights reserved.
 * 
 * Revision: $Id: Multiline.java 1209 2008-07-28 00:16:18Z robin $
 */
package io.onedev.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.validation.validator.CodeValidator;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=CodeValidator.class) 
public @interface Code {
	
	String GROOVY = "Groovy";
	
	String SHELL = "Shell";
	
	String POWER_SHELL = "PowerShell";
	
	String GROOVY_TEMPLATE = "Java Server Pages";
	
	String language();
	
	String variableProvider() default "";
	
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}