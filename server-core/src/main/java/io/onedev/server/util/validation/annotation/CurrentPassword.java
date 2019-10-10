/*
 * Copyright OneDev Inc.,
 * Date: 2008-2-28
 * All rights reserved.
 *
 * Revision: $Id: PathElement.java 1209 2008-07-28 00:16:18Z robin $
 */
package io.onedev.server.util.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import io.onedev.server.util.validation.CurrentPasswordValidator;

/**
 * @author robin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=CurrentPasswordValidator.class) 
public @interface CurrentPassword {

	String message() default "Current password does not match";
	
	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
