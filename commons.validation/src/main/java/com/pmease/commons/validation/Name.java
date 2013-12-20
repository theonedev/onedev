/*
 * Copyright PMEase Inc.,
 * Date: 2008-2-28
 * All rights reserved.
 *
 * Revision: $Id: PathElement.java 1209 2008-07-28 00:16:18Z robin $
 */
package com.pmease.commons.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author robin
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=NameValidator.class) 
public @interface Name {
	String message() default "Name can not contain any of below characters:\n" + 
			NameValidator.invalidChars;

	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
