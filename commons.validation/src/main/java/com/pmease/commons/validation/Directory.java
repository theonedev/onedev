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
@Constraint(validatedBy=DirectoryValidator.class) 
public @interface Directory {
	String message() default "Make sure the OS user running this program has permission to "
			+ "write files into this directory.";

	Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
